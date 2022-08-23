package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.dwfdata.vo.EstoqueVO;
import br.com.sankhya.modelcore.helper.AnexoSistemaHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;

public class Estoque {

    public static Timestamp getMenorValidade(BigDecimal codProd, BigDecimal codEmp, BigDecimal codLocal) throws Exception {
        EntityFacade dwfFacade = null;
        JdbcWrapper jdbc = null;

        dwfFacade = EntityFacadeFactory.getDWFFacade();
        jdbc = dwfFacade.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        sql.setNamedParameter("CODPROD", codProd.toString());
        sql.setNamedParameter("CODEMP", codEmp.toString());
        sql.setNamedParameter("CODLOCAL", codLocal.toString());
        //sql.setNamedParameter("CONTROLE", controle);

        ResultSet rset = sql.executeQuery("SELECT MIN(EST.DTVAL) DTVAL FROM TGFEST EST JOIN TGFLOC LOC ON EST.CODLOCAL = LOC.CODLOCAL WHERE EST.ATIVO = 'S' AND EST.TIPO = 'P' AND LOC.AD_FEFO = 'S' AND EST.CODPROD = :CODPROD AND EST.CODEMP = :CODEMP AND EST.CODLOCAL = :CODLOCAL AND EST.DTVAL >= SYSDATE AND (EST.ESTOQUE-EST.RESERVADO) > 0");
        EstoqueVO estoqueVO = (EstoqueVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance(DynamicEntityNames.ESTOQUE, EstoqueVO.class);


        if (rset.next()) {
            return rset.getTimestamp("DTVAL");
        }

        return null;
    }

    public static boolean naoTemEstoqueDisponivelParaQtdNeg(DynamicVO estVO, DynamicVO itemVO) {
        BigDecimal disponivel = estVO.asBigDecimalOrZero("ESTOQUE").subtract(estVO.asBigDecimalOrZero("RESERVADO"));
        return disponivel.subtract(itemVO.asBigDecimalOrZero("QTDNEG")).compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean naoTemEstoqueDisponivel(DynamicVO itemVO) {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.CODEMP = ? and this.CODPROD = ? and this.CODLOCAL = ? and this.CONTROLE = ? and this.CODPARC = 0 and this.TIPO = 'P'", new Object[] {itemVO.asBigDecimalOrZero("CODEMP"), itemVO.asBigDecimalOrZero("CODPROD"), itemVO.asBigDecimalOrZero("CODLOCALORIG"), itemVO.asString("CONTROLE")});
        Collection<DynamicVO> estoques = null;
        try {
            estoques = dwfFacade.findByDynamicFinderAsVO(finder);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final boolean existeEstoque = estoques.stream().findFirst().isPresent();
        if (existeEstoque) {
            DynamicVO estVO = estoques.stream().findFirst().get();
            return estVO.asBigDecimalOrZero("ESTOQUE").subtract(estVO.asBigDecimalOrZero("RESERVADO")).compareTo(BigDecimal.ZERO) < 0;
        }
        return false;

    }

   /* public static String itensSemEstoqueDisponivel(DynamicVO cabVO) {
        String mensagem = "";
        Collection<DynamicVO> itensNota = cabVO.asCollection("ItemNota");
        if (itensNota.stream().filter(Estoque::naoTemEstoqueDisponivel)){

        }

    }*/


    public static String getControle(String controle) {
        return (StringUtils.getEmptyAsNull(controle) == null) ? " " : controle.trim();
    }

    public static Timestamp getValidadeLote(BigDecimal codProd, BigDecimal codEmp, BigDecimal codLocal, String controle) throws Exception {
        EntityFacade dwfFacade = null;
        JdbcWrapper jdbc = null;

        dwfFacade = EntityFacadeFactory.getDWFFacade();
        jdbc = dwfFacade.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        sql.setNamedParameter("CODPROD", codProd.toString());
        sql.setNamedParameter("CODEMP", codEmp.toString());
        sql.setNamedParameter("CODLOCAL", codLocal.toString());
        sql.setNamedParameter("CONTROLE", controle);

        ResultSet rset = sql.executeQuery("SELECT DTVAL FROM TGFEST WHERE ATIVO = 'S' AND TIPO = 'P' AND CODPROD = :CODPROD AND CODLOCAL = :CODLOCAL AND CODEMP = :CODEMP AND CONTROLE = :CONTROLE");

        if (rset.next()) {
            return rset.getTimestamp("DTVAL");
        }

        return null;
    }

    public static void carregaPainelEstoque() throws Exception {
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                    EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                    FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.ESTOQUE > 0 AND this.CODLOCAL IN (202, 203, 204, 205, 207, 208, 211, 304, 305, 307, 308, 311, 313)");
                    finder.setMaxResults(-1);
                    Collection<DynamicVO> estVO = dwfFacade.findByDynamicFinderAsVO(finder);

                    dwfFacade.removeByCriteria(new FinderWrapper("AD_TGFEST", "this.CODEMP <> 0"));

                    for (DynamicVO estoque: estVO) {

                        DynamicVO painelVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_TGFEST");
                        painelVO.setProperty("CODEMP", estoque.asBigDecimalOrZero("CODEMP"));
                        painelVO.setProperty("CODPROD", estoque.asBigDecimalOrZero("CODPROD"));
                        painelVO.setProperty("CODLOCAL", estoque.asBigDecimalOrZero("CODLOCAL"));
                        painelVO.setProperty("CONTROLE", estoque.asString("CONTROLE"));
                        painelVO.setProperty("CODPARC", estoque.asBigDecimalOrZero("CODPARC"));
                        painelVO.setProperty("TIPO", estoque.asString("TIPO"));
                        dwfFacade.createEntity("AD_TGFEST", (EntityVO) painelVO);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JapeSession.close(hnd);

        }

    }


    public static void carregaPainelEstoquev2() throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            //FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.ESTOQUE > 0 AND this.CODLOCAL IN (202, 204, 205, 207, 208, 211, 304, 305, 307, 308, 311, 313)");
            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.ESTOQUE > 0 AND this.CODLOCAL <> 0");
            //FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.CODEMP <> 0 AND this.ESTOQUE > 0 AND this.CODLOCAL IN (202, 204, 205, 207, 208, 211, 304, 305, 307, 308, 311, 313)");
            finder.setMaxResults(-1);
            Collection<DynamicVO> estoquesVO = dwfFacade.findByDynamicFinderAsVO(finder);
            FinderWrapper finder2 = new FinderWrapper("AD_TGFEST", "this.CODEMP <> 0");
            finder.setMaxResults(-1);
            dwfFacade.removeByCriteria(finder2);

            for (DynamicVO estoque : estoquesVO) {
                DynamicVO painelVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("AD_TGFEST");
                painelVO.setProperty("CODEMP", estoque.asBigDecimal("CODEMP"));
                painelVO.setProperty("CODPROD", estoque.asBigDecimal("CODPROD"));
                painelVO.setProperty("CODLOCAL", estoque.asBigDecimal("CODLOCAL"));
                painelVO.setProperty("CONTROLE", estoque.asString("CONTROLE"));
                painelVO.setProperty("CODPARC", estoque.asBigDecimal("CODPARC"));
                painelVO.setProperty("TIPO", estoque.asString("TIPO"));
                dwfFacade.createEntity("AD_TGFEST", (EntityVO) painelVO);
            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void copiaAnexos(DynamicVO estVO, DynamicVO estNovo) throws Exception {

        String codEmp = estNovo.asBigDecimal("CODEMP").toString();
        String codProd = estNovo.asBigDecimal("CODPROD").toString();
        String codLocal = estNovo.asBigDecimal("CODLOCAL").toString();
        String controle = estNovo.asString("CONTROLE");
        String codParc = estNovo.asBigDecimal("CODPARC").toString();
        String tipo = estNovo.asString("TIPO");

        Collection<DynamicVO> anexos = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ANEXO_SISTEMA, "this.NOMEINSTANCIA = 'Estoque' AND this.PKREGISTRO LIKE '_\\_" + estVO.asBigDecimal("CODPROD").toString() + "\\_%\\_" + estVO.asString("CONTROLE") + "\\_0\\_P\\_Estoque' ESCAPE '\\'"));

        //String chaveArquivo = AnexoSistemaHelper.buildChaveArquivo(DynamicEntityNames.ESTOQUE, (EntityPrimaryKey) estNovo.getPrimaryKey());

        String pkRegistro = String.format("%s_%s_%s_%s_%s_%s_Estoque", codEmp, codProd, codLocal, controle, codParc, tipo);

        for (DynamicVO anexoVO : anexos) {
            String instancia = anexoVO.asString("NOMEINSTANCIA");
            String chave = anexoVO.asString("CHAVEARQUIVO");

            DynamicVO anexoEstoqueVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance(DynamicEntityNames.ANEXO_SISTEMA);
            anexoEstoqueVO.setProperty("NOMEINSTANCIA", "Estoque");
            anexoEstoqueVO.setProperty("PKREGISTRO", pkRegistro);
            anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("DESCRICAO", anexoVO.getProperty("DESCRICAO"));
            anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("TIPOAPRES", anexoVO.getProperty("TIPOAPRES"));
            anexoEstoqueVO.setProperty("TIPOACESSO", anexoVO.getProperty("TIPOACESSO"));
            anexoEstoqueVO.setProperty("CODUSU", anexoVO.getProperty("CODUSU"));
            anexoEstoqueVO.setProperty("DHCAD", anexoVO.getProperty("DHCAD"));

            EntityFacadeFactory.getDWFFacade().createEntity(DynamicEntityNames.ANEXO_SISTEMA, (EntityVO) anexoEstoqueVO);
            anexoEstoqueVO.setProperty("CHAVEARQUIVO", gerarMD5ChaveArquivo(anexoEstoqueVO.asBigDecimalOrZero("NUATTACH").toString() + "_" + anexoEstoqueVO.asString("PKREGISTRO")));
            EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.ANEXO_SISTEMA, (EntityVO) anexoEstoqueVO);

            Path sourceDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath() + "/Sistema/Anexos/" + instancia + "/" + chave);
            Path targetDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath() + "/Sistema/Anexos/" + anexoEstoqueVO.asString("NOMEINSTANCIA") + "/" + anexoEstoqueVO.asString("CHAVEARQUIVO"));

            //if (true) throw new MGEModelException("source: "+sourceDirectory + "\ntarget: "+targetDirectory);
            Files.copy(sourceDirectory, targetDirectory);
        }

    }

    private static String gerarMD5ChaveArquivo(String chaveArquivo) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        return StringUtils.toHexString(md5.digest(chaveArquivo.getBytes()));
    }

}
