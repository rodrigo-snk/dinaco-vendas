package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.model.entities.vo.EmpresaVO;
import com.sankhya.util.BigDecimalUtil;
import org.apache.james.mime4j.message.Entity;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

public class RegraFrete implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();


        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final BigDecimal codEmp = cabVO.asBigDecimal("CODEMP");
            DynamicVO empVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, codEmp);
            //DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            final BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
            final BigDecimal vlrFreteNota = cabVO.asBigDecimalOrZero("VLRFRETE");
            final String observacaoFrete = cabVO.asString("AD_OBSFRETE");
            final BigDecimal minimoFrete = empVO.asBigDecimalOrZero("AD_MINFRETE");
            final BigDecimal vlrFrete = empVO.asBigDecimalOrZero("AD_VLRFRETE");

            final boolean notaMenorQueMinimoFrete = vlrNota.compareTo(minimoFrete) < 1;
            final boolean freteMenorQueFreteMinimo = vlrFreteNota.compareTo(vlrFrete) < 1;

            if (notaMenorQueMinimoFrete && freteMenorQueFreteMinimo && !(observacaoFrete == null)) {
                liberacaoLimite(contextoRegra, codUsuarioLogado, cabVO, observacaoFrete, 1003);
            } else {
                LiberacaoAlcadaHelper.apagaSolicitacoEvento(1003, cabVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
            }
        }

    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }
}
