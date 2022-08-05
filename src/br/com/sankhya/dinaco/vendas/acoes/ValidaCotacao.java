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
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.util.Optional;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.verificaFormaEntrega;

public class ValidaCotacao implements AcaoRotinaJava {
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


            verificaFormaEntrega(cabVO);

            CabecalhoNota.verificaCRDoParceiro(cabVO);

            EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);








        }









    }
}
