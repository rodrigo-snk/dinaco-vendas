package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Custo;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import org.apache.struts.action.DynaActionForm;

import java.sql.Timestamp;
import java.util.Collection;

public class AtualizaCustoMoedaDigitado implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

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

        final boolean atualizandoCustoDigitadoMoeda = persistenceEvent.getModifingFields().isModifing("AD_CUSTOMOEINFO");

        DynamicVO itemNotaVO = (DynamicVO) persistenceEvent.getVo();
        Timestamp dtNeg = itemNotaVO.asDymamicVO("CabecalhoNota").asTimestamp("DTNEG");

        if (atualizandoCustoDigitadoMoeda) {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            FinderWrapper finder = new FinderWrapper("Custo", "this.CODPROD = ? and this.CONTROLE = ? and this.DTATUAL", new Object[]{itemNotaVO.asBigDecimalOrZero("CODPROD"), itemNotaVO.asString("CONTROLE"), dtNeg});
            Collection<DynamicVO> custo = dwfFacade.findByDynamicFinderAsVO(finder);

            for (DynamicVO vo: custo) {
                Custo.atualizaCustoMoeda(vo);
            }
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
