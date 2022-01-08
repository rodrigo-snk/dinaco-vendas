package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;

public class CabecalhoNota {

    public static Collection<DynamicVO> buscaNotasOrigem(Object nuNota) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        Collection<DynamicVO> notasVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTA = ?", nuNota);
            finder.setMaxResults(-1);
            notasVO = dwfFacade.findByDynamicFinderAsVO(finder);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return notasVO;
    }

    public static DynamicVO buscaNotaPelaPK(BigDecimal nuNota) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        DynamicVO notaVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            notaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return notaVO;
    }

    public static BigDecimal ultimoPrecoVendaNFe(BigDecimal codProd) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        Collection<DynamicVO> itensVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.CODPROD = ?", codProd);
            finder.setMaxResults(-1);
            finder.setOrderBy("NUNOTA DESC");
            itensVO = dwfFacade.findByDynamicFinderAsVO(finder);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

        for (DynamicVO itemVO: itensVO) {
            DynamicVO cabVO = buscaNotaPelaPK(itemVO.asBigDecimalOrZero("NUNOTA"));
            if (ComercialUtils.ehNFEAprovada(cabVO) && ComercialUtils.ehVenda(cabVO.asString("TIPMOV"))){
                return itemVO.asBigDecimal("VLRUNIT");
            }
        }
        return BigDecimal.ZERO;
    }

    public static void updateCodCenCus(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CODCENCUS",Parceiro.getCodCenCus(cabVO.getProperty("CODPARC")))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void updateCodNat(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CODNAT",Parceiro.getCodNat(cabVO.getProperty("CODPARC")))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void update(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CIF_FOB",cabVO.getProperty("CIF_FOB"))
                    .set("AD_REDESPACHO", cabVO.getProperty("AD_REDESPACHO"))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void verificaFormaEntrega(DynamicVO notaVO) throws Exception {

        String formaEntrega = StringUtils.getNullAsEmpty(notaVO.asString("AD_FORMAENTREGA"));

        switch (formaEntrega) {
            //CIF 2-4-5-6-7
            case "2":
            case "6":
            case "7":
                notaVO.setProperty("AD_REDESPACHO", "N");
                notaVO.setProperty("CIF_FOB","C");
                break;
            case "4":
            case "5":
                notaVO.setProperty("AD_REDESPACHO", "S");
                notaVO.setProperty("CIF_FOB","C");
                break;
            case "1": //FOB 1
                notaVO.setProperty("CIF_FOB","F");
                notaVO.setProperty("AD_REDESPACHO", "N");
                break;
            case "3": //Sem Frete
            default:
                notaVO.setProperty("CIF_FOB","S");
                notaVO.setProperty("AD_REDESPACHO", "N");
                break;
        }
        CabecalhoNota.update(notaVO);
    }

    /**
     *  Verifica se frete diferente de FOB ou Sem Frete e se Parceiro Transportadora não está preenchido
     */
    public static void verificaTransportadoraObrigatoria(DynamicVO cabVO) throws Exception {
        String cifFob = cabVO.asString("CIF_FOB");
        BigDecimal codParcTransp = cabVO.asBigDecimalOrZero("CODPARCTRANSP");
        final boolean naoPrecisaTransportadora = cifFob.equalsIgnoreCase("S") || cifFob.equalsIgnoreCase("F");

        if (!naoPrecisaTransportadora && BigDecimalUtil.isNullOrZero(codParcTransp)){
            //throw new MGEModelException("Transportadora obrigatória, Forma Entrega: " +cabVO.asString("AD_FORMAENTREGA")+ ", CIF/FOB: " +cabVO.asString("CIF_FOB")+ ", Transportadora: " +codParcTransp);
            throw new MGEModelException("Transportadora obrigatória para a forma de entrega selecionada. Verifique na aba Transporte.");
        }
    }
}
