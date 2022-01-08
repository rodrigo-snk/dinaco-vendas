package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import com.sankhya.util.BigDecimalUtil;

import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.getFinanceirosByNunota;

public class RegraTipoTitulo implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {


    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        final boolean confirmacaoNota = JapeSession.getPropertyAsBoolean(AtributosRegras.CONFIRMANDO, false);

        if (confirmacaoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            Collection<DynamicVO> finsVO = getFinanceirosByNunota(cabVO.asBigDecimalOrZero("NUNOTA"));

            final boolean tipoTituloZero = finsVO.stream().anyMatch(vo -> BigDecimalUtil.isNullOrZero(vo.asBigDecimalOrZero("CODTIPTIT")));

            if (ComercialUtils.ehCompra(cabVO.asString("TIPMOV")) && tipoTituloZero) {
                throw new MGEModelException("Tipo de título não pode ser 0 - <SEM TIPO DE TITULO> para movimentações geradas de compras. Verifique o Financeiro.");
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
