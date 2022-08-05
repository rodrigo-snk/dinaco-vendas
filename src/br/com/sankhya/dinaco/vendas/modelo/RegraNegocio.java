package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

public class RegraNegocio {

    public static boolean verificaRegra(BigDecimal nuReg, BigDecimal codTipOper) throws Exception {
        DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, nuReg);
        final boolean regraAtiva = "S".equals(rngVO.asString("ATIVO"));

        HashSet<BigDecimal> tops = new HashSet<>();
        Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
        topsRngVO.forEach(vo -> tops.add(vo.asBigDecimal("CODTIPOPER")));

        return regraAtiva && tops.contains(codTipOper);
    }
}
