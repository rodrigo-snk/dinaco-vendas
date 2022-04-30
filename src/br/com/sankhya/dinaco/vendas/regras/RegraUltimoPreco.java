package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Produto;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.*;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;


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
            final boolean validaPrecoAbaixo  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));

            if (validaPrecoAbaixo) {
                Collection<DynamicVO> itensNotaVO = cabVO.asCollection("ItemNota");

                Predicate<DynamicVO> verificaUltimoPreco = item -> {
                    try {
                        return verificaUltimoPreco(item) || verificaPrecoTabela(item);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                };

                Predicate<DynamicVO> semObservacao = item -> StringUtils.getNullAsEmpty(item.asString("AD_OBSULTVLRMOE")).isEmpty();


                Collection<DynamicVO> itens = itensNotaVO.stream()
                        .filter(verificaUltimoPreco)
                        .filter(semObservacao)
                        .collect(Collectors.toList());

                // Se o campo observação estiver vazio, apresenta mensagem de erro.
                StringBuilder mensagem = new StringBuilder();
                formataMensagemErro(itens, mensagem);
                if (mensagem.length() > 0) {
                    throw new MGEModelException(String.valueOf(mensagem));
                }

                formataObservacaoLiberador(itensNotaVO, mensagem);

                if (precisaLiberacao(itensNotaVO)) {
                    liberacaoLimite(contextoRegra,
                            codUsuarioLogado,
                            cabVO,
                            String.valueOf(mensagem),
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
            final boolean ultimoPrecoMaiorQuePrecodoItem = ultimoPrecoVendaNFe.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNITMOE")) > 0 && !BigDecimalUtil.isNullOrZero(ultimoPrecoVendaNFe);

            BigDecimal valorVendaTabela = precoTabela(itemNotaVO, cabVO);

            final boolean valorTabelaMaiorQuePrecodoItem = valorVendaTabela.compareTo(itemNotaVO.asBigDecimalOrZero("VLRUNITMOE")) > 0 && !BigDecimalUtil.isNullOrZero(valorVendaTabela);

            if (validaUltimoPrecoVenda) {
                if (ultimoPrecoMaiorQuePrecodoItem) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário moeda do %d - %s menor que o último preço de venda (%.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), ultimoPrecoVendaNFe));
                }
                if (valorTabelaMaiorQuePrecodoItem) {
                    contextoRegra.getBarramentoRegra().addMensagem(String.format("Valor unitário moeda do %d - %s menor que o preço de tabela (%.2f).", produtoVO.asInt("CODPROD"), produtoVO.asString("DESCRPROD"), valorVendaTabela));
                }
            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }

    }

    private BigDecimal precoTabela(DynamicVO itemVO, DynamicVO cabVO) throws Exception {
        // Verifica ultimo preço de tabela
        return ComercialUtils.obtemPreco(cabVO.asBigDecimalOrZero("CODEMP"),
                cabVO.asBigDecimalOrZero("CODPARC"),
                null,
                null,
                itemVO.asBigDecimalOrZero("CODPROD"),
                itemVO.asBigDecimalOrZero("CODLOCALORIG"),
                TimeUtils.getNow(),
                null,
                cabVO.asString("TIPMOV"),
                null).getValorVenda();
    }

    public boolean verificaUltimoPreco(DynamicVO itemVO) throws Exception {

            DynamicVO cabVO = itemVO.asDymamicVO("CabecalhoNota");
            final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));
            final BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe(itemVO.asBigDecimalOrZero("CODPROD"), cabVO.asBigDecimalOrZero("CODPARC"));
            final boolean ultimoPrecoMaiorQuePrecodoItem = ultimoPrecoVendaNFe.compareTo(itemVO.asBigDecimalOrZero("VLRUNITMOE")) > 0 && !BigDecimalUtil.isNullOrZero(ultimoPrecoVendaNFe);

        return validaUltimoPrecoVenda && ultimoPrecoMaiorQuePrecodoItem;
    }

    public boolean verificaPrecoTabela(DynamicVO itemVO) throws Exception {

        DynamicVO cabVO = itemVO.asDymamicVO("CabecalhoNota");
        final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));

        // Verifica ultimo preço de tabela
        BigDecimal precoTabela = precoTabela(itemVO,cabVO);

        final boolean valorTabelaMaiorQuePrecodoItem = precoTabela.compareTo(itemVO.asBigDecimalOrZero("VLRUNITMOE")) > 0 && !BigDecimalUtil.isNullOrZero(precoTabela);

        return validaUltimoPrecoVenda && valorTabelaMaiorQuePrecodoItem;
    }

    // Exige liberação da nota se algum dos itens estiver com o valor unitário abaixo do preço de tabela ou abaixo do último preço praticado
    private boolean precisaLiberacao(Collection<DynamicVO> itensNotaVO) {
        Predicate<DynamicVO> verificaPreco = item -> {
            try {
                return verificaUltimoPreco(item) || verificaPrecoTabela(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        };

        return itensNotaVO.stream().anyMatch(verificaPreco);
        /*return itensNotaVO.stream().anyMatch(item -> {
            try {
                return (verificaPrecoTabela(item) || verificaUltimoPreco(item));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        });*/
    }

    private StringBuilder formataMensagemErro(Collection<DynamicVO> itens, StringBuilder mensagem) throws Exception {
        for (DynamicVO item:
                itens) {
            if (verificaUltimoPreco(item) && StringUtils.getNullAsEmpty(item.asString("AD_OBSULTVLRMOE")).isEmpty()) {
                mensagem.append(String.format("Preencha observação últ. vlr. moeda no item %d - %s (último preço de venda %.2f).\n", item.asInt("CODPROD"), Produto.getDescricao(item.asBigDecimalOrZero("CODPROD")), item.asBigDecimalOrZero("AD_ULTVLRUNITMOE")));
            }

            if (verificaPrecoTabela(item) && StringUtils.getNullAsEmpty(item.asString("AD_OBSULTVLRMOE")).isEmpty()) {
                mensagem.append(String.format("Preencha observação últ. vlr. moeda no item %d - %s (preço de tabela %.2f).\n", item.asInt("CODPROD"), Produto.getDescricao(item.asBigDecimalOrZero("CODPROD")), precoTabela(item, item.asDymamicVO("CabecalhoNota"))));
            }
        }
        return mensagem;
    }

    private StringBuilder formataObservacaoLiberador(Collection<DynamicVO> itens, StringBuilder mensagem) throws Exception {
        for (DynamicVO item:
                itens) {
            if (verificaUltimoPreco(item)) {
                mensagem.append(String.format("Ult. vlr. moeda no item %d - %s (últ. vlr. unit. moeda: %.2f / vlr. unit. moeda: %.2f ).\n", item.asInt("CODPROD"), Produto.getDescricao(item.asBigDecimalOrZero("CODPROD")), item.asBigDecimalOrZero("AD_ULTVLRUNITMOE"),  item.asBigDecimalOrZero("VLRUNITMOE")));
            }

            if (verificaPrecoTabela(item) ) {
                mensagem.append(String.format("Últ. vlr. moeda no item %d - %s (preço de tabela %.2f).\n", item.asInt("CODPROD"), Produto.getDescricao(item.asBigDecimalOrZero("CODPROD")), precoTabela(item, item.asDymamicVO("CabecalhoNota"))));
            }
        }
        return mensagem;
    }


}
