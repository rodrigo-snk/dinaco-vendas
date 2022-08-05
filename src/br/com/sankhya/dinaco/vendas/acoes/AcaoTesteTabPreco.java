package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class AcaoTesteTabPreco implements AcaoRotinaJava {
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

                Collection<DynamicVO> tabelas = entity.findByDynamicFinderAsVO(new FinderWrapper("Excecao", "this.AD_VLRVENDAWMW is null"));

                for (DynamicVO excVO: tabelas) {
                    excVO.setProperty("AD_VLRVENDAWMW", BigDecimal.ONE);
                    entity.saveEntity("Excecao", (EntityVO) excVO);

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
