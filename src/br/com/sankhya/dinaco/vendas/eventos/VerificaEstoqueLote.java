package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Estoque;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;


import java.math.BigDecimal;
import java.util.Collection;

public class VerificaEstoqueLote implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        final boolean alterandoLote = persistenceEvent.getModifingFields().isModifing("CONTROLE");

        if (alterandoLote) {
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            DynamicVO itemVO = (DynamicVO) persistenceEvent.getVo();
            String controle = Estoque.getControle(itemVO.asString("CONTROLE"));
            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));

            if (RegraNegocio.verificaRegra(BigDecimal.valueOf(17), cabVO.asBigDecimalOrZero("CODTIPOPER"))) {
                FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.CODEMP = ? and this.CODPROD = ? and this.CODLOCAL = ? and this.CONTROLE = ? and this.CODPARC = 0 and this.TIPO = 'P'", new Object[] {itemVO.asBigDecimalOrZero("CODEMP"), itemVO.asBigDecimalOrZero("CODPROD"), itemVO.asBigDecimalOrZero("CODLOCALORIG"), controle});
                Collection<DynamicVO> estoques = dwfFacade.findByDynamicFinderAsVO(finder);
                final boolean existeEstoque = estoques.stream().findFirst().isPresent();

                if (existeEstoque) {
                    DynamicVO estVO = estoques.stream().findFirst().get();
                    BigDecimal disponivel = estVO.asBigDecimalOrZero("ESTOQUE").subtract(estVO.asBigDecimalOrZero("RESERVADO"));

                    if (Estoque.naoTemEstoqueDisponivelParaQtdNeg(estVO, itemVO)) throw new MGEModelException("Não tem estoque disponível para este item neste local.\n Disponível: " + disponivel);
                } else {
                    throw new MGEModelException("Não existe estoque para este item.");
                }
            }
        }

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
