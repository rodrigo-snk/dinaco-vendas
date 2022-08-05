package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

public class AjustaLinhaDigitavelECodigoBarras implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();
        final boolean ehReceita = finVO.asBigDecimal("RECDESP").compareTo(BigDecimal.ONE) == 0;

        if (ehReceita) {
            if (StringUtils.getNullAsEmpty(finVO.asString("LINHADIGITAVEL")).isEmpty())  {
                finVO.setProperty("LINHADIGITAVEL", "1");

            }
            if (StringUtils.getNullAsEmpty(finVO.asString("CODIGOBARRA")).isEmpty()) {
                finVO.setProperty("CODIGOBARRA", "1");
            }
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
