package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dwf.financeiro.utils.ConciliacaoBancariaGridPrinter;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.DynamicVOPojo;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.mgecomercial.model.facades.helpper.FinanceiroHelpper;
import br.com.sankhya.mgecomercial.model.utils.CheckoutUtils;
import br.com.sankhya.mgefin.model.services.ConciliacaoBancariaSP;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.cheque.ConciliacaoCheque;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ParceiroVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.ContaBancariaVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiraVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.FinanceiroVO;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.MovimentoBancarioVO;
import br.com.sankhya.modelcore.financeiro.helper.BaixaHelper;
import br.com.sankhya.modelcore.financeiro.util.DadosBaixa;
import br.com.sankhya.modelcore.financeiro.util.FinanceiroUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.FinanceiroHelper;
import br.com.sankhya.pes.model.integracao.parceiros.EParceiro;
import br.com.sankhya.pes.model.integracao.parceiros.Parceiro;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public class LancaReceitaOFX implements AcaoRotinaJava {

    BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();
        //final boolean baixar = "S".equals(contextoAcao.getParam("BAIXA"));

        for (Registro linha: linhas) {
/*           Timestamp dtLanc = (Timestamp) linha.getCampo("DTLANC");
            BigDecimal valor = (BigDecimal) linha.getCampo("VALOR");
            BigDecimal codBco = (BigDecimal) linha.getCampo("CODBCO");
            BigDecimal nuBco = (BigDecimal) linha.getCampo("NUBCO");
            BigDecimal nroCta = (BigDecimal) linha.getCampo("NROCTA");
            BigDecimal recDesp = (BigDecimal) linha.getCampo("RECDESP");
            String conciliado = (String) linha.getCampo("CONCILIADO");
            String cpfCnpj = (String) linha.getCampo("CPF_CNPJ");*/

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            BigDecimal nuExb = (BigDecimal) linha.getCampo("NUEXB");
            DynamicVO extratoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EXTRATO_BANCARIO, nuExb);
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(BigDecimal.valueOf(1300)); // TOP LANÇAMENTO FINANCEIRO
            ContaBancariaVO contaVO = null;

            Optional contaBancaria
                    = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.CONTA_BANCARIA,
                            "this.CODBCO = ? AND this.CODCTABCO = ?",
                            new Object[]{extratoVO.getProperty("CODBCO"), extratoVO.getProperty("NROCTA")}), ContaBancariaVO.class)
                    .stream()
                    .findFirst();

            if (contaBancaria.isPresent()) {
                JdbcWrapper jdbc = dwfFacade.getJdbcWrapper();
                jdbc.openSession();
                contaVO = (ContaBancariaVO) contaBancaria.get();

                FinanceiroVO finVO = (FinanceiroVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.FINANCEIRO, FinanceiroVO.class);

                Collection<ParceiroVO> parcVO = (Collection<ParceiroVO>) dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.PARCEIRO, "this.CODCTABCOINT = ?", contaVO.getCODCTABCOINT()), ParceiroVO.class);

                final boolean existeParceiro = parcVO.stream().findAny().isPresent();

                if (existeParceiro) {
                    ParceiroVO parceiroVO = parcVO.stream().findFirst().get();
                    finVO.setCODPARC(parceiroVO.getCODPARC());
                    finVO.setCODNAT(parceiroVO.asBigDecimalOrZero("AD_CODNAT"));
                    finVO.setCODCENCUS(parceiroVO.asBigDecimalOrZero("AD_CODCENCUS"));
                } else {
                    contextoAcao.mostraErro("Parceiro não encontrado.");
                }

                // Faz o lançamento da receita no Financeiro
                finVO.setCODEMP(contaVO.getCODEMP());
                finVO.setDTVENC(extratoVO.asTimestamp("DTLANC"));
                finVO.setCODCTABCOINT(contaVO.getCODCTABCOINT());
                finVO.setDTNEG(extratoVO.asTimestamp("DTLANC"));
                finVO.setCODTIPOPER(topVO.asBigDecimalOrZero("CODTIPOPER"));
                finVO.setHISTORICO(extratoVO.asString("HIST"));
                finVO.setVLRDESDOB(extratoVO.asBigDecimalOrZero("VALOR"));
                finVO.setRECDESP(extratoVO.asBigDecimalOrZero("RECDESP"));
                finVO.setORIGEM("F");
                finVO.setDHMOV(extratoVO.asTimestamp("DTLANC"));
                finVO.setNUMNOTA(BigDecimal.ZERO);
                dwfFacade.createEntity(DynamicEntityNames.FINANCEIRO, finVO);

                // Baixa o título no Financeiro
                BaixaHelper baixaHelper = new BaixaHelper(finVO.asBigDecimal("NUFIN"), codUsuarioLogado);
                DadosBaixa dadosBaixa = baixaHelper.montaDadosBaixa(Timestamp.valueOf(LocalDateTime.now()), false);
                baixaHelper.baixar(dadosBaixa);

                // Concilia o lançamento bancário
                MovimentoBancarioVO movimentoBancarioVO = (MovimentoBancarioVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.MOVIMENTO_BANCARIO, finVO.getNUBCO(), MovimentoBancarioVO.class);
                movimentoBancarioVO.setCONCILIADO("S");
                movimentoBancarioVO.setDHCONCILIACAO(TimeUtils.getNow());
                dwfFacade.saveEntity(DynamicEntityNames.MOVIMENTO_BANCARIO, movimentoBancarioVO);

                // Muda o extrato para conciliado e salva o registro.
                extratoVO.setProperty("CONCILIADO", "S");
                extratoVO.setProperty("NUBCO", finVO.getNUBCO());
                dwfFacade.saveEntity(DynamicEntityNames.EXTRATO_BANCARIO, (EntityVO) extratoVO);

                jdbc.closeSession();

            } else {
                contextoAcao.mostraErro("Conta bancária inválida.");
            }
        }

        contextoAcao.setMensagemRetorno("Foram conciliados: " +linhas.length);
    }
}
