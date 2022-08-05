package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import com.sankhya.util.BigDecimalUtil;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.*;

public class VerificaCRNaturezaParceiro implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

        verificaCRDoParceiro(cabVO);
        verificaNaturezaDoParceiro(cabVO);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota && ehPedidoCompraVenda(cabVO.asString("TIPMOV"))) {

            final boolean semNatureza = BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODNAT"));
            final boolean semCR = BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODCENCUS"));

            String mensagem = "";
            if (semNatureza) mensagem = mensagem.concat("Preenchimento da Natureza é obrigatório.\n");
            if (semCR) mensagem = mensagem.concat("Preenchimento  do Centro de Resultado é obrigatório.");

            if (!mensagem.isEmpty()) throw new MGEModelException(mensagem);
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
