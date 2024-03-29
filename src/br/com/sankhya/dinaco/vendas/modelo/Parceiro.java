package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.model.entities.vo.ContatoVO;
import com.sankhya.model.entities.vo.ContatoVOBasico;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.*;

public class Parceiro {

    public static DynamicVO getParceiroByPK(Object codParc) throws MGEModelException {
        DynamicVO parceiroVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            parceiroVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PARCEIRO, codParc);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return parceiroVO;
    }


    public static String cadastraContato(DynamicVO parcVO, String nomeContato, String telefone, String email, String area, String cargo) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO contatoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.CONTATO);

        contatoVO.setProperty("CODPARC", parcVO.asBigDecimal("CODPARC"));
        contatoVO.setProperty("NOMECONTATO", nomeContato);
        contatoVO.setProperty("TELEFONE", telefone);
        contatoVO.setProperty("EMAIL", email);
        contatoVO.setProperty("AD_AREA", area);
        contatoVO.setProperty("CARGO", cargo);

        contatoVO.setProperty("DTCAD", TimeUtils.getNow());
        contatoVO.setProperty("ATIVO", "S");
        dwfFacade.createEntity(DynamicEntityNames.CONTATO, (EntityVO) contatoVO);

        return String.format("Contato %s cadastrado com sucesso para o parceiro %s", contatoVO.asString("NOMECONTATO"), contatoVO.asDymamicVO("Parceiro").asString("NOMEPARC"));

    }

    public static String tipoVencimento(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asString("AD_RVENC_MS");
    }


    public static String tipoRegra(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asString("AD_RVENC_RECDESP");
    }

    public static int prazoVencimentoItens(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asBigDecimalOrZero("AD_PRAZOVENCITENS").intValue();
    }

    public static boolean temPtaxMedio(Object codParc) throws MGEModelException {
        return "S".equals(getParceiroByPK(codParc).asString("AD_PTAXMEDIO"));
    }

    public static LinkedList<Object> getDias(Map<Object, Boolean> map) {

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
        return getParceiroByPK(codParc).asBigDecimalOrZero("CODCENCUS");
    }

    public static BigDecimal getCodCenCusUnidadeNegocio(Object codParc) throws MGEModelException {
        DynamicVO parcVO = getParceiroByPK(codParc);
        if (BigDecimalUtil.isNullOrZero(parcVO.asBigDecimal("AD_CODUNNEG"))) return BigDecimal.ZERO;
        else return parcVO.asDymamicVO("AD_UNNEG").asBigDecimalOrZero("CODCENCUS");
    }


    public static BigDecimal getCodNat(Object codParc) throws MGEModelException {
        return getParceiroByPK(codParc).asBigDecimalOrZero("AD_CODNAT");
    }

    public static Timestamp dataLimiteQueClienteAceitaVencimento(BigDecimal codParc) throws MGEModelException {
        return TimeUtils.dataAdd(TimeUtils.getNow(), prazoVencimentoItens(codParc), 5);
    }
}
