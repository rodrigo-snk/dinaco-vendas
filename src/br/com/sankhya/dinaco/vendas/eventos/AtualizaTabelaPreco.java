package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;

import com.sankhya.util.BigDecimalUtil;

import static br.com.sankhya.dinaco.vendas.modelo.Produto.atualizaPrecoTabela;

/**
 * Este evento atualiza a tabela de preços (Excecao - TGFEXC) quando um novo produto é cadastrado e os campos LC do Produto (AD_LCPROD), % Margem Mínima (AD_MARGEMINIMA) e  % Margem Máxima (AD_MARGEMAXIMA) são preenchidos;
 * ou na atualização de pelo menos um desses 3 campos ou atualização da Moeda origem e p/ preço.
 */
public class AtualizaTabelaPreco implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO prodVO = (DynamicVO) persistenceEvent.getVo();
        atualizaPrecoTabela(prodVO);
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

        final boolean alterandoCamposPreco = persistenceEvent.getModifingFields().isModifingAny("AD_LCPROD,AD_MARGEMAXIMA,AD_MARGEMINIMA,CODMOEDA");

        if (alterandoCamposPreco) {
            atualizaPrecoTabela((DynamicVO) persistenceEvent.getVo());
        }

    }



    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
