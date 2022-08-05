package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.exigeNumPedido2;

/**
 * Prenche NUMPEDIDO2 nos itens
 * Evento na ItemNota (TGFITE)
 */
public class PreencheNumPedido2Item implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO itemVO = (DynamicVO) persistenceEvent.getVo();
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimal("NUNOTA"), CabecalhoNotaVO.class);

        if (!StringUtils.getNullAsEmpty(cabVO.asString("NUMPEDIDO2")).isEmpty()){
            itemVO.setProperty("NUMPEDIDO2", cabVO.asString("NUMPEDIDO2"));
        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {



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
