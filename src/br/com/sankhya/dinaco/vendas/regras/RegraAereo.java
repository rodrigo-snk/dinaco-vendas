package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RegraAereo implements Regra {

    private final BigDecimal nuRng = BigDecimal.valueOf(5); // REGRA DE NEGOCIO AEREO

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {
        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");


        if (isItemNota) {
            DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
            String mensagemAereo = verificaAereo(itemVO);

            if (!StringUtils.isEmpty(mensagemAereo)) contextoRegra.getBarramentoRegra().addMensagem(mensagemAereo);
        }
    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }

    private String verificaAereo(DynamicVO itemVO) throws Exception {
        Set<BigDecimal> tops = new HashSet<>();

        BigDecimal codTipOper  = itemVO.asDymamicVO("CabecalhoNota").asBigDecimalOrZero("CODTIPOPER");
        DynamicVO localVO = itemVO.asDymamicVO("LocalFinanceiro");
        final boolean localAereo = localVO.containsProperty("AD_AEREO") && localVO != null && "S".equals(localVO.asString("AD_AEREO"));

        DynamicVO rngVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.REGRA_NEGOCIO, nuRng);
        final boolean ativo = "S".equals(rngVO.asString("ATIVO"));

        Collection<DynamicVO> topsRngVO = rngVO.asCollection("TopRegraNegocio");
        topsRngVO.forEach(vo -> tops.add(vo.asBigDecimalOrZero("CODTIPOPER")));

        if (ativo && localAereo &&tops.contains(codTipOper)) {
            return "Item está em local aéreo. Verifique o preço de venda.";
        }

        return "";
    }
}
