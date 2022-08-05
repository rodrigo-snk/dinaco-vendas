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
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ImpedeFaturamentoFinsdeExportacao implements EventoProgramavelJava {
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
            final boolean ehFinsExportacao = "S".equals(StringUtils.getNullAsEmpty(cabOrigVO.asString("AD_FINSEXPORT")));

            DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, BigDecimal.valueOf(6)); // REGRA DE NEG�CIO FINS DE EXPORTA��O
            final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

            HashSet<BigDecimal> tops = new HashSet<>();
            Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
            topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

            // Se TOP de destino estiver na regra de neg�cio 6 ? FINS DE EXPORTA��O e no cabe�alho n�o estiver marcado o flag Fins de Exporta��o
            // Se no cabe�alho estiver marcado o flag Fins de Exporta��o e TOP de destino n�o estiver na regra de neg�cio 6 ? FINS DE EXPORTA��O
            // Impede o faturamento

            if (regraAtiva && !ehFinsExportacao && tops.contains(codTipOper)) {
                throw new MGEModelException("Somente � poss�vel faturar para esta TOP com fins de exporta��o.");
            }
            if (regraAtiva && ehFinsExportacao && !tops.contains(codTipOper)) {
                throw new MGEModelException("Somente � poss�vel faturar para TOPs com fins de exporta��o.");
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
