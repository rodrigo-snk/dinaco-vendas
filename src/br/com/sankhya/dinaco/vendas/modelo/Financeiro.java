package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

public class Financeiro {

    public static DynamicVO getFinanceiroByPK(Object codParc) throws MGEModelException {
        DynamicVO financeiroVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper financeiroDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);
            financeiroVO = financeiroDAO.findByPK(codParc);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return financeiroVO;
    }

   public static LocalDate calculaVencimento(LocalDate dtVenc, LinkedList<Object> diasSemana) throws MGEModelException {
        int diaDoVencimento = dtVenc.getDayOfWeek().getValue();
        int calculoDias = 0;
       for (Object dia : diasSemana) {
           //if (true) throw new MGEModelException("DEURUIM" + diasSemana.peekLast());
           if (diaDoVencimento > (Integer) diasSemana.peekLast()) {
               calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.peekFirst();
               break;
           } else if (((Integer) dia) > diaDoVencimento) {
               calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.get(diasSemana.indexOf(dia))-7;
               break;
           }
       }
        return dtVenc.plusDays(calculoDias);
    }

    public static void atualizaVencimento(Object nuFin, LocalDate dtVenc) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            //if (true) throw new MGEModelException(String.valueOf(dtVenc));
            JapeFactory.dao(DynamicEntityNames.FINANCEIRO).
                    prepareToUpdateByPK(nuFin)
                    .set("DTVENC", Timestamp.valueOf(dtVenc.atTime(LocalTime.MIDNIGHT)))
                    .update();

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

    }
}
