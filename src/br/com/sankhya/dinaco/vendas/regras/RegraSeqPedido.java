package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import com.sankhya.util.StringUtils;

import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.ItemNota.exigeCodCliente;
import static br.com.sankhya.dinaco.vendas.modelo.ItemNota.exigeSeqPedido2;

public class RegraSeqPedido implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        // Na confirmação da nota verifica se a TOP e Parceiro exigem Seq. no Pedido.
        // Se Seq. no Pedido nos itens não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
            if (exigeSeqPedido2(cabVO) && itensNota.stream().anyMatch(vo -> StringUtils.getNullAsEmpty(vo.asString("SEQPEDIDO2")).isEmpty())){
                throw new MGEModelException("Tipo de Operação e Parceiro exigem preenchimento da Seq. no Pedido para cada item.");
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
