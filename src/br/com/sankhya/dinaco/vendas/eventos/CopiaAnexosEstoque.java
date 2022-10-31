package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.EntityDAO;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.helper.AnexoSistemaHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.TimeUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class CopiaAnexosEstoque implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (true) throw new Exception("DEURUIMBEFINS");
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

            Collection<DynamicVO> itens = cabVO.asCollection("ItemNota");

            BigDecimal codLocalDestino = itens.stream().filter(vo -> vo.asBigDecimal("SEQUENCIA").compareTo(BigDecimal.ZERO) < 0).findFirst().get().asBigDecimal("CODLOCALORIG");

            Collection<DynamicVO> estoques = EntityFacadeFactory.getDWFFacade().findByDynamicFinder(new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.CODEMP = 2 and this.CODPROD = 7227 and this.CODLOCAL = ?", codLocalDestino));

            if (CollectionUtils.isNotEmpty(estoques)) {
                throw new Exception("Achou o estoque");
            } else {
                throw new Exception("Não achou o estoque no local " + codLocalDestino);

            }

        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {
        if (true) throw new Exception("DEURUIMBEFDEL");

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        if (true) throw new Exception("DEURUIMINS");

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO estVO = (DynamicVO) persistenceEvent.getVo();
        if (true) throw new Exception("DEURUIMUPD" + persistenceEvent.getModifingFields().toString());


    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

        if (true) throw new Exception("DEURUIMBEFCOMMIT");




    }
}
