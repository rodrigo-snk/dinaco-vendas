package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelperTest;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Optional;

public class AcaoTeste implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        /*BigDecimal codProd = BigDecimal.valueOf((Integer) contextoAcao.getParam("CODPROD"));
        (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
        DynamicVO itemNotaVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, codProd);
        DynamicVO cabVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);*/

        Registro[] linhas = contextoAcao.getLinhas();

        for(Registro linha: linhas) {

            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);

            Optional<DynamicVO> var = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTA =?", nuNota)).stream().findAny();
            if (var.isPresent()) {
                DynamicVO cabOrigVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, var.get().asBigDecimalOrZero("NUNOTAORIG"));

                cabVO.setProperty("AD_RUBPROJ", cabOrigVO.getProperty("AD_RUBPROJ"));
                EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);

                JdbcWrapper jdbc = EntityFacadeFactory.getCoreFacade().getJdbcWrapper();
                jdbc.openSession();

                NativeSql sql = new NativeSql(jdbc);

                sql.appendSql("UPDATE TGFCAB SET AD_RUBPROJ = :RUBRICA WHERE NUNOTA = :NUNOTA");

                sql.setNamedParameter("RUBRICA", cabOrigVO.getProperty("AD_RUBPROJ"));
                sql.setNamedParameter("NUNOTA", cabVO.asBigDecimalOrZero("NUNOTA"));

                sql.executeUpdate();

                jdbc.closeSession();

            }





        }









    }
}
