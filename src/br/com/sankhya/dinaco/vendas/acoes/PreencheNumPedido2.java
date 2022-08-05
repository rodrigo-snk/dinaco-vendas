package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;

public class PreencheNumPedido2 implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        String numPedido2 = (String) contextoAcao.getParam("NUMPEDIDO2");

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        for (Registro linha: linhas) {
            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");
            BigDecimal sequencia = (BigDecimal) linha.getCampo("SEQUENCIA");

            ItemNotaVO itemNotaVO = (ItemNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_NOTA, new Object[] {nuNota, sequencia}, ItemNotaVO.class);

            itemNotaVO.setProperty("NUMPEDIDO2", numPedido2);
            dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, itemNotaVO);

        }
    }
}
