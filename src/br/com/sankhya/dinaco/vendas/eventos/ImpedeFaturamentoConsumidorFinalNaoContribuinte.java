package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

public class ImpedeFaturamentoConsumidorFinalNaoContribuinte implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = varVO.asBigDecimalOrZero("NUNOTA");
            BigDecimal nuNotaOrig = varVO.asBigDecimalOrZero("NUNOTAORIG");

            //Nota destino
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");

            // Nota de Origem
            DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNotaOrig);
            final boolean ehConsumidorFinalNaoContribuinte = "C".equals(StringUtils.getNullAsEmpty(cabOrigVO.asDymamicVO("Parceiro").asString("CLASSIFICMS")));

            if (!ehCompraOuEntradaOuVenda(cabVO.asString("TIPMOV"))) {

                DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, BigDecimal.valueOf(7)); // 7 ? FATURAMENTO CONSUMIDOR FINAL
                final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

                HashSet<BigDecimal> tops = new HashSet<>();
                Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");

                topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

                // Impede o faturamento
                // Se TOP de destino estiver na regra de neg�cio 7 - FATURAMENTO CONSUMIDOR FINAL N�O CONTRIBUINTE e o Parceiro n�o for consumidor final n�o contribuinte
                // Se o Parceiro for consumidor final e TOP de destino n�o estiver na regra de neg�cio 7 - FATURAMENTO CONSUMIDOR FINAL N�O CONTRIBUINTE

                if (regraAtiva && !ehConsumidorFinalNaoContribuinte && tops.contains(codTipOper)) {
                    throw new MGEModelException("Somente � poss�vel faturar para esta TOP parceiros com classifica��o ICMS - Consumidor Final N�o Contribuinte.");
                }
                if (regraAtiva && ehConsumidorFinalNaoContribuinte && !tops.contains(codTipOper)) {
                    throw new MGEModelException("Somente � poss�vel faturar para TOPs de consumidor final n�o contribuinte.");
                }

            }

        } finally {
            JapeSession.close(hnd);
        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    private boolean ehCompraOuEntradaOuVenda(String tipMov) {
        return "O-C-E-D-N-1-V".indexOf(tipMov) > -1;
    }
}
