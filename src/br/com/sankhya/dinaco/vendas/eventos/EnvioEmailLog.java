package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;

public class EnvioEmailLog implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            DynamicVO filaVO = (DynamicVO) persistenceEvent.getVo();
            //BigDecimal codFila = filaVO.asBigDecimalOrZero("CODFILA");
            BigDecimal nuNota = filaVO.asBigDecimalOrZero("NUCHAVE");

            if (!BigDecimalUtil.isNullOrZero(nuNota)) {
                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
                BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
                BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");
                String email = Empresa.getEmailLOG(codEmp);

                final boolean naoFoiEnviadaNFe = StringUtils.getNullAsEmpty(filaVO.asString("ASSUNTO")).contains("Envio da NF-e") && CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.NUCHAVE = ? AND ASSUNTO LIKE '%Envio da NF-e%'", nuNota)));
                final boolean naoFoiEnviadaCancelada = StringUtils.getNullAsEmpty(filaVO.asString("ASSUNTO")).contains("NF-e CANCELADA") && CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.NUCHAVE = ? AND ASSUNTO LIKE '%NF-e CANCELADA%'", nuNota)));


                DynamicVO empVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, cabVO.asBigDecimalOrZero("CODEMP"));
                final boolean enviaEmailParaLogistica = DataDictionaryUtils.campoExisteEmTabela("AD_ENVIANFELOG", "TSIEMP") && "S".equals(empVO.asString("AD_ENVIANFELOG"));

                if ((naoFoiEnviadaNFe || naoFoiEnviadaCancelada) && enviaEmailParaLogistica && RegraNegocio.verificaRegra(BigDecimal.valueOf(12), codTipOper) && !StringUtils.isEmpty(email)) {
                    //copiaEmail(filaVO,email);
                  /* DynamicVO destVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("MSDDestFilaMensagem");
                    destVO.setProperty("SEQUENCIA", BigDecimal.ONE);
                    destVO.setProperty("CODFILA", codFila);
                    destVO.setProperty("EMAIL", email);
                    dwfFacade.createEntity("MSDDestFilaMensagem", (EntityVO) destVO);*/

                    filaVO.setProperty("EMAIL", filaVO.asString("EMAIL").concat(",").concat(email));
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
