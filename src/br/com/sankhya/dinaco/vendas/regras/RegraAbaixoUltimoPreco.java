package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParceiroHellper;
import br.com.sankhya.modelcore.util.SASUtil;
import com.sankhya.model.entities.vo.ParceiroVO;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.SQLException;

import static br.com.sankhya.modelcore.comercial.ComercialUtils.obtemPreco;

public class RegraAbaixoUltimoPreco implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra);
        }
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra);
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

    public void verificaUltimoPreco (ContextoRegra contextoRegra) throws Exception {

            DynamicVO itemNotaVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final BigDecimal nuNota = itemNotaVO.asBigDecimalOrZero("NUNOTA");
            DynamicVO cabVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            DynamicVO produtoVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemNotaVO.asBigDecimalOrZero("CODPROD"));
            BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe(itemNotaVO.asBigDecimalOrZero("CODPROD"));

            BigDecimal valorVendaTabela = ComercialUtils.obtemPreco(cabVO.asBigDecimalOrZero("CODEMP"), cabVO.asBigDecimalOrZero("CODPARC"),
                    null,
                    null,
                    itemNotaVO.asBigDecimalOrZero("CODPROD"),
                    itemNotaVO.asBigDecimalOrZero("CODLOCALORIG"),
                    TimeUtils.getNow(),
                    null,
                    cabVO.asString("TIPMOV"),
                    null).getValorVenda();

            if (ComercialUtils.ehVenda(cabVO.asString("TIPMOV"))) {
                if (ultimoPrecoVendaNFe.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNIT")) > 0) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário do %d - %s menor que o último preço de venda (R$ %.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"),ultimoPrecoVendaNFe));
                }
                if (valorVendaTabela.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNIT"))> 0) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário do %d - %s menor que o último preço de tabela (R$ %.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"),ultimoPrecoVendaNFe));
                }
            }

    }
}
