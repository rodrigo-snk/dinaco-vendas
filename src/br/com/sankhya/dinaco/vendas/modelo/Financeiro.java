package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import javax.ejb.Local;
import java.math.BigDecimal;
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

    public static LocalDate calculaVencimentoMes(LocalDate dtVenc, LinkedList<Object> diasMes) throws MGEModelException {
        int diaDoVencimento = dtVenc.getDayOfMonth();
        int lengthOfMonth = dtVenc.lengthOfMonth();

        for (Object dia : diasMes) {
            if (diaDoVencimento > (Integer) diasMes.peekLast()) {
                dtVenc = dtVenc.withDayOfMonth((Integer) diasMes.peekFirst()).plusMonths(1);
                break;
            } else if ((Integer) dia > diaDoVencimento && (Integer) dia <= lengthOfMonth) {
                dtVenc = dtVenc.withDayOfMonth((Integer) dia);
                break;
            }
        }
        return dtVenc;
    }

    public static void atualizaVencimento(Object nuFin, LocalDate dtVenc) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.FINANCEIRO)
            .prepareToUpdateByPK(nuFin)
            .set("DTVENC", Timestamp.valueOf(dtVenc.atTime(LocalTime.MIDNIGHT)))
            .update();
           // if (true) throw new MGEModelException(String.valueOf(Timestamp.valueOf(dtVenc.atTime(LocalTime.MIDNIGHT))));
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

    }

    public void verificaRegras(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO fin = (DynamicVO) persistenceEvent.getVo();
        Timestamp data = (Timestamp) persistenceEvent.getModifingFields().getNewValue("DTVENC");
        BigDecimal codParc = fin.asBigDecimalOrZero("CODPARC");
        BigDecimal nuFin = fin.asBigDecimalOrZero("NUFIN");
        BigDecimal recDesp = fin.asBigDecimalOrZero("RECDESP");
        LocalDate dtVenc = data.toLocalDateTime().toLocalDate();
        boolean isReceita = recDesp.compareTo(BigDecimal.ONE) == 0;
        boolean isDespesa = recDesp.compareTo(BigDecimal.valueOf(-1)) == 0;

        boolean atualiza = Parceiro.tipoRegra(codParc).equalsIgnoreCase("A") || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("D")) && isDespesa) || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("R")) && isReceita);

        switch (Parceiro.tipoVencimento(codParc)){
            case "S":
                LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue()) && atualiza) {
                    atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                }
                break;

            case "M":
                int diaMesVencimento = dtVenc.getDayOfMonth();
                LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                if (!diasMes.contains(diaMesVencimento) && atualiza) {
                    atualizaVencimento(nuFin,calculaVencimentoMes(dtVenc, diasMes));
                }
                break;

            case "P":
                int dias = Parceiro.maisDias(codParc).intValue();
                if ((dias != 0) && atualiza) {
                    //atualizaVencimento(nuFin, dtVenc.plusMonths(1).withDayOfMonth(dias));
                    atualizaVencimento(nuFin, dtVenc.plusDays(1));
                    if (true) throw new MGEModelException("DEURUIM");
                }
                break;
        }


    }
}
