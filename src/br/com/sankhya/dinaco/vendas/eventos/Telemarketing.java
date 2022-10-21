package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
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

import java.math.BigDecimal;

public class Telemarketing implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO relVO = (DynamicVO) persistenceEvent.getVo();
        final boolean situacaoResolvido = "N".equals(StringUtils.getNullAsEmpty(relVO.asString("PENDENTE")));

        relVO.setProperty("DHPROXCHAM", relVO.getProperty("AD_DHVISITA"));

        if (situacaoResolvido) throw new MGEModelException("Não é possível criar relatório com situação: Resolvido.");
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO relVO = (DynamicVO) persistenceEvent.getVo();

        final boolean isModifingSituacao = persistenceEvent.getModifingFields().isModifing("PENDENTE");
        final boolean situacaoResolvido = "N".equals(StringUtils.getNullAsEmpty(relVO.asString("PENDENTE")));

        final boolean tipoCompromissoAnaliseCliente = relVO.asBigDecimalOrZero("CODHIST").compareTo(BigDecimal.valueOf(5)) == 0;

        relVO.setProperty("DHPROXCHAM", relVO.getProperty("AD_DHVISITA"));


        if (isModifingSituacao && situacaoResolvido && !tipoCompromissoAnaliseCliente) {

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            final boolean semApresentacoesMkt = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_APRTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean semPrototiposApresentados = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_PROTTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean semParticipantes = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_PARTICIPANTE", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            final boolean lifeAndPersonalCare = Parceiro.getParceiroByPK(relVO.asBigDecimalOrZero("CODPARC")).asBigDecimalOrZero("AD_CODUNNEG").compareTo(BigDecimal.valueOf(2)) == 0;
            //final boolean semAssuntosAbordados = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_ASTVSTTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));
            //final boolean semDepartamentosAbordados = CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_DPTOTEL", "this.NUREL = ?", relVO.asBigDecimal("NUREL"))));

            StringBuilder mensagem = new StringBuilder();

            if (semParticipantes) mensagem.append("Adicione ao menos um participante.\n");
            if (semApresentacoesMkt) mensagem.append("Adicione ao menos um material de marketing.\n");
            if (semPrototiposApresentados && lifeAndPersonalCare) mensagem.append("Adicione ao menos um protótipo apresentado para clientes da BU Life & Personal Care.\n");
            //if (semAssuntosAbordados) mensagem.append("Adicione ao menos um assunto abordado.\n");
            //if (semDepartamentosAbordados) mensagem.append("Adicione ao menos um departamento abordado.");

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
