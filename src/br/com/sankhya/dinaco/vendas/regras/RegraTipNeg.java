package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.negociacaoDiferenteDaSugerida;
import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

/**
 * Substituída pelas regras 14 e 15
 */
public class RegraTipNeg implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");

        if (isCabecalhoNota) {
            negociacaoDiferenteDaSugerida(contextoRegra, contextoRegra.getPrePersistEntityState().getNewVO());
        }
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        //final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");
        //final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

        if (isCabecalhoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final boolean isModifyingCODTIPVENDA = contextoRegra.getPrePersistEntityState().getModifingFields().isModifing("CODTIPVENDA");

            if (isModifyingCODTIPVENDA) {
                negociacaoDiferenteDaSugerida(contextoRegra, cabVO);
            }

           /* if (isConfirmandoNota) {
                final boolean motivoAlteracaoTipNegociacaoNaoPreenchido = StringUtils.getNullAsEmpty(cabVO.asString("AD_MOTALTTIPNEG")).isEmpty();

                if (negociacaoDiferenteDaSugerida(cabVO)) {
                    if (motivoAlteracaoTipNegociacaoNaoPreenchido) throw new MGEModelException("Preencha o motivo da alteração do tipo de negociação.");
                    liberacaoLimite(contextoRegra, codUsuarioLogado, cabVO, cabVO.asString("AD_MOTALTTIPNEG"),1002);
                } else {
                    LiberacaoAlcadaHelper.apagaSolicitacoEvento(1002, cabVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
                }
            }*/

        }
    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) {

    }

}
