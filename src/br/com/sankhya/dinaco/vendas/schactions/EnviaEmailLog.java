package br.com.sankhya.dinaco.vendas.schactions;

import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.getCotacaoDiaAnterior;

public class EnviaEmailLog implements ScheduledAction {

    public static void insertFilaEmail(String assunto, String email, char[] msgEmail) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO emailVO = (DynamicVO)dwfFacade.getDefaultValueObjectInstance("MSDFilaMensagem");
        emailVO.setProperty("ASSUNTO", assunto);
        emailVO.setProperty("EMAIL", email);
        emailVO.setProperty("MENSAGEM", msgEmail);
        emailVO.setProperty("CODCON", BigDecimal.ZERO);
        emailVO.setProperty("STATUS", "Pendente");
        emailVO.setProperty("MAXTENTENVIO", BigDecimal.valueOf(3L));
        emailVO.setProperty("TIPOENVIO", "E");
        dwfFacade.createEntity("MSDFilaMensagem", (EntityVO)emailVO);
    }

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        try {
            Collection<DynamicVO> mensagensVO = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.DTENTRADA = SYSDATE AND NUCHAVE IS NOT NULL"));

            for (DynamicVO filaVO: mensagensVO) {

                BigDecimal nuNota = filaVO.asBigDecimalOrZero("NUCHAVE");

                if (!BigDecimalUtil.isNullOrZero(nuNota)) {
                    DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
                    BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
                    BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");
                    String email = Empresa.getEmailLOG(codEmp);

                    DynamicVO empVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, cabVO.asBigDecimalOrZero("CODEMP"));
                    final boolean enviaEmailParaLogistica = DataDictionaryUtils.campoExisteEmTabela("AD_ENVIANFELOG", "TSIEMP") && "S".equals(empVO.asString("AD_ENVIANFELOG"));

                    if (enviaEmailParaLogistica && RegraNegocio.verificaRegra(BigDecimal.valueOf(12), codTipOper) && !StringUtils.isEmpty(email)) {
                        //filaVO.setProperty("EMAIL", filaVO.asString("EMAIL").concat(email));
                        insertFilaEmail(filaVO.asString("ASSUNTO"), email, filaVO.asClob("MENSAGEM"));
                    }
                }

            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
