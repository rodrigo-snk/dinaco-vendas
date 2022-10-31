package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

public class ImpedeFaturamentoOportunidadeCancelada implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
           hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            // Pedido de Origem
            DynamicVO cabOrigVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);

            boolean oportunidadeReprovadaOuCancelada = false;
            Collection<DynamicVO> oportunidades = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("AD_NEGVENDA1A", "this.NUNOTA = ?", nuNotaOrig));
            final boolean existeOportunidade = oportunidades.stream().findFirst().isPresent();

            if (existeOportunidade) {
                DynamicVO oportunidadeVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO("AD_NEGVENDA", oportunidades.stream().findFirst().get().asBigDecimal("NUNEGOCIACAO"));
                oportunidadeReprovadaOuCancelada = "CAN".equals(oportunidadeVO.asString("STATUS")) || "REP".equals(oportunidadeVO.asString("STATUS"));
            }

            final boolean regraAtiva = RegraNegocio.verificaRegra(BigDecimal.valueOf(26), cabOrigVO.asBigDecimal("CODTIPOPER"));
            // Se TOP de origem estiver na regra de negócio 26 IMPEDE CONFIRMAÇÃO E FATURAMENTO DE OPORTUNIDADE CANCELADA OU REPROVADA e Oportunidade estiver cancelada ou reprovada
            // Impede o faturamento

            if (regraAtiva && oportunidadeReprovadaOuCancelada) {
                throw new MGEModelException("Não é possível faturar pedido de Oportunidade cancelada ou reprovada.");
            }

        } finally {
            JapeSession.close(hnd);
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
