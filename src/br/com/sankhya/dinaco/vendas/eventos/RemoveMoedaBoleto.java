package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import java.math.BigDecimal;

public class RemoveMoedaBoleto implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();

        final boolean ehReceita = finVO.asInt("RECDESP") == 1;
        final boolean origemEstoque = "E".equals(finVO.asString("ORIGEM"));

        if (ehReceita && origemEstoque) {
            finVO.setProperty("CODMOEDA", BigDecimal.ZERO);
            finVO.setProperty("VLRMOEDA", BigDecimal.ZERO);
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();

        final boolean ehReceita = finVO.asInt("RECDESP") == 1;
        final boolean origemEstoque = "E".equals(finVO.asString("ORIGEM"));

        if (ehReceita && origemEstoque) {
            finVO.setProperty("CODMOEDA", BigDecimal.ZERO);
            finVO.setProperty("VLRMOEDA", BigDecimal.ZERO);
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
