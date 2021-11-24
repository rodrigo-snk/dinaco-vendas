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
import br.com.sankhya.ws.ServiceContext;

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
}
