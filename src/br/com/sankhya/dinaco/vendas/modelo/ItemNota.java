package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.cotacao.model.services.CotacaoHelper;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.mgecomercial.model.centrais.cac.CACSP;
import br.com.sankhya.mgecomercial.model.centrais.cac.CACSPBean;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.CentralCabecalhoNota;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelperTest;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemNota {

    public static boolean exigeCodCliente(DynamicVO cabVO) throws Exception {
        return "S".equals(StringUtils.getNullAsEmpty(Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asString("AD_ITEPRODPARC")))
                && "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_ITEPRODPARC")));

    }

    public static boolean exigeSeqPedido2(DynamicVO cabVO) throws Exception {
        return "S".equals(StringUtils.getNullAsEmpty(Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asString("AD_EXIGESEQPEDIDO2")))
                && "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_EXIGESEQPEDIDO2")));

    }

    public static boolean semLote(DynamicVO itemNotaVO) {
        return StringUtils.getNullAsEmpty(itemNotaVO.asString("CONTROLE")).trim().isEmpty() ;
    }

    public static void atualizaValoresItens(DynamicVO cabVO) throws Exception {

        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

            final boolean topPtaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));

            if (topPtaxDiaAnterior) {
                BigDecimal vlrMoeda = cabVO.asBigDecimalOrZero("VLRMOEDA");
                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                //CabecalhoNotaVO cabecalhoNotaVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, cabVO.asBigDecimal("NUNOTA"), CabecalhoNotaVO.class);
                Collection<ItemNotaVO> itens = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", cabVO.asBigDecimal("NUNOTA")), ItemNotaVO.class);

                //itens.forEach(vo -> vo.setProperty("VLRTOT", vo.getVLRUNIT().multiply(vo.getQTDNEG())));
                ServiceContext servico = ServiceContext.getCurrent();
                JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
                JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

                for (DynamicVO itemVO: itens) {
                    BigDecimal vlrNovoEmReais = vlrMoeda.multiply(itemVO.asBigDecimalOrZero("VLRUNITMOE"));
                    BigDecimal vlrAntigoEmReais = itemVO.asBigDecimalOrZero("VLRUNIT");

                    //if(true) throw new MGEModelException("Vlr. Antigo: " + vlrAntigoEmReais + "\nValor novo: " +vlrNovoEmReais + "\nValor moeda:" +vlrMoeda);

                    // Em 07-08-2022 alterado parametro de vlrNovoEmReais para vlrAntigoEmReais
                    atualizarItemNota(servico,cabVO, itemVO, itemVO.asBigDecimalOrZero("QTDNEG"), vlrMoeda, vlrNovoEmReais);

                    dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
                }
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

    }

    private static void atualizarItemNota(ServiceContext servico, DynamicVO cabVO, DynamicVO itemAtual, BigDecimal qtdNeg, BigDecimal vlrCotacaoMoeda, BigDecimal vlrUnit) throws Exception {
        itemAtual.setProperty("VLRUNIT", vlrUnit);

        CentralItemNota itemNota = new CentralItemNota();
        itemNota.recalcularValores("VLRUNIT", vlrUnit.toString(), itemAtual, cabVO.asBigDecimalOrZero("NUNOTA"));

        List<DynamicVO> itensFatura = new ArrayList<>();
        itensFatura.add(itemAtual);

        CACHelper cacHelper = new CACHelper();
        cacHelper.incluirAlterarItem(cabVO.asBigDecimalOrZero("NUNOTA"), servico, null, false, itensFatura);
        //CACHelper.recalcularValoresMoeda(cabVO, itemAtual, "VLRUNIT", vlrCotacaoMoeda, CACHelper.DecimaisGerais.build(cabVO, itemAtual));

        //if(true) throw new MGEModelException("Vlr. Moeda:" + vlrCotacaoMoeda + "\nVlr Unit: " + itemAtual.asBigDecimalOrZero("VLRUNIT") + "\nVlr Total: " + itemAtual.asBigDecimalOrZero("VLRTOT") + "\nVlr Unit. Moeda: " + itemAtual.asBigDecimalOrZero("VLRUNITMOE"));

    }
}
