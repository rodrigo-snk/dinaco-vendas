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
import br.com.sankhya.mgecomercial.model.facades.helpper.FinanceiroHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.ContaBancariaVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.MovimentoBancarioVO;
import br.com.sankhya.modelcore.financeiro.util.FinanceiroUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.FinanceiroHelper;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

public class ConciliaReceitaOFX implements AcaoRotinaJava {
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
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(BigDecimal.valueOf(1600)); // TOP MOVIMENTO BANCÁRIO
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

                JapeWrapper movimento = JapeFactory.dao(DynamicEntityNames.MOVIMENTO_BANCARIO);


                movimento.create()
                        .set("DTLANC", extratoBancarioVO.asTimestamp("DTLANC"))
                        .set("CODTIPOPER", topVO.asBigDecimalOrZero("CODTIPOPER"))
                        .set("HISTORICO", extratoBancarioVO.asString("HIST"))
                        .set("CODCTABCOINT", contaBancariaVO.getCODCTABCOINT())
                        .set("VLRLANC", extratoBancarioVO.asBigDecimalOrZero("VALOR"))
                        .set("CODLANC",BigDecimal.valueOf(16))
                        .set("RECDESP", extratoBancarioVO.asBigDecimalOrZero("RECDESP"))
                        .set("ORIGMOV", "A")
                        .set("CONCILIADO", "S")
                        .set("DHCONCILIACAO", TimeUtils.getNow())
                        .set("VLRMOEDA", BigDecimal.ZERO)
                        .save();

                movimentoBancarioVO.setDTLANC(extratoBancarioVO.asTimestamp("DTLANC"));
                movimentoBancarioVO.setCODTIPOPER(topVO.asBigDecimalOrZero("CODTIPOPER"));
                movimentoBancarioVO.setHISTORICO(extratoBancarioVO.asString("HIST"));
                movimentoBancarioVO.setCODCTABCOINT(contaBancariaVO.getCODCTABCOINT());
                //movimentoBancarioVO.setNUMDOC(extratoBancarioVO.asBigDecimalOrZero("NRODOC"));
                movimentoBancarioVO.setCODLANC(BigDecimal.valueOf(16));
                movimentoBancarioVO.setVLRLANC(extratoBancarioVO.asBigDecimalOrZero("VALOR"));
                movimentoBancarioVO.setRECDESP(extratoBancarioVO.asBigDecimalOrZero("RECDESP"));
                movimentoBancarioVO.setORIGMOV("A");
                //movimentoBancarioVO.setDHCONCILIACAO(TimeUtils.getNow());
                movimentoBancarioVO.setCONCILIADO("N");
                movimentoBancarioVO.setVLRMOEDA(BigDecimal.ZERO);

                movimentoBancarioVO2.setProperty("DTLANC", extratoBancarioVO.asTimestamp("DTLANC"));
                movimentoBancarioVO2.setProperty("CODTIPOPER", topVO.asBigDecimalOrZero("CODTIPOPER"));
                movimentoBancarioVO2.setProperty("HISTORICO", extratoBancarioVO.asString("HIST"));
                movimentoBancarioVO2.setProperty("CODCTABCOINT", contaBancariaVO.getCODCTABCOINT());
                movimentoBancarioVO2.setProperty("CODCTABCOCONTRA", BigDecimal.valueOf(3));
                //movimentoBancarioVO2.setProperty("NUMDOC", extratoBancarioVO.asBigDecimalOrZero("NRODOC"));
                movimentoBancarioVO2.setProperty("VLRLANC", extratoBancarioVO.asBigDecimalOrZero("VALOR"));
                movimentoBancarioVO2.setProperty("CODLANC",BigDecimal.valueOf(16));
                movimentoBancarioVO2.setProperty("RECDESP", extratoBancarioVO.asBigDecimalOrZero("RECDESP"));
                movimentoBancarioVO2.setProperty("ORIGMOV", "A"); // Aplicação
                movimentoBancarioVO2.setProperty("DHCONCILIACAO", TimeUtils.getNow());
                movimentoBancarioVO2.setProperty("CONCILIADO", "S"); // Sim
                movimentoBancarioVO2.setProperty("VLRMOEDA", BigDecimal.ZERO); // Sim

                dwfFacade.createEntity(DynamicEntityNames.MOVIMENTO_BANCARIO, (EntityVO) movimentoBancarioVO2);

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
