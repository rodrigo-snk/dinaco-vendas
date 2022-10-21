package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Email;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.StringUtils;
import com.sun.jna.Native;

import java.sql.ResultSet;
import java.util.Base64;

public class NotificaUsuarioSATIV implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO sativVO = (DynamicVO) persistenceEvent.getVo();
        final boolean semIDSativ = StringUtils.getNullAsEmpty(sativVO.asString("IDSATIV")).isEmpty();

        if (semIDSativ) {

            JapeSession.SessionHandle hnd = null;
            ResultSet rset = null;
            NativeSql sql = null;
            JdbcWrapper jdbc = null;
            try {
                hnd = JapeSession.open();
                EntityFacade entity = EntityFacadeFactory.getDWFFacade();
                jdbc = entity.getJdbcWrapper();
                jdbc.openSession();

                sql = new NativeSql(jdbc);

                sql.appendSql("SELECT TO_CHAR(EXTRACT(YEAR FROM SYSDATE)) || '.' || LPAD(TO_CHAR(TO_NUMBER(SUBSTR(IDSATIV, 6, 4)) + 1), 4, '0') AS IDSATIV FROM AD_SATIV WHERE IDSATIV > TO_CHAR(EXTRACT(YEAR FROM SYSDATE)) ORDER BY IDSATIV DESC");
                rset = sql.executeQuery();

                if (rset.next()) {
                    sativVO.setProperty("IDSATIV", rset.getString("IDSATIV"));
                } else {
                    sql.executeQuery("SELECT TO_CHAR(EXTRACT(YEAR FROM SYSDATE) || '.' || LPAD( '1', 4, '0' )) FROM DUAL");
                    if (rset.next()) sativVO.setProperty("IDSATIV", rset.getString("IDSATIV"));
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

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO sativVO = (DynamicVO) persistenceEvent.getVo();

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        String idSativ = sativVO.asString("IDSATIV");

        //if (true) throw new Exception(idSativ);
        String emailLab = "laboratorio@dinaco.com.br";
        String originalInput = "br.com.sankhya.menu.adicional.AD_SATIV";
        String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

        String contrato = "{\"IDSATIV\":" + idSativ + "}";
        String contratoEncoded = Base64.getEncoder().encodeToString(contrato.getBytes());


        String linkSativ = "<a target=\"_top\" href=\"http://dinaco.snk.ativy.com:40062/mge/system.jsp#app/" + encodedString + "/" + contratoEncoded + "\">Clique aqui para abrir.</a>";

        String mensagem = String.format("O protótipo SATIV %s foi adicionado.\n %s", idSativ, linkSativ);
        char[] msgEmail = mensagem.toCharArray();

        Email.insertFilaEmail(dwfFacade, "SATIV " + idSativ + " adicionado", emailLab, msgEmail);
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isModifingStatus = persistenceEvent.getModifingFields().isModifing("STATUSLAB");

        if (isModifingStatus) {
            DynamicVO sativVO = (DynamicVO) persistenceEvent.getVo();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            String idSativ = sativVO.asString("IDSATIV");

            String email = sativVO.asDymamicVO("Usuario").asString("EMAIL");

            String status = DataDictionaryUtils.getFieldOptionPorValor("AD_SATIV", "STATUSLAB", sativVO.asString("STATUSLAB")).getDescription();

            String originalInput = "br.com.sankhya.menu.adicional.AD_SATIV";
            String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

            String contrato = "{\"IDSATIV\":\"" + idSativ + "\"}";
            String contratoEncoded = Base64.getEncoder().encodeToString(contrato.getBytes());


            String linkSativ = "<a target=\"_top\" href=\"http://dinaco.snk.ativy.com:40062/mge/system.jsp#app/" + encodedString + "/" + contratoEncoded + "\">Clique aqui para abrir.</a>";
            String mensagem = String.format("O status do protótipo SATIV %s foi alterado para %s. \n %s", idSativ, status, linkSativ);
            char[] msgEmail = mensagem.toCharArray();


            if (!StringUtils.getNullAsEmpty(email).isEmpty()) Email.insertFilaEmail(dwfFacade, "SATIV " + idSativ + "  - Status alterado", email, msgEmail);
        }

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
