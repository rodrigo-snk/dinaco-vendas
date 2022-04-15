package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class ImpedeFaturamentoExportacao implements EventoProgramavelJava {
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

        JdbcWrapper jdbc = null;
        try {
            JapeSession.open();
            jdbc = EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            jdbc.openSession();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = varVO.asBigDecimalOrZero("NUNOTA");
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            //Nota destino
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            //final boolean ehPedido = cabVO.asString("TIPMOV").equalsIgnoreCase("P");
            final boolean ehExportacao = DataDictionaryUtils.campoExisteEmTabela("AD_EXPORTACAO", "TGFTOP") && "S".equals(cabVO.asDymamicVO("TipoOperacao").asString("AD_EXPORTACAO"));

            // Nota de Origem
            DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
            final boolean parceiroEstrangeiro = !StringUtils.getNullAsEmpty(cabOrigVO.asDymamicVO("Parceiro").asString("IDESTRANGEIRO")).isEmpty();

            // Se TOP de destino estiver marcada como exportação e o parceiro for estrangeiro
            // Impede o faturamento
            if (ehExportacao && !parceiroEstrangeiro) {
                throw new MGEModelException("Somente é possível faturar para esta TOP com parceiros estrangeiros.");
            } else if (!ehExportacao && parceiroEstrangeiro) {
                throw new MGEModelException("Para parceiros estrangeiros só é possível faturar para TOPs do tipo exportação");
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
