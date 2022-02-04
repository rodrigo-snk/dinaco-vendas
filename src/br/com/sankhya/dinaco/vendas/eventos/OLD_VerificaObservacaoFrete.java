package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;


public class OLD_VerificaObservacaoFrete implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isModifingVLRNOTA = persistenceEvent.getModifingFields().isModifing("VLRNOTA");

        if (!isModifingVLRNOTA) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            final BigDecimal codEmp = cabVO.asBigDecimal("CODEMP");
            DynamicVO empVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA_FINANCEIRO, codEmp);
            final boolean topVerificaFreteMinimo  = "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_FRETEMIN")));
            final BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
            final BigDecimal vlrFreteNota = cabVO.asBigDecimalOrZero("VLRFRETE");
            final String observacaoFrete = StringUtils.getNullAsEmpty(cabVO.asString("AD_OBSFRETE"));
            final BigDecimal minimoFrete = empVO.asBigDecimalOrZero("AD_MINFRETE");
            final BigDecimal vlrFrete = empVO.asBigDecimalOrZero("AD_VLRFRETE");

            final boolean notaMenorQueMinimoFrete = vlrNota.compareTo(minimoFrete) <= 0;
            final boolean freteMenorQueFreteMinimo = vlrFreteNota.compareTo(vlrFrete) <= 0;


            if (topVerificaFreteMinimo && notaMenorQueMinimoFrete && freteMenorQueFreteMinimo && observacaoFrete.isEmpty()) {
                throw new MGEModelException("Preencha Observação Frete.");
            }
        }

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
