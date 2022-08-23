package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.helper.AnexoSistemaHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.ResultSet;

public class CopiaAnexo implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {



    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO estVO = (DynamicVO) persistenceEvent.getVo();

        JdbcWrapper jdbcWrapper = null;
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        jdbcWrapper = dwfFacade.getJdbcWrapper();
        String instancia = estVO.getValueObjectID().replace(".ValueObject", "");

        NativeSql sql = new NativeSql(jdbcWrapper);
        sql.appendSql("select anx.*\n");
        sql.appendSql("from TSIANX ANX\n");
        sql.appendSql("where ANX.PKREGISTRO = :CODEMP || '_' || :CODPROD|| '_' || :CODLOCAL || '_' || :CONTROLE || '_' || :CODPARC || '_' || :TIPO || '_' || ANX.NOMEINSTANCIA\n");
        sql.appendSql("AND NOMEINSTANCIA = :INSTANCIA");
        sql.setNamedParameter("INSTANCIA", instancia);
        sql.setNamedParameter("CODEMP", estVO.asBigDecimalOrZero("CODEMP"));
        sql.setNamedParameter("CODPROD", estVO.asBigDecimalOrZero("CODPROD"));
        sql.setNamedParameter("CODLOCAL", estVO.asBigDecimalOrZero("CODLOCAL"));
        sql.setNamedParameter("CONTROLE", estVO.asString("CONTROLE"));
        sql.setNamedParameter("CODPARC", estVO.asBigDecimalOrZero("CODPARC"));
        sql.setNamedParameter("TIPO", estVO.asString("TIPO"));

        ResultSet rset = sql.executeQuery();

        while (rset.next()) {

            DynamicVO anexoEstoqueVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance("AnexoSistema");

            EntityPrimaryKey pk = new EntityPrimaryKey(new Object[] {estVO.asBigDecimalOrZero("CODEMP"), estVO.asBigDecimalOrZero("CODPROD"), estVO.asBigDecimalOrZero("CODLOCAL"), estVO.asString("CONTROLE"), estVO.asBigDecimalOrZero("CODPARC"), estVO.asString("TIPO")});
            String chaveArquivo = AnexoSistemaHelper.buildChaveArquivo("Estoque", pk);
            anexoEstoqueVO.setProperty("NOMEINSTANCIA", "Estoque");
            anexoEstoqueVO.setProperty("PKREGISTRO", rset.getString("PKREGISTRO").replace("AD_TGFEST", "Estoque"));
            anexoEstoqueVO.setProperty("NOMEARQUIVO", rset.getString("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("DESCRICAO", rset.getString("DESCRICAO"));
            anexoEstoqueVO.setProperty("NOMEARQUIVO", rset.getString("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("TIPOAPRES", rset.getString("TIPOAPRES"));
            anexoEstoqueVO.setProperty("TIPOACESSO", rset.getString("TIPOACESSO"));
            anexoEstoqueVO.setProperty("CODUSU", rset.getBigDecimal("CODUSU"));
            anexoEstoqueVO.setProperty("DHCAD", TimeUtils.getNow());

            EntityFacadeFactory.getDWFFacade().createEntity("AnexoSistema", (EntityVO) anexoEstoqueVO);
            anexoEstoqueVO.setProperty("CHAVEARQUIVO", gerarMD5ChaveArquivo(anexoEstoqueVO.asBigDecimalOrZero("NUATTACH").toString() + "_" + anexoEstoqueVO.asString("PKREGISTRO")));
            EntityFacadeFactory.getDWFFacade().saveEntity("AnexoSistema", (EntityVO) anexoEstoqueVO);

            Path sourceDirectory = Paths.get(Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath() + "/Sistema/Anexos/" + rset.getString("NOMEINSTANCIA")) + "/" + rset.getObject("CHAVEARQUIVO"));
            Path targetDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath()+"/Sistema/Anexos/"+anexoEstoqueVO.asString("NOMEINSTANCIA")+"/"+anexoEstoqueVO.asString("CHAVEARQUIVO"));

            Files.copy(sourceDirectory, targetDirectory);

        }


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

    private String gerarMD5ChaveArquivo(String chaveArquivo) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return StringUtils.toHexString(md5.digest(chaveArquivo.getBytes()));
    }
}
