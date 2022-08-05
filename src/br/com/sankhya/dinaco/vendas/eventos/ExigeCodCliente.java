package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.ItemNota.exigeCodCliente;

public class ExigeCodCliente implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        // Na confirmação da nota verifica se a TOP e Parceiro exigem Cód. Cliente.
        // Se Cód. Cliente nos itens não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota) {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            Collection<DynamicVO> itensNota = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", cabVO.asBigDecimal("NUNOTA")));
            if (exigeCodCliente(cabVO) && itensNota.stream().anyMatch(vo -> StringUtils.getNullAsEmpty(vo.asString("AD_CODCLIENTE")).isEmpty())){
                throw new MGEModelException("Tipo de Operação e Parceiro exigem preenchimento do Código do Cliente para cada item.");
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
