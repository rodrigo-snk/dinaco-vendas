package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

public class EmailAnaliseAdmCom implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        for(Registro linha: linhas) {

            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");
            String executivo = (String) linha.getCampo("EXECUTIVO");
            String referencia = (String) linha.getCampo("REFERENCIA");
            String descricaoProd = (String) linha.getCampo("DESCRPROD");
            String motivo = (String) linha.getCampo("OBS");
            BigDecimal qtdNeg = (BigDecimal) linha.getCampo("QTDNEG");
            BigDecimal precoUSD = (BigDecimal) linha.getCampo("VLRUNITMOE");
            BigDecimal precoBRL = (BigDecimal) linha.getCampo("VLRUNIT");
            Timestamp dtUltimoPreco = (Timestamp) linha.getCampo("DTULTPRECO");
            BigDecimal ultPrecoUSD = (BigDecimal) linha.getCampo("ULTPRECOVENDA");
            BigDecimal icms = (BigDecimal) linha.getCampo("ICMS");
            BigDecimal margemMinima = (BigDecimal) linha.getCampo("AD_MARGEMINIMA");
            BigDecimal reducaoUltPreco = (BigDecimal) linha.getCampo("REDULTVLR");
            BigDecimal margemLC = (BigDecimal) linha.getCampo("MARGEMLC");


            //contextoAcao.setMensagemRetorno(nuNota.toString().concat(executivo).concat(referencia).concat(descricaoProd).concat(motivo).concat(qtdNeg.toString()).concat(precoUSD.toString()).concat(precoBRL.toPlainString()).concat(dtUltimoPreco.toString()).concat(reducaoUltPreco.toPlainString()));

            contextoAcao.setMensagemRetorno(nuNota.toPlainString() + executivo + descricaoProd + referencia + motivo);



        }
    }
}
