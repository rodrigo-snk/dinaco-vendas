package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.pes.model.helpers.EmailHelper;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.getCotacaoDiaAnterior;

public class Custo {
    public static void atualizaCustoMoeda(DynamicVO custoVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO prodVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, custoVO.asBigDecimal("CODPROD"));

        FinderWrapper finder = new FinderWrapper("CustoItem", "this.CODPROD = ? and this.CONTROLE = ? and this.DTATUAL = ?", new Object[]{custoVO.asBigDecimalOrZero("CODPROD"), custoVO.asString("CONTROLE"), custoVO.asTimestamp("DTATUAL")});
        Collection<DynamicVO> custoItem = dwfFacade.findByDynamicFinderAsVO(finder);

        BigDecimal nuNota = custoItem.stream().findFirst().isPresent() ? custoItem.stream().findFirst().get().asBigDecimalOrZero("NUNOTA") : BigDecimal.ZERO;
        BigDecimal sequencia = custoItem.stream().anyMatch(vo -> vo.asInt("SEQUENCIA") > 0) ? custoItem.stream().filter(vo -> vo.asInt("SEQUENCIA") > 0).findFirst().get().asBigDecimalOrZero("SEQUENCIA") : BigDecimal.ZERO;

        BigDecimal ptaxDI = null;
        BigDecimal dolarDI = null;
        //boolean ehTop700 = false;
        boolean ehTop212 = false;

        BigDecimal custoDolarDigitado = null;
        if (!BigDecimalUtil.isNullOrZero(nuNota)) {
            DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, new Object[]{nuNota});
            final boolean ehTop200 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(200)) == 0;
            final boolean ehTop215 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(200)) == 0;
            final boolean ehTop203 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(203)) == 0;
            final boolean ehTop1706 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(1706)) == 0;
            ehTop212 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(212)) == 0;
            //ehTop700 = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(700)) == 0;

            if (ehTop203) {
                DynamicVO itemVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_NOTA, new Object[]{nuNota, sequencia});
                Collection<DynamicVO>  decImpVO = itemVO.asCollection("ImpostosImportacao.DeclaracaoImportacao");
                if (CollectionUtils.isNotEmpty(decImpVO)) {
                    Timestamp dtRegistro = decImpVO.stream().findFirst().get().asTimestamp("DTREGISTRO");
                    ptaxDI = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), dtRegistro);
                    dolarDI = getCotacaoDiaAnterior(BigDecimal.valueOf(4), dtRegistro);
                }
            }

            if (ehTop1706) {
               Timestamp dataDI = cabVO.asTimestamp("AD_DATADI");
                if (dataDI != null) {
                    ptaxDI = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), dataDI);
                    dolarDI = getCotacaoDiaAnterior(BigDecimal.valueOf(4), dataDI);
                }
            }

            if (ehTop200 || ehTop215) {
                // Variaveis com nome DI mas somente para não alterar a lógica
                ptaxDI = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), cabVO.asTimestamp("DTFATUR"));
                dolarDI = getCotacaoDiaAnterior(BigDecimal.valueOf(4), cabVO.asTimestamp("DTFATUR"));
            }

            if (ehTop212) {
                DynamicVO itemVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_NOTA, new Object[]{nuNota, sequencia});
                custoDolarDigitado = itemVO.asBigDecimal("AD_CUSTOMOEINFO");
            }

        }

        final boolean ehEuro = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(6)) == 0; //  Moeda 6 - EURO
        final boolean ehDolar = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(4)) == 0; // Moeda 4 - DOLAR

        BigDecimal ptaxDiaAnterior = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), custoVO.asTimestamp("DTATUAL"));
        BigDecimal dolarDiaAnterior = getCotacaoDiaAnterior(BigDecimal.valueOf(4), custoVO.asTimestamp("DTATUAL"));
        BigDecimal custoMedioSemICMS = custoVO.asBigDecimal("CUSSEMICM");

        BigDecimal custoMedioSemICMSDolar = dolarDI == null ? custoMedioSemICMS.divide(dolarDiaAnterior, MathContext.DECIMAL32) : custoMedioSemICMS.divide(dolarDI, MathContext.DECIMAL32);

        if (ehDolar) {
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMSDolar);
            //if (ehTop700) custoVO.setProperty("AD_CUSSEMICMSUSD", null);
            if (ehTop212) custoVO.setProperty("AD_CUSSEMICMSUSD", custoDolarDigitado);
            dwfFacade.saveEntity(DynamicEntityNames.CUSTO, (EntityVO) custoVO);
        }
        if (ehEuro) {
            BigDecimal custoMedioSemICMSMoeda = ptaxDI == null ? custoMedioSemICMS.divide(ptaxDiaAnterior, MathContext.DECIMAL32) : custoMedioSemICMS.divide(ptaxDI, MathContext.DECIMAL32);
            //if(true) throw new MGEModelException("Custo em euro: " +custoMedioSemICMSMoeda);
            custoVO.setProperty("AD_CUSSEMICMSEUR", custoMedioSemICMSMoeda);
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMSDolar);
           /* if (ehTop700) {
                custoVO.setProperty("AD_CUSSEMICMSEUR", null);
                custoVO.setProperty("AD_CUSSEMICMSUSD", null);
            }*/
            dwfFacade.saveEntity(DynamicEntityNames.CUSTO, (EntityVO) custoVO);
        }

    }
}
