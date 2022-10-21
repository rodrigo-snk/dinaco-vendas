package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashSet;

public class AtualizaDtFaturamento implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
           hnd = JapeSession.open();

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = varVO.asBigDecimalOrZero("NUNOTA");
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            //Nota destino
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");

            // Nota de Origem
            DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
            final Timestamp dtFatur = cabOrigVO.asTimestamp("DTFATUR");


            final boolean faturamentoFuturo = TimeUtils.compareOnlyDates(TimeUtils.getNow(),dtFatur) < 0;
            final boolean regraAtiva = RegraNegocio.verificaRegra(BigDecimal.valueOf(23), codTipOper);
            // Se TOP de destino estiver na regra de negócio 23 AJUSTA DATA DE FATURAMENTO NOS PEDIDOS
            // Se na cotação de origem a data de faturamento for maior que a data atual
            // Atualiza a data de faturamento do pedido gerado para a data de faturamento da cotação

            if (faturamentoFuturo && regraAtiva) {
                cabVO.setProperty("DTFATUR", cabOrigVO.getProperty("DTFATUR"));
                dwfFacade.saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);
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
