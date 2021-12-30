package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.dwfdata.vo.EstoqueVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

public class Estoque {

    public static Timestamp getValidadeMinima(BigDecimal codProd, BigDecimal codEmp) throws Exception {
        EntityFacade dwfFacade = null;
        JdbcWrapper jdbc = null;

        dwfFacade = EntityFacadeFactory.getDWFFacade();
        jdbc = dwfFacade.getJdbcWrapper();
        NativeSql sql = new NativeSql(jdbc);
        sql.setNamedParameter("CODPROD", codProd.toString());
        sql.setNamedParameter("CODEMP", codEmp.toString());
        //sql.setNamedParameter("CODLOCAL", codLocal.toString());
        //sql.setNamedParameter("CONTROLE", controle);

        ResultSet rset = sql.executeQuery("SELECT MIN(DTVAL) DTVAL FROM TGFEST WHERE ATIVO = 'S' AND TIPO = 'P' AND CODPROD = :CODPROD  AND CODEMP = :CODEMP");
        EstoqueVO estoqueVO = (EstoqueVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance(DynamicEntityNames.ESTOQUE, EstoqueVO.class);


        if (rset.next()) {
            return rset.getTimestamp("DTVAL");
        }

        return null;
    }

    public static Timestamp getValidade(BigDecimal codProd, BigDecimal codEmp, BigDecimal codLocal, String controle) throws Exception {
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

}
