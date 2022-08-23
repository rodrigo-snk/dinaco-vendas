package br.com.sankhya.dinaco.vendas.schactions;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.Finder;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.getCotacaoDiaAnterior;

public class AtualizaCustoMoeda implements ScheduledAction {

    private void atualizaCustoMoeda(DynamicVO custoVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO prodVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, custoVO.asBigDecimal("CODPROD"));

        final boolean ehEuro = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(6)) == 0; //  Moeda 6 - EURO
        final boolean ehDolar = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(4)) == 0; // Moeda 4 - DOLAR

        BigDecimal ptaxDiaAnterior = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), custoVO.asTimestamp("DTATUAL"));
        BigDecimal dolarDiaAnterior = getCotacaoDiaAnterior(BigDecimal.valueOf(4), custoVO.asTimestamp("DTATUAL"));
        BigDecimal custoMedioSemICMS = custoVO.asBigDecimal("CUSSEMICM");

        if (ehDolar) {
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMS.divide(dolarDiaAnterior, MathContext.DECIMAL32));
            dwfFacade.saveEntity(DynamicEntityNames.CUSTO, (EntityVO) custoVO);
        }
        if (ehEuro) {
            custoVO.setProperty("AD_CUSSEMICMSEUR", custoMedioSemICMS.divide(ptaxDiaAnterior, MathContext.DECIMAL32));
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMS.divide(dolarDiaAnterior, MathContext.DECIMAL32));
            dwfFacade.saveEntity(DynamicEntityNames.CUSTO, (EntityVO) custoVO);
        }

    }
    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        try {
            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.CUSTO, "this.AD_CUSSEMICMSUSD is NULL OR this.AD_CUSSEMICMSEUR is NULL or this.AD_CUSSEMICMSUSD = 0 or this.AD_CUSSEMICMSEUR = 0");
            finder.setMaxResults(-1);
            Collection<DynamicVO> custos = dwfFacade.findByDynamicFinderAsVO(finder);
            //Collection<DynamicVO> custos = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.CUSTO, "this.AD_CUSSEMICMSUSD IS NOT NULL OR this.AD_CUSSEMICMSEUR IS NOT NULL"));

            custos.stream().forEach(vo -> {
                try {
                    atualizaCustoMoeda(vo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
