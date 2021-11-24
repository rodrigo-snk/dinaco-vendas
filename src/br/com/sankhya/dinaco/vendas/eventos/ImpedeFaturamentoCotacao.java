package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;

public class ImpedeFaturamentoCotacao implements EventoProgramavelJava {
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

        JapeSession.SessionHandle hnd = null;
        JdbcWrapper jdbc = null;
        try {
            hnd = JapeSession.open();
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = varVO.asBigDecimalOrZero("NUNOTA");
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            //Nota destino
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            final boolean ehPedido = cabVO.asString("TIPMOV").equalsIgnoreCase("P");
            final boolean ehContaEOrdem = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(1130)) == 0;

            // Nota de Origem
            DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
            final boolean ehCotacaoOrig = cabOrigVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(901)) == 0;
            final boolean hasTerceirista = cabOrigVO.asBigDecimalOrZero("AD_CODPARCTERC").compareTo(BigDecimal.ZERO) != 0;

            // Se for Pedido (TIPMOV = 'P') e não for 1030 - Pedido de Venda - Conta e Ordem e nem for 901 - Cotação de Venda
            // Impede o faturamento
            if (ehCotacaoOrig && !ehContaEOrdem && hasTerceirista) {
                throw new MGEModelException("Cotação tem terceirista. Somente é possível faturar para a TOP 1130 - Pedido de Venda - Compra e Ordem.");
            }

        } finally {
            jdbc.closeSession();
        }

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
