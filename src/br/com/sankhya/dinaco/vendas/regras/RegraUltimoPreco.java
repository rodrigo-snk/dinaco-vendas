package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Financeiro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.gerenteonline.MargemContribuicaoHelper;
import br.com.sankhya.modelcore.util.*;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;


public class RegraUltimoPreco implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra, contextoRegra.getPrePersistEntityState().getNewVO());
        }
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra, contextoRegra.getPrePersistEntityState().getNewVO());
        }

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            if (ComercialUtils.ehVenda(cabVO.asString("TIPMOV"))) {
                // Exige liberação da nota se algum dos itens estiver com o valor unitário abaixo do preço de tabela ou abaixo do último preço praticado
                //noinspection unchecked
                if (EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA,"itemNotaVO", "this.NUNOTA = ?", cabVO.asBigDecimalOrZero("NUNOTA").toString()))
                        .stream()
                        .anyMatch(itemNota -> {
                            try {
                                return verificaUltimoPreco(contextoRegra, (DynamicVO) itemNota);
                            } catch (MGEModelException e) {
                                e.printStackTrace();
                            }
                            return false;
                        })) {
                    Financeiro.liberacaoLimite(contextoRegra,
                            codUsuarioLogado,
                            cabVO,
                            "",
                            1004);
                } else {
                    LiberacaoAlcadaHelper.apagaSolicitacoEvento(1004,
                            cabVO.asBigDecimalOrZero("NUNOTA"),
                            "TGFCAB",
                            null);
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

    public boolean verificaUltimoPreco(ContextoRegra contextoRegra, DynamicVO itemNotaVO) throws MGEModelException {

        try {
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemNotaVO.asBigDecimalOrZero("NUNOTA"));
            DynamicVO produtoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemNotaVO.asBigDecimalOrZero("CODPROD"));
            BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe(itemNotaVO.asBigDecimalOrZero("CODPROD"), cabVO.asBigDecimalOrZero("CODPARC"));

            BigDecimal valorVendaTabela = ComercialUtils.obtemPreco(cabVO.asBigDecimalOrZero("CODEMP"), cabVO.asBigDecimalOrZero("CODPARC"),
                    null,
                    null,
                    itemNotaVO.asBigDecimalOrZero("CODPROD"),
                    itemNotaVO.asBigDecimalOrZero("CODLOCALORIG"),
                    TimeUtils.getNow(),
                    null,
                    cabVO.asString("TIPMOV"),
                    null).getValorVenda();

            if (CabecalhoNota.ehPedidoOuVenda(cabVO.asString("TIPMOV"))) {
                if (ultimoPrecoVendaNFe.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNIT")) > 0) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário do %d - %s menor que o último preço de venda (R$ %.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), ultimoPrecoVendaNFe));
                    return true;
                }
                if (valorVendaTabela.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNIT")) > 0) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário do %d - %s menor que o último preço de tabela (R$ %.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), ultimoPrecoVendaNFe));
                    return true;
                }
            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }

        return false;
    }
}
