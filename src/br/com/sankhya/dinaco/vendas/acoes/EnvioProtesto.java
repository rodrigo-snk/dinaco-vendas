package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiroVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EnvioProtesto implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {

        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        Registro[] linhas = contextoAcao.getLinhas();
        Timestamp dtEnvio = (Timestamp) contextoAcao.getParam("DTENVIO");
        String tipo = (String) contextoAcao.getParam("TIPO");
        String status = (String) contextoAcao.getParam("STATUS");


        for (Registro linha: linhas) {
            BigDecimal nuFin = (BigDecimal) linha.getCampo("NUFIN");
            boolean confirmaAlteracao = true;

            FinanceiroVO finVO = (FinanceiroVO) dwf.findEntityByPrimaryKeyAsVO(DynamicEntityNames.FINANCEIRO, nuFin, FinanceiroVO.class);

            // Cartório
            if ("C".equals(tipo)) {

                if (DataDictionaryUtils.campoExisteEmTabela("AD_CARTORIO", "TGFFIN")) {
                    if ("S".equals(StringUtils.getNullAsEmpty(finVO.asString("AD_CARTORIO")))) {
                        confirmaAlteracao = contextoAcao.confirmarSimNao("Atenção", String.format("Título (Nro. Único %s ) já foi enviado para o cartório em %s. Deseja alterar a data?", finVO.getNUFIN(), TimeUtils.formataDDMMYYYY(finVO.asTimestamp("AD_DTCARTORIO"))),1);
                    }
                    finVO.setProperty("AD_CARTORIO", "S");

                    if (DataDictionaryUtils.campoExisteEmTabela("AD_DTCARTORIO", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_DTCARTORIO", dtEnvio);
                    if (DataDictionaryUtils.campoExisteEmTabela("AD_STATUSCOBRANCA", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_STATUSCOBRANCA", status);
                }

            }

            // Serasa
            if ("S".equals(tipo)) {

                if (DataDictionaryUtils.campoExisteEmTabela("AD_SERASA", "TGFFIN")) {
                    if ("S".equals(StringUtils.getNullAsEmpty(finVO.asString("AD_SERASA")))) {
                        confirmaAlteracao = contextoAcao.confirmarSimNao("Atenção", String.format("Título (Nro. Único %s ) já foi enviado para o Serasa em %s. Deseja alterar a data?", finVO.getNUFIN(), TimeUtils.formataDDMMYYYY(finVO.asTimestamp("AD_DTSERASA"))),1);
                    }
                    finVO.setProperty("AD_SERASA", "S");

                    if (DataDictionaryUtils.campoExisteEmTabela("AD_DTSERASA", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_DTSERASA", dtEnvio);
                    if (DataDictionaryUtils.campoExisteEmTabela("AD_STATUSCOBRANCA", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_STATUSCOBRANCA", status);
                }

            }
            // Escritório de Cobrança
            if ("E".equals(tipo)) {

                if (DataDictionaryUtils.campoExisteEmTabela("AD_COBRANCA", "TGFFIN")) {
                    if ("S".equals(StringUtils.getNullAsEmpty(finVO.asString("AD_COBRANCA")))) {
                        confirmaAlteracao = contextoAcao.confirmarSimNao("Atenção", String.format("Título (Nro. Único %s ) já foi enviado para o escritório de cobrança em %s. Deseja alterar a data?", finVO.getNUFIN(), TimeUtils.formataDDMMYYYY(finVO.asTimestamp("AD_DTCOBRANCA"))),1);
                    }
                    finVO.setProperty("AD_COBRANCA", "S");

                    if (DataDictionaryUtils.campoExisteEmTabela("AD_DTCOBRANCA", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_DTCOBRANCA", dtEnvio);
                    if (DataDictionaryUtils.campoExisteEmTabela("AD_STATUSCOBRANCA", "TGFFIN") && confirmaAlteracao) finVO.setProperty("AD_STATUSCOBRANCA", status);
                }

            }

            dwf.saveEntity(DynamicEntityNames.FINANCEIRO,  finVO);

        }
    }
}
