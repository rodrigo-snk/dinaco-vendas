package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.ContaBancariaVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiraVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiroVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.MovimentoBancarioVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.Optional;

public class LancaReceitaOFX implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();


        for (Registro linha: linhas) {
/*            Timestamp dtLanc = (Timestamp) linha.getCampo("DTLANC");
            BigDecimal valor = (BigDecimal) linha.getCampo("VALOR");
            BigDecimal codBco = (BigDecimal) linha.getCampo("CODBCO");
            BigDecimal nuBco = (BigDecimal) linha.getCampo("NUBCO");
            BigDecimal nroCta = (BigDecimal) linha.getCampo("NROCTA");
            BigDecimal recDesp = (BigDecimal) linha.getCampo("RECDESP");
            String conciliado = (String) linha.getCampo("CONCILIADO");
            String cpfCnpj = (String) linha.getCampo("CPF_CNPJ");*/

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            BigDecimal nuExb = (BigDecimal) linha.getCampo("NUEXB");
            DynamicVO extratoBancarioVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EXTRATO_BANCARIO, nuExb);
            MovimentoBancarioVO movimentoBancarioVO = (MovimentoBancarioVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.MOVIMENTO_BANCARIO, MovimentoBancarioVO.class);
            DynamicVO movimentoBancarioVO2 = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.MOVIMENTO_BANCARIO);
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(BigDecimal.valueOf(1300)); // TOP LANÇAMENTO FINANCEIRO
            ContaBancariaVO contaBancariaVO = null;

            Optional contaBancaria
                    = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.CONTA_BANCARIA,
                            "this.CODBCO = ? AND this.CODCTABCO = ?",
                            new Object[]{extratoBancarioVO.getProperty("CODBCO"), extratoBancarioVO.getProperty("NROCTA")}), ContaBancariaVO.class)
                    .stream()
                    .findFirst();

            if (contaBancaria.isPresent()) {
                JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
                jdbc.openSession();
                contaBancariaVO = (ContaBancariaVO) contaBancaria.get();

                FinanceiroVO finVO = (FinanceiroVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.FINANCEIRO, FinanceiroVO.class);

                finVO.setRECDESP(extratoBancarioVO.asBigDecimalOrZero("RECDESP"));
                finVO.setDTNEG(extratoBancarioVO.asTimestamp("DTLANC"));
                finVO.setDTVENC(extratoBancarioVO.asTimestamp("DTLANC"));
                finVO.setCODTIPOPER(topVO.asBigDecimalOrZero("CODTIPOPER"));
                finVO.setHISTORICO(extratoBancarioVO.asString("HIST"));
                finVO.setCODCTABCOINT(contaBancariaVO.getCODCTABCOINT());
                finVO.setVLRDESDOB(extratoBancarioVO.asBigDecimalOrZero("VALOR"));
                finVO.setORIGEM("F");

                dwfFacade.createEntity(DynamicEntityNames.FINANCEIRO, finVO);

                // Muda o extrato para conciliado e salva o registro.
                extratoBancarioVO.setProperty("CONCILIADO", "S");
                extratoBancarioVO.setProperty("NUBCO", movimentoBancarioVO2.getProperty("NUBCO"));
                dwfFacade.saveEntity(DynamicEntityNames.EXTRATO_BANCARIO, (EntityVO) extratoBancarioVO);

                jdbc.closeSession();

            } else {
                contextoAcao.mostraErro("Conta bancária inválida.");
            }
        }

        contextoAcao.setMensagemRetorno("Foram conciliados: " +linhas.length);
    }
}
