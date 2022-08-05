package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.FilaConferenciaCrudListener;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

public class ImpedeAlteracaoCabecalho implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            // Verifica se este Nro. Origem � origem de outras notas, ou seja, j� foi faturada
            final boolean foiFaturada = CollectionUtils.isNotEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTAORIG = ?", cabVO.asBigDecimal("NUNOTA"))));

            // REGRA DE NEG�CIO IMPEDE ALTERA��O NO CABE�ALHO DA NOTA
            DynamicVO rngVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, BigDecimal.valueOf(8));
            final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

            HashSet<BigDecimal> tops = new HashSet<>();
            Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
            topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

            // Se TOP estiver na regra de neg�cio 8 ? IMPEDE ALTERA��O NO CABE�ALHO DA NOTA
            // Se a nota que est� sendo alterada j� tiver dado origem a outra nota
            // Impede a altera��o

            if (tops.contains(codTipOper) && regraAtiva && foiFaturada) {
                throw new MGEModelException("N�o � permitido altera��o de cabe�alho de pedido/cota��o faturado.");
            }


        } finally {
            JapeSession.close(hnd);
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
