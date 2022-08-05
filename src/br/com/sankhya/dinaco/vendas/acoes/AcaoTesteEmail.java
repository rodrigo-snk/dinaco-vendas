package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
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
import java.util.Optional;

public class AcaoTesteEmail implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        Registro[] linhas = contextoAcao.getLinhas();

        for(Registro linha: linhas) {

            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            JapeSession.SessionHandle hnd = null;

            try {
                hnd = JapeSession.open();

                if (!BigDecimalUtil.isNullOrZero(nuNota)) {
                    EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                    DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
                    BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
                    BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");

                    //final boolean naoFoiEnviadaNFe = StringUtils.getNullAsEmpty(filaVO.asString("ASSUNTO")).contains("Envio da NF-e") && CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.NUCHAVE = ? AND ASSUNTO LIKE '%Envio da NF-e%'", nuNota)));

                    DynamicVO empVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, codEmp);
                    String email = empVO.asString("AD_EMAILRESPTECNICO");

                    if (CabecalhoNota.temProdutoPerigoso(nuNota) && RegraNegocio.verificaRegra(BigDecimal.valueOf(13), codTipOper) && !StringUtils.isEmpty(email)) {
               /* DynamicVO filaNovaVO = filaVO.buildClone();
                filaNovaVO.setProperty("CODFILA", null);
                filaNovaVO.setProperty("EMAIL", email);
                dwfFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) filaNovaVO);*/
                    contextoAcao.setMensagemRetorno("FUNFOU");
                    }
                }


            } finally {
                JapeSession.close(hnd);
            }

        }









    }
}
