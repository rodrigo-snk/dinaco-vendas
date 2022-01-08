package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.*;

public class Parceiro {

    public static DynamicVO getParceiroByPK(Object codParc) throws MGEModelException {
        DynamicVO parceiroVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper parceiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
            parceiroVO = parceiroDAO.findByPK(codParc);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return parceiroVO;
    }

    public static String tipoVencimento(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asString("AD_RVENC_MS");
    }

    public static String tipoRegra(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asString("AD_RVENC_RECDESP");
    }

    protected static LinkedList<Object> getDias(Map<Object, Boolean> map) {

        LinkedList<Object> result = new LinkedList<>();
        for (Map.Entry<Object, Boolean> entry : map.entrySet()) {
            if (Objects.equals(entry.getValue(), true)) {
                result.add(entry.getKey());
            }
            // we can't compare like this, null will throws exception
              /*(if (entry.getValue().equals(value)) {
                  result.add(entry.getKey());
              }*/
        }
        result.sort((d1, d2) -> {
            Integer dia1 = (Integer) d1;
            Integer dia2 = (Integer) d2;
            return dia1.compareTo(dia2);
        });

        return result;
    }

    public static LinkedList<Object> diasSemana(Object codParc) throws MGEModelException {
        Map<Object, Boolean> mapDias = new HashMap<>();
        DynamicVO parceiro = getParceiroByPK(codParc);

        mapDias.put(DayOfWeek.MONDAY.getValue(), parceiro.asString("AD_RVENC_SEG").equals("S"));
        mapDias.put(DayOfWeek.TUESDAY.getValue(), parceiro.asString("AD_RVENC_TER").equals("S"));
        mapDias.put(DayOfWeek.WEDNESDAY.getValue(),parceiro.asString("AD_RVENC_QUA").equals("S"));
        mapDias.put(DayOfWeek.THURSDAY.getValue(), parceiro.asString("AD_RVENC_QUI").equals("S"));
        mapDias.put(DayOfWeek.FRIDAY.getValue(), parceiro.asString("AD_RVENC_SEX").equals("S"));
        mapDias.put(DayOfWeek.SATURDAY.getValue(), parceiro.asString("AD_RVENC_SAB").equals("S"));
        mapDias.put(DayOfWeek.SUNDAY.getValue(), parceiro.asString("AD_RVENC_DOM").equals("S"));

        return getDias(mapDias);
    }

    public static LinkedList<Object> diasMes(Object codParc) throws MGEModelException {
        Map<Object, Boolean> mapDias = new HashMap<>();
        DynamicVO parceiro = getParceiroByPK(codParc);

        for (int i = 1; i <= 31; i++) {
            mapDias.put(i, parceiro.asString("AD_RVENC_D"+i).equalsIgnoreCase("S"));
        }

        return getDias(mapDias);
    }

    public static BigDecimal maisDias(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asBigDecimalOrZero("AD_RVENC_DD");
    }

    public static BigDecimal getCodCenCus(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asBigDecimalOrZero("AD_CODCENCUS");
    }

    public static BigDecimal getCodNat(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asBigDecimalOrZero("AD_CODNAT");
    }
}
