package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.LiberacaoLimiteVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

public class RegraTipNeg implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");

        if (isCabecalhoNota) {
            verificaSugestaoNegociacao(contextoRegra);
        }
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");
        final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

        if (isConfirmandoNota) {

            if (verificaSugestaoNegociacao(contextoRegra)) {
                DynamicVO notaVO = contextoRegra.getPrePersistEntityState().getNewVO();
                liberacaoLimite(contextoRegra, codUsuarioLogado, notaVO, "",1002);
            }

        }

        if (isCabecalhoNota) {
            final boolean isModifyingCODTIPVENDA = contextoRegra.getPrePersistEntityState().getModifingFields().isModifing("CODTIPVENDA");
            if (isModifyingCODTIPVENDA) verificaSugestaoNegociacao(contextoRegra);
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

    private boolean verificaSugestaoNegociacao(ContextoRegra contextoRegra) throws Exception {
        BigDecimal codTipVenda = contextoRegra.getPrePersistEntityState().getNewVO().asBigDecimal("CODTIPVENDA");
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(contextoRegra.getPrePersistEntityState().getNewVO().asBigDecimalOrZero("CODTIPOPER"));
        boolean ehVenda = CabecalhoNota.ehPedidoOuVenda(topVO.asString("TIPMOV"));
        boolean ehCompra = ComercialUtils.ehCompra(topVO.asString("TIPMOV"));

        DynamicVO complementoParcVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.COMPLEMENTO_PARCEIRO, contextoRegra.getPrePersistEntityState().getNewVO().asBigDecimal("CODPARC"));
        BigDecimal sugestaoEntrada = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGENTR");
        BigDecimal sugestaoSaida = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGSAID");

        if (ehVenda && !BigDecimalUtil.isNullOrZero(sugestaoSaida)) {
            if (codTipVenda.compareTo(sugestaoSaida) != 0) {
                contextoRegra.getBarramentoRegra().addMensagem("Tipo de Negociação diferente da sugerida para o parceiro. Necessita liberação na confirmação da nota.");
                return true;
            }
        } else if (ehCompra && !BigDecimalUtil.isNullOrZero(sugestaoEntrada)) {
            if (codTipVenda.compareTo(sugestaoEntrada) != 0) {
                contextoRegra.getBarramentoRegra().addMensagem("Tipo de Negociação diferente da sugerida para o parceiro. Necessita liberação na confirmação da nota.");
                return true;
            }
        }
        return false;
    }


}
