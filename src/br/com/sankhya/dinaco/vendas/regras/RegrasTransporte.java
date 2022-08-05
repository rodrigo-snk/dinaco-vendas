package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

/**
 * Regra substituída pelo evento RegraTransporte
 */
public class RegrasTransporte implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        //final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");
        /*final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            final boolean ignoraFormaEntrega = DataDictionaryUtils.campoExisteEmTabela("AD_IGNORAFORMAENTREGA", "TGFTOP") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_IGNORAFORMAENTREGA")));
            final boolean isRedespacho =  "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(cabVO.asString("AD_REDESPACHO")));
            final boolean semRedespacho =  BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODPARCREDESPACHO"));
            final boolean semTransportadora =  BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODPARCTRANSP"));
            final boolean obrigaTransportadora = "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_OBRIGATRANSP")));


            if (!ignoraFormaEntrega && isRedespacho && semRedespacho) {
                throw new MGEModelException("Redespacho (Recebedor) é obrigatório.");
            }

            // Verifica se TOP obriga transportadora (AD_OBRIGATRANSP = 'S') e Parceiro Transportadora não preenchido
            if (!ignoraFormaEntrega && obrigaTransportadora && semTransportadora) {
                CabecalhoNota.verificaTransportadoraObrigatoria(cabVO);
            }

        }*/

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            String mensagem = "";

            mensagem = mensagem.concat(CabecalhoNota.verificaRedespacho(cabVO));
            // Verifica se TOP obriga transportadora (AD_OBRIGATRANSP = 'S') e Parceiro Transportadora não preenchido
            mensagem = mensagem.concat(CabecalhoNota.verificaTransportadoraObrigatoria(cabVO));
            if (!mensagem.isEmpty()) {
                mensagem = mensagem.concat("\nVerifique a aba Transporte.");
                throw new MGEModelException(mensagem);
            }
        }
    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }


}
