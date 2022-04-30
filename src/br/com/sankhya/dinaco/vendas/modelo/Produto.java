package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.util.*;

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
        return StringUtils.getNullAsEmpty(getProdutoByPK(codProd).asString("AD_ESPECIE"));
    }


    public static String getDescricao(BigDecimal codProd) throws MGEModelException {
        return StringUtils.getNullAsEmpty(getProdutoByPK(codProd).asString("DESCRPROD"));

    }

    public static void atualizaPrecoTabela(DynamicVO prodVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        Collection<DynamicVO> tabelaVO = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("ViewTabelaPrecos", "this.ATIVO = 'S' AND this.CODMOEDA = ?", new Object[] {prodVO.asBigDecimal("CODMOEDA")}));
        BigDecimal nuTab = tabelaVO.stream().findAny().isPresent() ? tabelaVO.stream().findAny().get().asBigDecimal("NUTAB") : null;

        if (!BigDecimalUtil.isNullOrZero(nuTab)) {
            Collection<DynamicVO> tabelaPrecoVO = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("Excecao", "this.NUTAB = ? and this.CODPROD = ?" ,new Object[] {nuTab, prodVO.asBigDecimal("CODPROD")}));
            DynamicVO excecaoVO = tabelaPrecoVO.stream().findFirst().isPresent() ? tabelaPrecoVO.stream().findFirst().get() : adicionaNaTabelaPrecos(prodVO, nuTab);

            if (!BigDecimalUtil.isNullOrZero(excecaoVO.asBigDecimal("AD_PRENETMIN"))){
                excecaoVO.setProperty("VLRVENDA", excecaoVO.getProperty("AD_PRENETMIN"));
                dwfFacade.saveEntity("Excecao", (EntityVO) excecaoVO);
            }
        }
    }

    private static DynamicVO adicionaNaTabelaPrecos(DynamicVO prodVO, BigDecimal nuTab) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO exececaoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Excecao");
        exececaoVO.setProperty("CODPROD", prodVO.asBigDecimal("CODPROD"));
        exececaoVO.setProperty("NUTAB", nuTab);
        exececaoVO.setProperty("CODLOCAL", BigDecimal.ZERO);
        exececaoVO.setProperty("CONTROLE", " ");
        dwfFacade.createEntity("Excecao", (EntityVO) exececaoVO);

        return exececaoVO;
    }
}
