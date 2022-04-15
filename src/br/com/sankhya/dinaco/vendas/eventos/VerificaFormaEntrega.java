package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.verificaFormaEntrega;

/**
 * Evento no CabecalhoNota (TGFCAB)
 * Verifica as regras da forma de entrega
 */
public class VerificaFormaEntrega implements EventoProgramavelJava {

    private final JdbcWrapper jdbc = null;
    JapeSession.SessionHandle hnd = null;

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        verificaFormaEntrega(cabVO);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isModifyingFormaEntrega = persistenceEvent.getModifingFields().isModifing("AD_FORMAENTREGA");
        if (isModifyingFormaEntrega) verificaFormaEntrega(cabVO);
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) {

    }

}
