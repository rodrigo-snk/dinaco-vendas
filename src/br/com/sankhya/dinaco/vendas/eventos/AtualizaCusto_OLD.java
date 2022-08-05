package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.math.MathContext;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.getCotacaoDiaAnterior;
/*
Evento substituido por ação agendada pois o evento na TGFCUS não estava sendo acionado
 */
public class AtualizaCusto_OLD implements EventoProgramavelJava {
    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);


    private void atualizaCustoMoeda(DynamicVO custoVO) throws Exception {
        DynamicVO prodVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, custoVO.asBigDecimal("CODPROD"));

        final boolean ehEuro = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(6)) == 0; //  Moeda 6 - EURO
        final boolean ehDolar = prodVO.asBigDecimal("CODMOEDA").compareTo(BigDecimal.valueOf(4)) == 0; // Moeda 4 - DOLAR

        BigDecimal ptaxDiaAnterior = getCotacaoDiaAnterior(prodVO.asBigDecimal("CODMOEDA"), custoVO.asTimestamp("DTATUAL"));
        BigDecimal dolarDiaAnterior = getCotacaoDiaAnterior(BigDecimal.valueOf(4), custoVO.asTimestamp("DTATUAL"));
        BigDecimal custoMedioSemICMS = custoVO.asBigDecimal("CUSSEMICM");

        if (ehDolar) {
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMS.divide(dolarDiaAnterior, MathContext.DECIMAL32));
        }
        if (ehEuro) {
            custoVO.setProperty("AD_CUSSEMICMSEUR", custoMedioSemICMS.divide(ptaxDiaAnterior, MathContext.DECIMAL32));
            custoVO.setProperty("AD_CUSSEMICMSUSD", custoMedioSemICMS.divide(dolarDiaAnterior, MathContext.DECIMAL32));
        }

    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (isConfirmandoNota) {
            DynamicVO custoVO = (DynamicVO) persistenceEvent.getVo();
            atualizaCustoMoeda(custoVO);
        }

    }



    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        if (isConfirmandoNota) {
            DynamicVO custoVO = (DynamicVO) persistenceEvent.getVo();
            atualizaCustoMoeda(custoVO);
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
