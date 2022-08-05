package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.inventario.helpers.CustoHelper;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ProdutoVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.hazelcast.spi.impl.merge.CollectionMergingValueImpl;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.confirmaNota;
import static br.com.sankhya.dinaco.vendas.modelo.Produto.getCustoVO;

public class AlteraLote implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        Registro[] linhas = contextoAcao.getLinhas();

        if (linhas.length > 1) contextoAcao.mostraErro("Selecione apenas um registro.");
        String novoControle = (String) contextoAcao.getParam("CONTROLE");
        BigDecimal qtd = BigDecimal.valueOf((Double) contextoAcao.getParam("QTD"));


        for (Registro linha: linhas) {
            BigDecimal codEmp = (BigDecimal) linha.getCampo("CODEMP");
            BigDecimal codProd = (BigDecimal) linha.getCampo("CODPROD");
            BigDecimal codLocal = (BigDecimal) linha.getCampo("CODLOCAL");
            BigDecimal codParc = (BigDecimal) linha.getCampo("CODPARC");
            String controle = (String) linha.getCampo("CONTROLE");
            String tipo = (String) linha.getCampo("TIPO");

            DynamicVO estVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ESTOQUE, new Object[] {codEmp, codProd, codLocal, controle,codParc, tipo});

            final boolean temReserva = estVO.asBigDecimal("RESERVADO").compareTo(BigDecimal.ZERO) > 0;
            final boolean naoEhEstoqueProprio = "T".equals(estVO.asString("TIPO")) || ("P".equals(estVO.asString("TIPO")) && !BigDecimalUtil.isNullOrZero(estVO.asBigDecimal("CODPARC")));
            final boolean qtdMaiorQueEstoque = qtd.compareTo(estVO.asBigDecimal("ESTOQUE")) > 0;

            if (naoEhEstoqueProprio) contextoAcao.mostraErro("Não é possível alterar nome de lote de produto em estoque de terceiros.");
            if (temReserva) contextoAcao.mostraErro("Não é possível alterar nome do lote de produto reservado.");
            if (qtdMaiorQueEstoque) contextoAcao.mostraErro("Quantidade selecionada maior que a disponível.");


            if (contextoAcao.confirmarSimNao("Atenção", String.format("Confirma alteração de nome do lote %s para %s?", controle, novoControle), 1)) {

                ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, codProd, ProdutoVO.class);
                DynamicVO empFinVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA_FINANCEIRO, estVO.asBigDecimal("CODEMP"));

                CabecalhoNotaVO cabSaidaVO = (CabecalhoNotaVO) empFinVO.asDymamicVO("CabecalhoNota_AD001").wrapInterface(CabecalhoNotaVO.class);
                CabecalhoNotaVO cabEntradaVO = (CabecalhoNotaVO) empFinVO.asDymamicVO("CabecalhoNota").wrapInterface(CabecalhoNotaVO.class);
                cabSaidaVO.setNUNOTA(null);
                cabSaidaVO.setDTNEG(TimeUtils.getNow());
                cabSaidaVO.setDTENTSAI(TimeUtils.getNow());
                cabSaidaVO.setDTMOV(TimeUtils.getNow());
                cabSaidaVO.setDTFATUR(TimeUtils.getNow());

                cabEntradaVO.setNUNOTA(null);
                cabEntradaVO.setDTNEG(TimeUtils.getNow());
                cabEntradaVO.setDTENTSAI(TimeUtils.getNow());
                cabEntradaVO.setDTMOV(TimeUtils.getNow());
                cabEntradaVO.setDTFATUR(TimeUtils.getNow());
                //contextoAcao.confirmar("Atenção", estVO.toString() ,1);

                criaNotaDeSaida(dwfFacade, estVO, prodVO, qtd, cabSaidaVO);

                criaNotaDeEntrada(dwfFacade, novoControle, estVO, prodVO, qtd, cabEntradaVO);

                DynamicVO cusVO = getCustoVO(codProd, codEmp, controle);


                if (cusVO != null) {
                    cusVO.setProperty("CONTROLE", novoControle);
                    cusVO.setProperty("DTATUAL", TimeUtils.getNow());
                    dwfFacade.createEntity(DynamicEntityNames.CUSTO, (EntityVO) cusVO);
                }


                criaLigacaoVar(dwfFacade, estVO, qtd, cabSaidaVO, cabEntradaVO);

                contextoAcao.setMensagemRetorno(String.format("Alteração feita com sucesso! Nota de saída: %s | Nota de entrada: %s", cabSaidaVO.getNUNOTA(), cabEntradaVO.getNUNOTA()));

            }
        }

    }

    private BigDecimal criaNotaDeSaida(EntityFacade dwfFacade, DynamicVO estVO, ProdutoVO prodVO, BigDecimal qtd, CabecalhoNotaVO cabSaidaVO) throws Exception {
        //NOTA DE SAIDA
        dwfFacade.createEntity(DynamicEntityNames.CABECALHO_NOTA, cabSaidaVO);
        String usarPrecoCusto = TipoOperacaoUtils.getTopVO(cabSaidaVO.getCODTIPOPER()).asString("USARPRECOCUSTO");
        BigDecimal precoCusto = ComercialUtils.obtemPrecoCusto(usarPrecoCusto, estVO.asString("CONTROLE"),estVO.asBigDecimal("CODEMP"), estVO.asBigDecimal("CODLOCAL"), estVO.asBigDecimal("CODPROD"));

        Collection<ItemNotaVO> itens = new ArrayList<>();
        ItemNotaVO itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA, ItemNotaVO.class);
        itemVO.setNUNOTA(cabSaidaVO.getNUNOTA());
        itemVO.setCODPROD(estVO.asBigDecimal("CODPROD"));
        itemVO.setCODEMP(estVO.asBigDecimal("CODEMP"));
        itemVO.setCODVOL(prodVO.getCODVOL());
        itemVO.setCONTROLE(estVO.asString("CONTROLE"));
        itemVO.setQTDNEG(qtd);
        itemVO.setVLRUNIT(precoCusto);
        itemVO.setCUSTO(precoCusto);
        itemVO.setVLRTOT(precoCusto.multiply(itemVO.getQTDNEG()));
        itemVO.setCODLOCALORIG(estVO.asBigDecimal("CODLOCAL"));
        itemVO.setATUALESTOQUE(BigDecimal.ZERO);
        itemVO.setRESERVA("N");
        itens.add(itemVO);
        ItemNotaHelpper.saveItensNota(itens, cabSaidaVO);

        // Recalculo de impostos
        final ImpostosHelpper impostos = new ImpostosHelpper();
        impostos.calcularImpostos(cabSaidaVO.getNUNOTA());
        impostos.totalizarNota(cabSaidaVO.getNUNOTA());
/*
        // Refaz financeiro
        final CentralFinanceiro centralFinanceiro = new CentralFinanceiro();
        centralFinanceiro.inicializaNota(cabSaidaVO.getNUNOTA());
        centralFinanceiro.refazerFinanceiro();*/

        confirmaNota(cabSaidaVO.getNUNOTA());

        return cabSaidaVO.getNUNOTA();
    }

    private BigDecimal criaNotaDeEntrada(EntityFacade dwfFacade, String novoControle, DynamicVO estVO, ProdutoVO prodVO, BigDecimal qtd, CabecalhoNotaVO cabEntradaVO) throws Exception {
        ItemNotaVO itemVO;
        Collection<ItemNotaVO> itens;
        //NOTA DE ENTRADA
        dwfFacade.createEntity(DynamicEntityNames.CABECALHO_NOTA, cabEntradaVO);
        String usarPrecoCusto = TipoOperacaoUtils.getTopVO(cabEntradaVO.getCODTIPOPER()).asString("USARPRECOCUSTO");
        BigDecimal precoCusto = ComercialUtils.obtemPrecoCusto(usarPrecoCusto, estVO.asString("CONTROLE"),estVO.asBigDecimal("CODEMP"), estVO.asBigDecimal("CODLOCAL"), estVO.asBigDecimal("CODPROD"));

        itens = new ArrayList<>();
        itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA, ItemNotaVO.class);
        itemVO.setNUNOTA(cabEntradaVO.getNUNOTA());
        itemVO.setCODPROD(estVO.asBigDecimal("CODPROD"));
        itemVO.setCODEMP(estVO.asBigDecimal("CODEMP"));
        itemVO.setCODVOL(prodVO.getCODVOL());
        itemVO.setCONTROLE(novoControle);
        itemVO.setQTDNEG(qtd);
        itemVO.setVLRUNIT(precoCusto);
        itemVO.setVLRTOT(precoCusto.multiply(itemVO.getQTDNEG()));
        itemVO.setCODLOCALORIG(estVO.asBigDecimal("CODLOCAL"));
        itemVO.setATUALESTOQUE(BigDecimal.ONE);
        itemVO.setRESERVA("N");
        itens.add(itemVO);
        ItemNotaHelpper.saveItensNota(itens, cabEntradaVO);

        DynamicVO estNovoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ESTOQUE, new Object[] {estVO.asBigDecimal("CODEMP"), estVO.asBigDecimal("CODPROD"), estVO.asBigDecimal("CODLOCAL"), novoControle, estVO.asBigDecimal("CODPARC"), estVO.asString("TIPO")});
        estNovoVO.setProperty("DTFABRICACAO", estVO.asTimestamp("DTFABRICACAO"));
        estNovoVO.setProperty("DTVAL", estVO.asTimestamp("DTVAL"));
        dwfFacade.saveEntity(DynamicEntityNames.ESTOQUE, (EntityVO) estNovoVO);

        PrecoCustoHelper.configuraProcessoAtualizacaoCusto();


        // Recalculo de impostos
        final ImpostosHelpper impostos = new ImpostosHelpper();
        impostos.calcularImpostos(cabEntradaVO.getNUNOTA());
        impostos.totalizarNota(cabEntradaVO.getNUNOTA());

        /*// Refaz financeiro
        final CentralFinanceiro centralFinanceiro = new CentralFinanceiro();
        centralFinanceiro.inicializaNota(cabEntradaVO.getNUNOTA());
        centralFinanceiro.refazerFinanceiro();*/

        confirmaNota(cabEntradaVO.getNUNOTA());

        return cabEntradaVO.getNUNOTA();
    }

    private void criaLigacaoVar(EntityFacade dwfFacade, DynamicVO estVO, BigDecimal qtd, CabecalhoNotaVO cabSaidaVO, CabecalhoNotaVO cabEntradaVO) throws Exception {
        DynamicVO varVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO);
        varVO.setProperty("NUNOTA", cabEntradaVO.getNUNOTA());
        varVO.setProperty("NUNOTAORIG", cabSaidaVO.getNUNOTA());
        varVO.setProperty("SEQUENCIA", BigDecimal.ONE);
        varVO.setProperty("SEQUENCIAORIG", BigDecimal.ONE);
        varVO.setProperty("QTDATENDIDA", qtd);
        dwfFacade.createEntity(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, (EntityVO) varVO);
    }

}
