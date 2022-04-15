package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.StringUtils;

public class Telemarketing implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO relVO = (DynamicVO) persistenceEvent.getVo();
        final boolean situacaoResolvido = "N".equals(StringUtils.getNullAsEmpty(relVO.asString("PENDENTE")));

        if (situacaoResolvido) throw new MGEModelException("Não é possível criar relatório com situação: Resolvido.");
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO relVO = (DynamicVO) persistenceEvent.getVo();

        final boolean isModifingSituacao = persistenceEvent.getModifingFields().isModifing("PENDENTE");
        final boolean situacaoResolvido = "N".equals(StringUtils.getNullAsEmpty(relVO.asString("PENDENTE")));

        if (isModifingSituacao && situacaoResolvido) {

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            final boolean semApresentacoes = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_APRTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean semParticipantes = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_PARTICIPANTE", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean semAssuntosAbordados = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_ASTVSTTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean semDepartamentosAbordados = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_DPTOTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));

            StringBuilder mensagem = new StringBuilder();

            if (semParticipantes) mensagem.append("Adicione ao menos um participante.\n");
            if (semApresentacoes) mensagem.append("Adicione ao menos uma apresentação.\n");
            if (semAssuntosAbordados) mensagem.append("Adicione ao menos um assunto abordado.\n");
            if (semDepartamentosAbordados) mensagem.append("Adicione ao menos um departamento abordado.");

            if (mensagem.length() > 0) throw new MGEModelException(mensagem.toString());
            
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
