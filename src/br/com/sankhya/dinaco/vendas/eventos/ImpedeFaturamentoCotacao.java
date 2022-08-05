package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.checkout.helpers.ParametroHelper;
import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.facades.avisossistema.OSMensagemAvisoCtx;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import br.com.sankhya.pes.model.helpers.NotificacoesAppHelper;
import com.sankhya.util.BigDecimalUtil;

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

        try {
            hnd = JapeSession.open();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = varVO.asBigDecimalOrZero("NUNOTA");
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            //Nota destino
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            //final boolean ehPedido = cabVO.asString("TIPMOV").equalsIgnoreCase("P");
            final boolean ehContaEOrdem = cabVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(1030)) == 0; // 1030 - Pedido de Venda - Conta e Ordem

            // Nota de Origem
            DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
            final boolean ehCotacaoOrig = cabOrigVO.asBigDecimalOrZero("CODTIPOPER").compareTo(BigDecimal.valueOf(901)) == 0; // 901 - Cotação de Venda
            final boolean temTerceirista = !BigDecimalUtil.isNullOrZero(cabOrigVO.asBigDecimalOrZero("CODPARCDEST"));

            // Se TOP de faturamento não for 1030 - Pedido de Venda - Conta e Ordem, TOP origem for 901 - Cotação de Venda e Terceirista estiver preenchido
            // Impede o faturamento
            if (ehCotacaoOrig && !ehContaEOrdem && temTerceirista) {
                throw new MGEModelException("Somente é possível faturar Cotação de Venda (901) com terceirista para Pedido de Venda - Compra e Ordem (1030).");
            } else if (ehCotacaoOrig && ehContaEOrdem && !temTerceirista) {
                throw new MGEModelException("Somente é possível faturar Cotação de Venda (901) para Pedido de Venda - Compra e Ordem (1030) com terceirista preenchido.");
            }

        } finally {
            JapeSession.close(hnd);
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
