package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;

import java.math.BigDecimal;

public class RegraRedespacho implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        //final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            boolean isRedespacho = cabVO.asString("AD_REDESPACHO").equalsIgnoreCase("S");
            boolean semRedespacho =  cabVO.asBigDecimalOrZero("CODPARCREDESPACHO").compareTo(BigDecimal.ZERO) == 0;

            if (isRedespacho && semRedespacho) {
                throw new MGEModelException("Campo redespacho é obrigatório.");
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
