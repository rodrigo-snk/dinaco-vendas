package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.sql.Timestamp;
import java.time.LocalTime;

import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehCompra;
import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehVenda;

public class PreencheCodCenCus implements EventoProgramavelJava {
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
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        String tipMov = cabVO.asString("TIPMOV");
        // Preeenche com Centro de Custo do Parceiro (TGFPAR.AD_CODCENCUS)
        // Se TIPMOV in ('O','C','E','P','V', 'D')
    if (ehCompra(tipMov) || ehVenda(tipMov)) {
        //cabVO.setProperty("CODCENCUS", Parceiro.getCodCenCus(cabVO.getProperty("CODPARC")));
        //EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);
        CabecalhoNota.updateCodCenCus(cabVO);
        }
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        //afterInsert(persistenceEvent);
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
