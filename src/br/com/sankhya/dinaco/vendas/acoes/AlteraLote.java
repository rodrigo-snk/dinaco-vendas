package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.bmp.PersistentLocalEntity;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ProdutoVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import javax.mail.FetchProfile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.confirmaNota;

public class AlteraLote implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        Registro[] linhas = contextoAcao.getLinhas();

        if (linhas.length > 1) contextoAcao.mostraErro("Selecione apenas um registro.");
        String novoControle = (String) contextoAcao.getParam("CONTROLE");

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

            if (naoEhEstoqueProprio) contextoAcao.mostraErro("N??o ?? poss??vel alterar nome de lote de produto em estoque de terceiros.");
            if (temReserva) contextoAcao.mostraErro("N??o ?? poss??vel alterar nome do lote de produto reservado.");

            if (contextoAcao.confirmarSimNao("Aten????o", String.format("Confirma altera????o de nome do lote %s para %s?", controle, novoControle), 1)) {

                ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, codProd, ProdutoVO.class);
                DynamicVO empFinVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA_FINANCEIRO, estVO.asBigDecimal("CODEMP"));

                CabecalhoNotaVO cabSaidaVO = (CabecalhoNotaVO) empFinVO.asDymamicVO("CabecalhoNotaModeloSaida").wrapInterface(CabecalhoNotaVO.class);
                CabecalhoNotaVO cabEntradaVO = (CabecalhoNotaVO) empFinVO.asDymamicVO("CabecalhoNotaModelo").wrapInterface(CabecalhoNotaVO.class);
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
                //contextoAcao.confirmar("Aten????o", estVO.toString() ,1);

                criaNotaDeSaida(dwfFacade, estVO, prodVO, cabSaidaVO);

                criaNotaDeEntrada(dwfFacade, novoControle, estVO, prodVO, cabEntradaVO);

                contextoAcao.setMensagemRetorno(String.format("Altera????o feita com sucesso! Nota de sa??da: %s | Nota de entrada: %s", cabSaidaVO.getNUNOTA(), cabEntradaVO.getNUNOTA()));

            }
        }

    }

    private BigDecimal criaNotaDeSaida(EntityFacade dwfFacade, DynamicVO estVO, ProdutoVO prodVO, CabecalhoNotaVO cabSaidaVO) throws Exception {
        //NOTA DE SAIDA
        dwfFacade.createEntity(DynamicEntityNames.CABECALHO_NOTA, cabSaidaVO);
        Collection<ItemNotaVO> itens = new ArrayList<>();
        ItemNotaVO itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA, ItemNotaVO.class);
        itemVO.setNUNOTA(cabSaidaVO.getNUNOTA());
        itemVO.setCODPROD(estVO.asBigDecimal("CODPROD"));
        itemVO.setCODEMP(estVO.asBigDecimal("CODEMP"));
        itemVO.setCODVOL(prodVO.getCODVOL());
        itemVO.setVLRUNIT(BigDecimal.ONE);
        itemVO.setCONTROLE(estVO.asString("CONTROLE"));
        itemVO.setQTDNEG(estVO.asBigDecimal("ESTOQUE"));
        itemVO.setCODLOCALORIG(estVO.asBigDecimal("CODLOCAL"));
        itemVO.setATUALESTOQUE(BigDecimal.ZERO);
        itemVO.setRESERVA("N");
        itens.add(itemVO);
        //contextoAcao.confirmar("Aten????o","Antes de salvar os itens na nota." ,2);
        ItemNotaHelpper.saveItensNota(itens, cabSaidaVO);
        confirmaNota(cabSaidaVO.getNUNOTA());

        return cabSaidaVO.getNUNOTA();
    }

    private BigDecimal criaNotaDeEntrada(EntityFacade dwfFacade, String novoControle, DynamicVO estVO, ProdutoVO prodVO, CabecalhoNotaVO cabEntradaVO) throws Exception {
        ItemNotaVO itemVO;
        Collection<ItemNotaVO> itens;
        //NOTA DE ENTRADA
        dwfFacade.createEntity(DynamicEntityNames.CABECALHO_NOTA, cabEntradaVO);
        itens = new ArrayList<>();
        itemVO = (ItemNotaVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ITEM_NOTA, ItemNotaVO.class);
        itemVO.setNUNOTA(cabEntradaVO.getNUNOTA());
        itemVO.setCODPROD(estVO.asBigDecimal("CODPROD"));
        itemVO.setCODEMP(estVO.asBigDecimal("CODEMP"));
        itemVO.setCODVOL(prodVO.getCODVOL());
        itemVO.setVLRUNIT(BigDecimal.ONE);
        itemVO.setCONTROLE(novoControle);
        itemVO.setQTDNEG(estVO.asBigDecimal("ESTOQUE"));
        itemVO.setCODLOCALORIG(estVO.asBigDecimal("CODLOCAL"));
        itemVO.setATUALESTOQUE(BigDecimal.ONE);
        itemVO.setRESERVA("N");
        itens.add(itemVO);
        ItemNotaHelpper.saveItensNota(itens, cabEntradaVO);

        DynamicVO estNovoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ESTOQUE, new Object[] {estVO.asBigDecimal("CODEMP"), estVO.asBigDecimal("CODPROD"), estVO.asBigDecimal("CODLOCAL"), novoControle, estVO.asBigDecimal("CODPARC"), estVO.asString("TIPO")});
        estNovoVO.setProperty("DTFABRICACAO", estVO.asTimestamp("DTFABRICACAO"));
        estNovoVO.setProperty("DTVAL", estVO.asTimestamp("DTVAL"));
        dwfFacade.saveEntity(DynamicEntityNames.ESTOQUE, (EntityVO) estNovoVO);

        confirmaNota(cabEntradaVO.getNUNOTA());

        return cabEntradaVO.getNUNOTA();
    }
}
