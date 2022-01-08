package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class AdicionaReceitasOFX implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        for (Registro linha: linhas) {
            Timestamp dtLanc = (Timestamp) linha.getCampo("DTLANC");
            BigDecimal valor = (BigDecimal) linha.getCampo("VALOR");
            BigDecimal codBco = (BigDecimal) linha.getCampo("CODBCO");
            BigDecimal nuBco = (BigDecimal) linha.getCampo("NUBCO");
            BigDecimal nroCta = (BigDecimal) linha.getCampo("NROCTA");
            BigDecimal recDesp = (BigDecimal) linha.getCampo("RECDESP");
            String conciliado = (String) linha.getCampo("CONCILIADO");
            String cpfCnpj = (String) linha.getCampo("CPF_CNPJ");




        }
    }
}
