package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import com.sankhya.util.CollectionUtils;
import com.sankhya.util.StringUtils;

import java.util.Collection;

/**
 * Prenche NUMPEDIDO2 nos itens
 * Evento na CabecalhoNota (TGFCAB)
 */
public class PreencheNumPedido2 implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        final boolean isModifingNUMPEDIDO2 = persistenceEvent.getModifingFields().isModifing("NUMPEDIDO2");

        if (isModifingNUMPEDIDO2) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
            if (CollectionUtils.isNotEmpty(itensNota) && !StringUtils.isEmpty(cabVO.asString("NUMPEDIDO2"))){
                for (DynamicVO itemVO : itensNota) {
                    itemVO.setProperty("NUMPEDIDO2", cabVO.asString("NUMPEDIDO2"));
                }
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
