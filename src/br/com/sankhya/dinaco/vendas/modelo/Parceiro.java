package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;

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

    private static List<Integer> getKeys(Map<Integer, Boolean> map) {

        List<Integer> result = new LinkedList<>();
            for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), true)) {
                    result.add(entry.getKey());
                }
                // we can't compare like this, null will throws exception
              /*(if (entry.getValue().equals(value)) {
                  result.add(entry.getKey());
              }*/
            }

        return result;
    }

    private static List<Integer> getKeys(Map<DayOfWeek, Boolean> map, Boolean value) {

        List<Integer> result = new LinkedList<>();
        if (map.containsValue(value)) {
            for (Map.Entry<DayOfWeek, Boolean> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), value)) {
                    result.add(entry.getKey().getValue());
                }
                // we can't compare like this, null will throws exception
              /*(if (entry.getValue().equals(value)) {
                  result.add(entry.getKey());
              }*/
            }
        }
        return result;
    }

    public static List<Integer>  diasSemana(Object codParc) throws MGEModelException {
        Map<DayOfWeek, Boolean> mapDias = new HashMap<>();

        mapDias.put(DayOfWeek.MONDAY, getParceiroByPK(codParc).asString("AD_RVENC_SEG").equals("S"));
        mapDias.put(DayOfWeek.TUESDAY, getParceiroByPK(codParc).asString("AD_RVENC_TER").equals("S"));
        mapDias.put(DayOfWeek.WEDNESDAY, getParceiroByPK(codParc).asString("AD_RVENC_QUA").equals("S"));
        mapDias.put(DayOfWeek.THURSDAY, getParceiroByPK(codParc).asString("AD_RVENC_QUI").equals("S"));
        mapDias.put(DayOfWeek.FRIDAY, getParceiroByPK(codParc).asString("AD_RVENC_SEX").equals("S"));
        mapDias.put(DayOfWeek.SATURDAY, getParceiroByPK(codParc).asString("AD_RVENC_SAB").equals("S"));
        mapDias.put(DayOfWeek.SUNDAY, getParceiroByPK(codParc).asString("AD_RVENC_DOM").equals("S"));

        return getKeys(mapDias, true);
    }

    public static List<Integer>  diasMes(Object codParc) throws MGEModelException {
        Map<Integer, Boolean> mapDias = new HashMap<>();

        mapDias.put(1, getParceiroByPK(codParc).asString("AD_RVENC_D1").equals("S"));
        mapDias.put(2, getParceiroByPK(codParc).asString("AD_RVENC_D2").equals("S"));
        mapDias.put(3, getParceiroByPK(codParc).asString("AD_RVENC_D3").equals("S"));
        mapDias.put(4, getParceiroByPK(codParc).asString("AD_RVENC_D4").equals("S"));
        mapDias.put(5, getParceiroByPK(codParc).asString("AD_RVENC_D5").equals("S"));

        //Simular outros dias do mês
        for (int i = 6; i <= 31; i++) {
            mapDias.put(i, false);
        }

        return getKeys(mapDias);
    }
}
