package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.Email;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Arrays;

public class EmailAnaliseAdmCom implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO usuarioVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.USUARIO, 49); // DAIANA.MACEDO


        for(Registro linha: linhas) {

            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");
            String razaoSocial = (String) linha.getCampo("RAZAOSOCIAL");
            String executivo = (String) linha.getCampo("APELIDO");
            String referencia = (String) linha.getCampo("REFERENCIA");
            String descricaoProd = (String) linha.getCampo("DESCRPROD");
            String motivo = (String) linha.getCampo("AD_OBSULTVLRMOE");
            BigDecimal qtdNeg = (BigDecimal) linha.getCampo("QTDNEG");
            BigDecimal precoUSD = (BigDecimal) linha.getCampo("VLRUNITMOE");
            BigDecimal precoBRL = (BigDecimal) linha.getCampo("VLRUNIT");
            Timestamp dtUltimoPreco = (Timestamp) linha.getCampo("DATAULTPRECO");
            BigDecimal ultPrecoUSD = (BigDecimal) linha.getCampo("ULTPRECOVENDA");
            BigDecimal icms = (BigDecimal) linha.getCampo("ALIQICMS");
            BigDecimal margemMinima = (BigDecimal) linha.getCampo("AD_MARGEMINIMA");
            BigDecimal reducaoUltPreco = (BigDecimal) linha.getCampo("REDULTVLR");
            BigDecimal margemLC = (BigDecimal) linha.getCampo("MARGEMLC");
            BigDecimal lc = (BigDecimal) linha.getCampo("LC");


            String mensagem = String.format("<html><b>Razão Social:</b> %s<br><b>Carteira:</b> %s<br><b>Produto: </b>%s - %s<br><b>Quantidade:</b> %.2f<br><b>Vlr. Unit. Moeda: </b>%.2f<br><b>ICMS:</b> %.2f %%<br><b>Dt. Últ. Preço: </b>%s<br><b>Ult. Vlr. Moeda: </b>%.2f<br><b>Red. Ult. Preço: </b>%.2f %%<br><b>Margem Mín.: </b>%.2f %%<br><b>Margem LC: </b>%.2f %%<br><b>Observação: </b> %s</html>", razaoSocial,executivo,referencia,descricaoProd,qtdNeg,precoUSD,icms, TimeUtils.formataDDMMYYYY(dtUltimoPreco),ultPrecoUSD, reducaoUltPreco, margemMinima,margemLC, motivo);
            final boolean envia = contextoAcao.confirmarSimNao(String.format("Confirma envio do email para %s?",usuarioVO.asString("EMAIL")), mensagem, 1);

            if (envia) {
                char[] msgEmail = mensagem.toCharArray();
                Email.insertFilaEmail(dwfFacade, String.format("Liberação de margem (Nro. Único: %s)", nuNota), usuarioVO.asString("EMAIL"), msgEmail);
            }

        }
    }
}
