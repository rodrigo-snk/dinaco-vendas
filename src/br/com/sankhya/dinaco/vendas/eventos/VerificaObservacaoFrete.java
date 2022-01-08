package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

public class VerificaObservacaoFrete implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isModifingVLRNOTA = persistenceEvent.getModifingFields().isModifing("VLRNOTA");

        if (!isModifingVLRNOTA) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            final BigDecimal codEmp = cabVO.asBigDecimal("CODEMP");
            DynamicVO empVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, codEmp);
            //DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            final BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
            final BigDecimal vlrFreteNota = cabVO.asBigDecimalOrZero("VLRFRETE");
            final String observacaoFrete = cabVO.asString("AD_OBSFRETE");
            final BigDecimal minimoFrete = empVO.asBigDecimalOrZero("AD_MINFRETE");
            final BigDecimal vlrFrete = empVO.asBigDecimalOrZero("AD_VLRFRETE");

            final boolean notaMenorQueMinimoFrete = vlrNota.compareTo(minimoFrete) < 1;
            final boolean freteMenorQueFreteMinimo = vlrFreteNota.compareTo(vlrFrete) < 1;


            if (ComercialUtils.ehVenda(cabVO.asString("TIPMOV")) && notaMenorQueMinimoFrete && freteMenorQueFreteMinimo && observacaoFrete == null) {
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
