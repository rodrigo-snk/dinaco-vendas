package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

public class Produto {

    public static DynamicVO getProdutoByPK(Object codProd) throws MGEModelException {
        DynamicVO produtoVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper produtoDAO = JapeFactory.dao(DynamicEntityNames.PRODUTO);
            produtoVO = produtoDAO.findByPK(codProd);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return produtoVO;
    }

    public static String getEspecie(Object codProd) throws MGEModelException {
        return StringUtils.getEmptyAsNull(getProdutoByPK(codProd).asString("AD_ESPECIE"));
    }

}
