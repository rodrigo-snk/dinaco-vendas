package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.util.Collection;

public class RegraAvisosCab implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

        /*boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        EntityFacade dwf = getDWFFacade();
        JdbcWrapper jdbcWrapper = dwf.getJdbcWrapper();
        jdbcWrapper.openSession();

        if (isItemNota) {
            //USOPROD = 'B' (Brinde)
            DynamicVO itemNotaVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final boolean isBrinde = itemNotaVO.asString("USOPROD").equalsIgnoreCase("B");
            final BigDecimal nuNota = itemNotaVO.asBigDecimalOrZero("NUNOTA");

            DynamicVO cabecalhoNotaVO = (DynamicVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            final BigDecimal codTipOper = (BigDecimal) cabecalhoNotaVO.getProperty("CODTIPOPER");

            if (isBrinde) {
                if (BigDecimal.valueOf(1001).equals(codTipOper)) {
                    contextoRegra.getBarramentoRegra().addMensagem("Voc� adicionou um produto com uso demonstra��o. CODTIPOPER: "+codTipOper);
                }
            }
            jdbcWrapper.closeSession();
        }*/

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

        final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");

        if (isCabecalhoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.FINANCEIRO, "RECDESP = 1 AND PROVISAO = 'N' AND DHBAIXA IS NULL AND DTVENC < SYSDATE - 1 AND this.CODPARC = ?", new Object[] { cabVO.asBigDecimalOrZero("CODPARC") });
            finder.setMaxResults(1);
            Collection<DynamicVO> finsVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinder(finder);

            if (finsVO.size() > 0) {
                contextoRegra.getBarramentoRegra().addMensagem(Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asString("NOMEPARC") + " est� inadimplente.");
            }
        }

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }
}
