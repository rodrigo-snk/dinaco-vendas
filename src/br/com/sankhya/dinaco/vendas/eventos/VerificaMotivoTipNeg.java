package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.negociacaoDiferenteDaSugerida;

public class VerificaMotivoTipNeg implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

           if (isConfirmandoNota) {
            final boolean motivoAlteracaoTipNegociacaoNaoPreenchido = StringUtils.getNullAsEmpty(cabVO.asString("AD_MOTALTTIPNEG")).isEmpty();

            if (negociacaoDiferenteDaSugerida(cabVO) && motivoAlteracaoTipNegociacaoNaoPreenchido) {
                    throw new MGEModelException("Preencha o motivo da alteração do tipo de negociação.");
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
