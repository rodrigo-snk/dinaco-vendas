package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class ImpedeFaturamentoZFM implements EventoProgramavelJava {
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
            final boolean ehZonaFrancaManaus = DataDictionaryUtils.campoExisteEmTabela("AD_ZFM", "TGFTOP") && "S".equals(cabVO.asDymamicVO("TipoOperacao").asString("AD_ZFM"));

            if (!ComercialUtils.ehCompra(cabVO.asString("TIPMOV"))) {

                // Nota de Origem
                DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
                final boolean parceiroTemSUFRAMA = !StringUtils.getNullAsEmpty(cabOrigVO.asDymamicVO("Parceiro").asDymamicVO("ComplementoParc").asString("CODSUFRAMA")).isEmpty();

                // Se TOP de destino estiver marcada como Zona Franca de Manaus e o parceiro não tiver SUFRAMA
                // Impede o faturamento
                if (ehZonaFrancaManaus && !parceiroTemSUFRAMA) {
                    throw new MGEModelException("Somente é possível faturar para esta TOP com parceiros da Zona Franca de Manaus.");
                }
                if (!ehZonaFrancaManaus && parceiroTemSUFRAMA) {
                    throw new MGEModelException("Para parceiros da Zona Franca de Manaus só é possível faturar para TOPs deste tipo.");
                }


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
