package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.ItemNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

import static br.com.sankhya.dinaco.vendas.modelo.ItemNota.*;

public class RegraVerificaLote implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        // Na confirmação da nota verifica se lote está preenchido
        // Se Lote (Controle) nos itens não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();

            DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, BigDecimal.valueOf(9)); // REGRA DE NEGÓCIO VERIFICA PREENCHIMENTO DO LOTE
            final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

            HashSet<BigDecimal> tops = new HashSet<>();
            Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
            topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

            // Se TOP de destino estiver na regra de negócio 9 ? VERIFICA PREENCHIMENTO DO LOTE
            // Verifica se em algum item da nota não foi preenchido o lote
            // Impede a confirmação da nota
            if (regraAtiva && tops.contains(cabVO.asBigDecimal("CODTIPOPER"))) {
                Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
                if (itensNota.stream().anyMatch(ItemNota::semLote)){
                    throw new MGEModelException("Verifique o preenchimento do lote nos itens.");
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
