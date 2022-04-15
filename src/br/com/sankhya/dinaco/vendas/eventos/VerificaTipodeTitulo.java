package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ParceiroVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;

import java.math.BigDecimal;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.getFinanceirosByNunota;



public class VerificaTipodeTitulo implements EventoProgramavelJava {

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO finVO = (DynamicVO) persistenceEvent.getVo();
        ParceiroVO parcVO = (ParceiroVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PARCEIRO, finVO.asBigDecimalOrZero("CODPARC"), ParceiroVO.class);


        if (!BigDecimalUtil.isNullOrZero(finVO.asBigDecimalOrZero("NUNOTA"))) {
            CabecalhoNotaVO cabVO = (CabecalhoNotaVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, finVO.asBigDecimalOrZero("NUNOTA"), CabecalhoNotaVO.class);

            // Atualiza tipo de título com o tipo de título padrão do Parceiro
            if (ComercialUtils.ehPedidoOuVenda(cabVO.getTIPMOV()) && BigDecimalUtil.isNullOrZero(finVO.asBigDecimalOrZero("CODTIPTIT"))) {
                finVO.setProperty("CODTIPTIT", parcVO.asBigDecimalOrZero("AD_CODTIPTIT"));
            }


            // Verificação substituída pelo evento VerificaFinanceiroSemTipoDeTitulo que faz a verificação na confirmação da nota e não na inclusão do título no Financeiro (TGFFIN)
           /* if (ComercialUtils.ehCompra(cabVO.getTIPMOV()) && BigDecimalUtil.isNullOrZero(finVO.asBigDecimalOrZero("CODTIPTIT"))) {
                throw new MGEModelException("Tipo de título não pode ser 0 - <SEM TIPO DE TÍTULO> para movimentações geradas de compras.");
            }*/

        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {


    }



    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
