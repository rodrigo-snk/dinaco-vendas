package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Financeiro;
import br.com.sankhya.dinaco.vendas.modelo.Produto;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.*;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;


public class RegraUltimoPreco implements Regra {

    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
    final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra, contextoRegra.getPrePersistEntityState().getNewVO());
        }
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            verificaUltimoPreco(contextoRegra, contextoRegra.getPrePersistEntityState().getNewVO());
        }

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));

            if (validaUltimoPrecoVenda) {
                Collection<DynamicVO> itensNotaVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA,"itemNotaVO", "this.NUNOTA = ?", cabVO.asBigDecimalOrZero("NUNOTA")));
                // Exige liberação da nota se algum dos itens estiver com o valor unitário abaixo do preço de tabela ou abaixo do último preço praticado
                if (itensNotaVO
                        .stream()
                        .anyMatch(itemNota -> {
                            try {
                                if (verificaUltimoPreco(itemNota) && StringUtils.getNullAsEmpty(itemNota.asString("AD_OBSULTVLRMOE")).isEmpty()) {
                                    throw new MGEModelException(String.format("Preencha observação ult. vlr. moeda no item %d - %s (último preço de venda %.2f).", itemNota.asInt("CODPROD"), Produto.getDescricao(itemNota.asBigDecimalOrZero("CODPROD")), itemNota.asBigDecimalOrZero("AD_ULTVLRUNITMOE")));
                                }
                                return verificaUltimoPreco(itemNota);
                            } catch (Exception e) {
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

    public void verificaUltimoPreco(ContextoRegra contextoRegra, DynamicVO itemNotaVO) throws MGEModelException {

        try {
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemNotaVO.asBigDecimalOrZero("NUNOTA"));
            DynamicVO produtoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemNotaVO.asBigDecimalOrZero("CODPROD"));
            final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));
            final BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe(itemNotaVO.asBigDecimalOrZero("CODPROD"), cabVO.asBigDecimalOrZero("CODPARC"));
            final boolean  ultimoPrecoMaiorQuePrecodoItem = ultimoPrecoVendaNFe.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNITMOE")) > 0;


            // Verifica ultimo preço de tabela VERIFICAR SE SERA NECESSARIO
           /* BigDecimal valorVendaTabela = ComercialUtils.obtemPreco(cabVO.asBigDecimalOrZero("CODEMP"), cabVO.asBigDecimalOrZero("CODPARC"),
                    null,
                    null,
                    itemNotaVO.asBigDecimalOrZero("CODPROD"),
                    itemNotaVO.asBigDecimalOrZero("CODLOCALORIG"),
                    TimeUtils.getNow(),
                    null,
                    cabVO.asString("TIPMOV"),
                    null).getValorVenda();*/

            if (validaUltimoPrecoVenda && ultimoPrecoMaiorQuePrecodoItem) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário moeda do %d - %s menor que o último preço de venda (%.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), ultimoPrecoVendaNFe));
                    /*  if (valorVendaTabela.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNITMOE")) > 0) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário moeda do %d - %s menor que o último preço de tabela (R$ %.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), ultimoPrecoVendaNFe));
                }*/
            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }

    }

    public boolean verificaUltimoPreco(DynamicVO itemNotaVO) throws Exception {

            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemNotaVO.asBigDecimalOrZero("NUNOTA"));
            final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));
            final BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe(itemNotaVO.asBigDecimalOrZero("CODPROD"), cabVO.asBigDecimalOrZero("CODPARC"));
            final boolean ultimoPrecoMaiorQuePrecodoItem = ultimoPrecoVendaNFe.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNITMOE")) > 0;

            return validaUltimoPrecoVenda && ultimoPrecoMaiorQuePrecodoItem;
    }
}
