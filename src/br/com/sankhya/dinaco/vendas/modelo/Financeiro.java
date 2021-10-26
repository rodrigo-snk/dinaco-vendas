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

public class Financeiro {

    public static DynamicVO getFinanceiroByPK(Object codParc) throws MGEModelException {
        DynamicVO financeiroVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper financeiroDAO = JapeFactory.dao(DynamicEntityNames.PARCEIRO);
            financeiroVO = financeiroDAO.findByPK(codParc);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return financeiroVO;
    }

   /* public static Timestamp calculaVencimento(LocalDate dtVenc) {

        dtVenc.getDayOfMonth();


        //atualizaVencimento(nuFin, dtVenc);

        return data;
    }*/

    public static void atualizaVencimento(Object nuFin, Object dtVenc) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            JapeFactory.dao(DynamicEntityNames.FINANCEIRO).
                    prepareToUpdateByPK(nuFin)
                    .set("DTVENC", dtVenc)
                    .update();

        } catch (Exception e) {
            throw new Exception("EXCEPTION");
        } finally {
            JapeSession.close(hnd);
        }

    }
}
