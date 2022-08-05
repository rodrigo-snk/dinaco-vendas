package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.Estoque;
import br.com.sankhya.dinaco.vendas.modelo.ItemNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

public class RegraVerificaEstoqueDisponivel implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        // Na confirmação da nota verifica se tem estoque disponível dos itens
        // Se não tiver estoque disponível em algum dos itens, impede confirmação da nota.
        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();

            DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, BigDecimal.valueOf(20)); // REGRA DE NEGÓCIO VERIFICA DISPONIBILIDADE DE ESTOQUE DOS ITENS
            final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

            HashSet<BigDecimal> tops = new HashSet<>();
            Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
            topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

            // Se TOP de destino estiver na regra de negócio 20 - VERIFICA DISPONIBILIDADE DE ESTOQUE DOS ITENS
            // Verifica se em algum item da nota não tem estoque disponível
            // Impede a confirmação da nota
            if (regraAtiva && tops.contains(cabVO.asBigDecimal("CODTIPOPER"))) {
                Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
                if (itensNota.stream().anyMatch(Estoque::naoTemEstoqueDisponivel)){
                    throw new MGEModelException("Algum item não tem estoque disponível.");
                }
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
