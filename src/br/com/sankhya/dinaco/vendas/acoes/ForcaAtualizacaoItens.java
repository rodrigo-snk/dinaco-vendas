package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

import static br.com.sankhya.dinaco.vendas.modelo.ItemNota.atualizarItemNota;

public class ForcaAtualizacaoItens implements AcaoRotinaJava {
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

            Collection<DynamicVO> itens = cabVO.asCollection("ItemNota");
            ServiceContext servico = ServiceContext.getCurrent();
            JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
            JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);


            for (DynamicVO itemVO: itens) {

                itemVO.setProperty("VLRUNIT", BigDecimal.valueOf(5.5));

                if (itemVO.asBigDecimalOrZero("CODLOCALORIG").compareTo(BigDecimal.ZERO) == 0) itemVO.setProperty("CODLOCALORIG", BigDecimal.valueOf(202));

                atualizarItemNota(servico,cabVO, itemVO, itemVO.asBigDecimalOrZero("QTDNEG"), null, BigDecimal.valueOf(5.5));

                EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
            }





        }









    }
}
