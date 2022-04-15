package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import com.sankhya.util.StringUtils;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.exigeOC;

public class RegraNroOC implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        // Na confirmação da nota verifica se a TOP e Parceiro exigem OC.
        // Se OC na nota não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            if (exigeOC(cabVO) && StringUtils.getNullAsEmpty(cabVO.asString("NUMPEDIDO2")).isEmpty()) {
                throw new MGEModelException("Tipo de Operação e Parceiro exigem preenchimento da Ordem de Compra.");
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
