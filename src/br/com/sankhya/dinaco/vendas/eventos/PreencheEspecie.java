package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;


import static br.com.sankhya.dinaco.vendas.modelo.Produto.validaEspecie;

public class PreencheEspecie implements EventoProgramavelJava {



    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
            validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
            validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
        validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
