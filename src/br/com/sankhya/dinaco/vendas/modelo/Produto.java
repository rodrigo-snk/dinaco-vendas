package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.updateEspecie;

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
        final boolean camposCalculoDoPrecoPreenchidos = !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_LCPROD")) && !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_MARGEMAXIMA")) && !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_MARGEMINIMA"));

        if (camposCalculoDoPrecoPreenchidos) {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            Collection<DynamicVO> tabelaVO = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("ViewTabelaPrecos", "this.ATIVO = 'S' AND this.CODMOEDA = ?", new Object[] {prodVO.asBigDecimal("CODMOEDA")}));
            BigDecimal nuTab = tabelaVO.stream().findAny().isPresent() ? tabelaVO.stream().findAny().get().asBigDecimal("NUTAB") : null;

            if (!BigDecimalUtil.isNullOrZero(nuTab)) {
                Collection<DynamicVO> tabelaPrecoVO = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper("Excecao", "this.NUTAB = ? and this.CODPROD = ?" ,new Object[] {nuTab, prodVO.asBigDecimal("CODPROD")}));
                DynamicVO excecaoVO = tabelaPrecoVO.stream().findFirst().isPresent() ? tabelaPrecoVO.stream().findFirst().get() : adicionaProdutoNaTabela(prodVO, nuTab);

                if (!BigDecimalUtil.isNullOrZero(excecaoVO.asBigDecimal("AD_PRENETMIN"))){
                    excecaoVO.setProperty("VLRVENDA", excecaoVO.getProperty("AD_PRENETMIN"));
                    dwfFacade.saveEntity("Excecao", (EntityVO) excecaoVO);
                }
            }
        }
    }

    private static DynamicVO adicionaProdutoNaTabela(DynamicVO prodVO, BigDecimal nuTab) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO exececaoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Excecao");
        exececaoVO.setProperty("CODPROD", prodVO.asBigDecimal("CODPROD"));
        exececaoVO.setProperty("NUTAB", nuTab);
        exececaoVO.setProperty("CODLOCAL", BigDecimal.ZERO);
        exececaoVO.setProperty("CONTROLE", " ");
        dwfFacade.createEntity("Excecao", (EntityVO) exececaoVO);

        return exececaoVO;
    }

    public static void validaEspecie(DynamicVO itemVO) throws Exception {

        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            Collection<ItemNotaVO> itensVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", new Object[] { itemVO.asBigDecimalOrZero("NUNOTA") }), ItemNotaVO.class);
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

            final boolean preencheEspecieAutomatico = "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PREENCESPAUT")));

            if (preencheEspecieAutomatico) {
                preencheEspecie(itensVO, cabVO);
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

    }

    private static void preencheEspecie(Collection<ItemNotaVO> itensVO, DynamicVO cabVO) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            Set<String> especies = new HashSet<>();

            itensVO.forEach(vo -> {
                try {
                    especies.add(getEspecie(vo.asBigDecimalOrZero("CODPROD")));
                } catch (MGEModelException e) {
                    e.printStackTrace();
                }
            });

            if (especies.size() > 1) {
                //cabVO.setProperty("VOLUME", "DIVERSOS");
                CabecalhoNota.updateEspecie(cabVO,"DIVERSOS");
            }
            if (especies.size() == 1) {
                String especie = especies.stream().findFirst().get();
                //cabVO.setProperty("VOLUME", StringUtils.getEmptyAsNull(especie));
                CabecalhoNota.updateEspecie(cabVO,StringUtils.getEmptyAsNull(especie));

            }
            if (especies.size() == 0) {
                cabVO.setProperty("VOLUME", null);
                CabecalhoNota.updateEspecie(cabVO,null);
            }
            //EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static DynamicVO getCustoVO(Object codProd, Object codEmp, Object controle) throws MGEModelException {
        DynamicVO custoVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper custoDAO = JapeFactory.dao(DynamicEntityNames.CUSTO);
            custoVO = custoDAO.findOne("this.CODPROD = ? AND this.CODEMP = ? AND this.CONTROLE = ? AND this.NUNOTA = 0 AND this.SEQUENCIA = 0", codProd, codEmp, controle);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return custoVO;
    }

}
