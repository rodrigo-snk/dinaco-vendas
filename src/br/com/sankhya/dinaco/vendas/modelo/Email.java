package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;

import java.math.BigDecimal;

public class Email {

    public static void insertFilaEmail(EntityFacade dwf, String assunto, String email, char[] msgEmail) throws Exception {
        DynamicVO emailVO = (DynamicVO)dwf.getDefaultValueObjectInstance("MSDFilaMensagem");
        emailVO.setProperty("ASSUNTO", assunto);
        emailVO.setProperty("EMAIL", email);
        emailVO.setProperty("MENSAGEM", msgEmail);
        emailVO.setProperty("CODCON", BigDecimal.ZERO);
        emailVO.setProperty("STATUS", "Pendente");
        emailVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3L));
        emailVO.setProperty("TIPOENVIO", "E");
        dwf.createEntity("MSDFilaMensagem", (EntityVO)emailVO);
    }

}
