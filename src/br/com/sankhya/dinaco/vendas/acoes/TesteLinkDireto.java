package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;

import java.math.BigDecimal;
import java.util.Base64;

public class TesteLinkDireto implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        for (Registro linha : linhas) {

            BigDecimal idSativ = (BigDecimal) linha.getCampo("IDSATIV");

            String originalInput = "br.com.sankhya.menu.adicional.AD_SATIV";
            String encodedString = Base64.getEncoder().encodeToString(originalInput.getBytes());

            String sativ = "{\"IDSATIV\":" + idSativ + "}";
            String sativEncoded = Base64.getEncoder().encodeToString(sativ.getBytes());


            contextoAcao.setMensagemRetorno(String.format("<center> SATIV %s.<br>", idSativ) + "<a target=\"_top\" href=\"http://dinaco.snk.ativy.com:40062/mge/system.jsp#app/" + encodedString + "/" + sativEncoded + "\">Clique aqui para abrir.</a></center>");



        }



        }
}
