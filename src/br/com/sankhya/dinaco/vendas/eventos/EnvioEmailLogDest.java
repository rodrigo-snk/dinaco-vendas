package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
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
import java.sql.ResultSet;

public class EnvioEmailLogDest implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            DynamicVO destVO = (DynamicVO) persistenceEvent.getVo();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            BigDecimal codFila = destVO.asBigDecimalOrZero("CODFILA");

            DynamicVO filaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.FILA_MSG, codFila);
            BigDecimal nuNota = filaVO.asBigDecimalOrZero("NUCHAVE");

            if (!BigDecimalUtil.isNullOrZero(nuNota)) {
                DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
                BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
                BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");
                String email = Empresa.getEmailLOG(codEmp);

                DynamicVO empVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, cabVO.asBigDecimalOrZero("CODEMP"));
                final boolean enviaEmailParaLogistica = DataDictionaryUtils.campoExisteEmTabela("AD_ENVIANFELOG", "TSIEMP") && "S".equals(empVO.asString("AD_ENVIANFELOG"));

                if (enviaEmailParaLogistica && RegraNegocio.verificaRegra(BigDecimal.valueOf(12), codTipOper) && !StringUtils.isEmpty(email)) {

                    /*DynamicVO novoAnexoVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ANEXO_POR_MENSAGEM);
                    novoAnexoVO.setProperty("CODFILA", codFila);
                    novoAnexoVO.setProperty("NUANEXO", destVO.asBigDecimalOrZero("NUANEXO"));
                    dwfFacade.createEntity(DynamicEntityNames.ANEXO_POR_MENSAGEM, (EntityVO) novoAnexoVO);*/

                    filaVO.setProperty("EMAIL", filaVO.asString("EMAIL").concat(",").concat(email));
                    //insereDestinatario(email, codFila);

                  
                }
            }


        } finally {
            JapeSession.close(hnd);
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

    private void insereDestinatario(String email, BigDecimal codFila) throws MGEModelException {
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

            sql = new NativeSql(jdbc);

            sql.appendSql("INSERT INTO TMDFMD (CODFILA, SEQUENCIA, EMAIL) VALUES (:CODFILA, 1,:EMAIL");
            sql.setNamedParameter("CODFILA", codFila);
            sql.setNamedParameter("EMAIL", email);
            sql.executeUpdate();


        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JdbcUtils.closeResultSet(rset);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);

        }
    }

    public static BigDecimal insertFilaEmail(String assunto, String email, char[] msgEmail) throws Exception {
        DynamicVO emailVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance("MSDFilaMensagem");
        emailVO.setProperty("ASSUNTO", assunto);
        emailVO.setProperty("EMAIL", email);
        emailVO.setProperty("MENSAGEM", msgEmail);
        emailVO.setProperty("CODCON", BigDecimal.ZERO);
        emailVO.setProperty("STATUS", "Pendente");
        emailVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3L));
        emailVO.setProperty("TIPOENVIO", "E");
        emailVO.setProperty("MIMETYPE", "text/html");
        EntityFacadeFactory.getDWFFacade().createEntity("MSDFilaMensagem", (EntityVO)emailVO);

        return emailVO.asBigDecimal("CODFILA");
    }

    public static BigDecimal copiaEmail(DynamicVO filaVO, String email) throws Exception {
        DynamicVO emailVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance("MSDFilaMensagem");
        emailVO.setProperty("ASSUNTO", filaVO.asString("ASSUNTO"));
        emailVO.setProperty("EMAIL", email);
        emailVO.setProperty("MENSAGEM", filaVO.asClob("MENSAGEM"));
        emailVO.setProperty("CODCON", filaVO.asBigDecimalOrZero("CODCON"));
        emailVO.setProperty("STATUS", "Pendente");
        emailVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3L));
        emailVO.setProperty("TIPOENVIO", "E");
        emailVO.setProperty("MIMETYPE", filaVO.asString("MIMETYPE"));
        emailVO.setProperty("TIPODOC", filaVO.asString("TIPODOC"));
        emailVO.setProperty("NUCHAVE", filaVO.asBigDecimalOrZero("NUCHAVE"));

        EntityFacadeFactory.getDWFFacade().createEntity("MSDFilaMensagem", (EntityVO)emailVO);

        return emailVO.asBigDecimal("CODFILA");
    }

}
