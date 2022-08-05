package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Produto.atualizaPrecoTabela;

public class AtualizaTabelaPrecos implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {



            JdbcWrapper jdbc = null;
            NativeSql sql = null;
            ResultSet rset = null;
            JapeSession.SessionHandle hnd = null;

            try {
                hnd = JapeSession.open();
                hnd.setFindersMaxRows(-1);
                EntityFacade entity = EntityFacadeFactory.getDWFFacade();
                jdbc = entity.getJdbcWrapper();
                jdbc.openSession();

              Collection<DynamicVO> produtos = entity.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PRODUTO, "this.AD_LCPROD IS NOT NULL AND this.AD_MARGEMAXIMA IS NOT NULL and this.AD_MARGEMINIMA IS NOT NULL"));

                for (DynamicVO prodVO: produtos) {
                    final boolean camposPrecosPreenchidos = !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_LCPROD")) && !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_MARGEMAXIMA")) && !BigDecimalUtil.isNullOrZero(prodVO.asBigDecimal("AD_MARGEMINIMA"));

                    if (camposPrecosPreenchidos) {
                        atualizaPrecoTabela(prodVO);
                    }
                }


            } catch (Exception e) {
                MGEModelException.throwMe(e);
            } finally {
                JdbcUtils.closeResultSet(rset);
                NativeSql.releaseResources(sql);
                JdbcWrapper.closeSession(jdbc);
                JapeSession.close(hnd);

            }

        }










}
