package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

public class RegraTransporte implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
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

        }

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
