package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.centrais.CACHelperTest;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class AcaoTeste implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        /*BigDecimal codProd = BigDecimal.valueOf((Integer) contextoAcao.getParam("CODPROD"));
        (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
        DynamicVO itemNotaVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, codProd);
        DynamicVO cabVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);*/





        //throw new MGEModelException(String.valueOf(CabecalhoNota.ultimoPrecoVendaNFe(codProd)));
    }
}
