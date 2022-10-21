package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.cotacao.model.services.CotacaoHelper;
import br.com.sankhya.dinaco.vendas.acoes.ValidaCotacao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.mgecomercial.model.centrais.cac.CACSP;
import br.com.sankhya.mgecomercial.model.centrais.cac.CACSPBean;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.CentralFinanceirosUtil;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SPBeanUtils;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;
import br.com.sankhya.ws.BusinessException;
import br.com.sankhya.ws.ServiceContext;
import com.google.gson.JsonObject;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;

public class CabecalhoNota {

    public static Collection<DynamicVO> buscaNotasOrigem(Object nuNota) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        Collection<DynamicVO> notasVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, "this.NUNOTA = ?", nuNota);
            finder.setMaxResults(-1);
            notasVO = dwfFacade.findByDynamicFinderAsVO(finder);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return notasVO;
    }

    public static boolean ehPedidoVenda(String tipMov) {
        return "P".contains(tipMov);
    }
    public static boolean ehPedidoCompraVenda(String tipMov) {
        return "P-O".contains(tipMov);
    }

    public static DynamicVO buscaNotaPelaPK(BigDecimal nuNota) {
        JapeSession.SessionHandle hnd = null;
        DynamicVO notaVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
            notaVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JapeSession.close(hnd);
        }
        return notaVO;
    }

    /*public static BigDecimal ultimoPrecoVendaNFe(BigDecimal codProd) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        Collection<DynamicVO> itensVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.CODPROD = ?", codProd);
            finder.setMaxResults(-1);
            finder.setOrderBy("NUNOTA DESC");
            itensVO = dwfFacade.findByDynamicFinderAsVO(finder);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

        for (DynamicVO itemVO: itensVO) {
            DynamicVO cabVO = buscaNotaPelaPK(itemVO.asBigDecimalOrZero("NUNOTA"));
            if (ComercialUtils.ehNFEAprovada(cabVO) && ComercialUtils.ehVenda(cabVO.asString("TIPMOV"))){
                return itemVO.asBigDecimal("VLRUNIT");
            }
        }
        return BigDecimal.ZERO;
    }*/

    public static BigDecimal ultimoPrecoVendaNFe(BigDecimal codProd, BigDecimal codParc) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        Optional<DynamicVO> item = Optional.empty();
        try {
            hnd = JapeSession.open();
            JdbcWrapper jdbc =  EntityFacadeFactory.getDWFFacade().getJdbcWrapper();
            NativeSql sql = new NativeSql(jdbc);

            sql.appendSql(" SELECT ITE.*, CAB.CODPARC");
            sql.appendSql(" FROM TGFITE ITE ");
            sql.appendSql(" JOIN TGFCAB CAB ON CAB.NUNOTA = ITE.NUNOTA ");
            sql.appendSql(" WHERE ITE.CODPROD = :CODPROD ");
            sql.appendSql(" AND CAB.CODPARC = :CODPARC ");
            sql.appendSql(" ORDER BY CAB.NUNOTA DESC ");

            sql.setNamedParameter("CODPROD", codProd);
            sql.setNamedParameter("CODPARC", codParc);

            item = sql.asVOCollection(DynamicEntityNames.ITEM_NOTA).stream()
                    .filter(vo -> ComercialUtils.ehNFEAprovada(buscaNotaPelaPK(vo.asBigDecimalOrZero("NUNOTA"))) && ComercialUtils.ehVenda(buscaNotaPelaPK(vo.asBigDecimalOrZero("NUNOTA")).asString("TIPMOV")))
                    .findFirst();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return item.isPresent() ? item.get().asBigDecimal("VLRUNITMOE") : BigDecimal.ZERO;
    }

    public static void updateCodCenCus(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CODCENCUS",Parceiro.getCodCenCus(cabVO.getProperty("CODPARC")))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }


    public static void updateEspecie(DynamicVO cabVO, String volume) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();

            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA).
                    prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("VOLUME",volume)
                    .update();

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void updateCodNat(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CODNAT",Parceiro.getCodNat(cabVO.getProperty("CODPARC")))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void update(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("CIF_FOB",cabVO.getProperty("CIF_FOB"))
                    .set("AD_REDESPACHO", cabVO.getProperty("AD_REDESPACHO"))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void verificaFormaEntrega(DynamicVO cabVO) throws Exception {
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

        final boolean entregaAmostra = topVO.containsProperty("AD_ENTREGAAMOSTRA") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_ENTREGAAMOSTRA")));

        if (!entregaAmostra) {
            final String formaEntrega = StringUtils.getNullAsEmpty(cabVO.asString("AD_FORMAENTREGA"));

            switch (formaEntrega) {
                //CIF 2-4-5-6-7
                case "2":
                case "6":
                case "7":
                    cabVO.setProperty("AD_REDESPACHO", "N");
                    cabVO.setProperty("CIF_FOB","C");
                    break;
                case "4":
                case "5":
                    cabVO.setProperty("AD_REDESPACHO", "S");
                    cabVO.setProperty("CIF_FOB","C");
                    break;
                case "1": //FOB 1
                    cabVO.setProperty("CIF_FOB","F");
                    cabVO.setProperty("AD_REDESPACHO", "N");
                    break;
                case "3": //Sem Frete
                default:
                    cabVO.setProperty("CIF_FOB","S");
                    cabVO.setProperty("AD_REDESPACHO", "N");
                    break;
            }
            // Preenche transportadora com TRANSAL SC (7790) se não houver transportadora preenchida
            if ("C".equals(cabVO.asString("CIF_FOB")) && BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODPARCTRANSP")))
                cabVO.setProperty("CODPARCTRANSP", BigDecimal.valueOf(7790)); // TRANSAL SC
        }
    }

    public static void verificaNaturezaDoParceiro(DynamicVO cabVO) throws MGEModelException {
        final String tipMov = cabVO.asString("TIPMOV");

        // Preeenche com Natureza do Parceiro (TGFPAR.AD_CODNAT)
        // Se TIPMOV in ('V')
        if (ehPedidoVenda(tipMov)) {
            final BigDecimal codNatParceiro = Parceiro.getCodNat(cabVO.getProperty("CODPARC"));
            if (!BigDecimalUtil.isNullOrZero(codNatParceiro) && BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODNAT"))) cabVO.setProperty("CODNAT", codNatParceiro);;
        }
    }

    public static void verificaCRDoParceiro(DynamicVO cabVO) throws MGEModelException {
        final String tipMov = cabVO.asString("TIPMOV");

        // Preeenche com Centro de Custo do Parceiro (TGFPAR.AD_CODCENCUS)
        // Se TIPMOV in ('V')
        if (ehPedidoVenda(tipMov)) {
            final BigDecimal codCenCusParceiro = Parceiro.getCodCenCusUnidadeNegocio(cabVO.getProperty("CODPARC"));
            if (!BigDecimalUtil.isNullOrZero(codCenCusParceiro) && BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODCENCUS"))) cabVO.setProperty("CODCENCUS", codCenCusParceiro);
        }
    }

    /**
     *  Verifica se frete diferente de FOB ou Sem Frete e se Parceiro Transportadora não está preenchido
     */
    public static String verificaTransportadoraObrigatoria(DynamicVO cabVO) throws Exception {
        DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean topIgnoraFormaEntrega = DataDictionaryUtils.campoExisteEmTabela("AD_IGNORAFORMAENTREGA", "TGFTOP") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_IGNORAFORMAENTREGA")));
        final boolean topObrigaTransportadora = DataDictionaryUtils.campoExisteEmTabela("AD_OBRIGATRANSP", "TGFTOP") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_OBRIGATRANSP")));
        final String cifFob = StringUtils.getNullAsEmpty(cabVO.asString("CIF_FOB"));
        final boolean semTransportadora = BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODPARCTRANSP"));
        final boolean naoPrecisaTransportadora = "S".equalsIgnoreCase(cifFob) || "F".equalsIgnoreCase(cifFob);

        if (!topIgnoraFormaEntrega && topObrigaTransportadora && !naoPrecisaTransportadora && semTransportadora){
            //throw new MGEModelException("Transportadora obrigatória, Forma Entrega: " +cabVO.asString("AD_FORMAENTREGA")+ ", CIF/FOB: " +cabVO.asString("CIF_FOB")+ ", Transportadora: " +codParcTransp);
            return "Parceiro Transportadora obrigatório para a forma de entrega selecionada.\n";

        }
        return "";

    }

    public static String verificaRedespacho(DynamicVO cabVO) throws Exception {
        DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean ignoraFormaEntrega = DataDictionaryUtils.campoExisteEmTabela("AD_IGNORAFORMAENTREGA", "TGFTOP") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_IGNORAFORMAENTREGA")));
        final boolean isRedespacho =  DataDictionaryUtils.campoExisteEmTabela("AD_REDESPACHO", "TGFCAB") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(cabVO.asString("AD_REDESPACHO")));
        final boolean semRedespacho =  BigDecimalUtil.isNullOrZero(cabVO.asBigDecimalOrZero("CODPARCREDESPACHO"));

        if (!ignoraFormaEntrega && isRedespacho && semRedespacho) {
            return "Redespacho (Recebedor) é obrigatório para a forma de entrega selecionada.\n";
        }

        return "";
    }

    public static boolean exigeNumPedido2(DynamicVO cabVO) throws Exception {
        return "S".equals(StringUtils.getNullAsEmpty(Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asString("AD_EXIGEOC")))
                && "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_EXIGEOC")));

    }

    public static boolean negociacaoDiferenteDaSugerida(ContextoRegra contextoRegra, DynamicVO cabVO) throws Exception {
        BigDecimal codTipVenda = cabVO.asBigDecimal("CODTIPVENDA"); // VERIFICAR QUANDO AO ALTERAR O TIPNEG O CABEÇALHO
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean validaSugestao = topVO.containsProperty("AD_VALIDATIPNEG") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_VALIDATIPNEG")));
        final boolean ehVenda = CabecalhoNota.ehPedidoVenda(topVO.asString("TIPMOV"));
        final boolean ehCompra = ComercialUtils.ehCompra(topVO.asString("TIPMOV"));

        DynamicVO complementoParcVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.COMPLEMENTO_PARCEIRO, cabVO.asBigDecimal("CODPARC"));
        BigDecimal sugestaoEntrada = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGENTR");
        BigDecimal sugestaoSaida = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGSAID");

        if (validaSugestao && ehVenda && !BigDecimalUtil.isNullOrZero(sugestaoSaida)) {
            if (codTipVenda.compareTo(sugestaoSaida) != 0) {
                contextoRegra.getBarramentoRegra().addMensagem("Tipo de Negociação diferente da sugerida para o parceiro. Necessita liberação na confirmação da nota.");
                return true;
            }
        }
        if (validaSugestao && ehCompra && !BigDecimalUtil.isNullOrZero(sugestaoEntrada)) {
            if (codTipVenda.compareTo(sugestaoEntrada) != 0) {
                contextoRegra.getBarramentoRegra().addMensagem("Tipo de Negociação diferente da sugerida para o parceiro. Necessita liberação na confirmação da nota.");
                return true;
            }
        }
        return false;
    }

    public static boolean negociacaoDiferenteDaSugerida(DynamicVO cabVO) throws Exception {
        BigDecimal codTipVenda = cabVO.asBigDecimal("CODTIPVENDA");
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean validaSugestao = topVO.containsProperty("AD_VALIDATIPNEG") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_VALIDATIPNEG")));
        final boolean ehVenda = CabecalhoNota.ehPedidoVenda(topVO.asString("TIPMOV"));
        final boolean ehCompra = ComercialUtils.ehCompra(topVO.asString("TIPMOV"));

        DynamicVO complementoParcVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.COMPLEMENTO_PARCEIRO, cabVO.asBigDecimal("CODPARC"));
        BigDecimal sugestaoEntrada = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGENTR");
        BigDecimal sugestaoSaida = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGSAID");

        if (validaSugestao && ehVenda && !BigDecimalUtil.isNullOrZero(sugestaoSaida)) {
            return codTipVenda.compareTo(sugestaoSaida) != 0;
        }

        if (validaSugestao && ehCompra && !BigDecimalUtil.isNullOrZero(sugestaoEntrada)) {
            return codTipVenda.compareTo(sugestaoEntrada) != 0;
        }
        return false;
    }

    public static BigDecimal getCotacaoDiaAnterior(BigDecimal codMoeda, Timestamp hoje) throws Exception {
        if (BigDecimalUtil.isNullOrZero(codMoeda)) return BigDecimal.ZERO;
        final Timestamp ontem = TimeUtils.dataAdd(hoje,-1, 5);
        Collection<DynamicVO> cotVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COTACAO_MOEDA, "this.CODMOEDA = ? and this.DTMOV = ?", new Object[] {codMoeda, TimeUtils.clearTime(ontem)}));

        if (!cotVO.stream().findFirst().isPresent()) {
            return getCotacaoDiaAnterior(codMoeda, ontem);
        }

        return cotVO.stream().findFirst().get().asBigDecimalOrZero("COTACAO");
    }

    public static BigDecimal getCotacaoDia(BigDecimal codMoeda, Timestamp hoje) throws Exception {
        if (BigDecimalUtil.isNullOrZero(codMoeda)) return BigDecimal.ZERO;
        Collection<DynamicVO> cotVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COTACAO_MOEDA, "this.CODMOEDA = ? and this.DTMOV = ?", new Object[] {codMoeda, TimeUtils.clearTime(hoje)}));

        if (!cotVO.stream().findFirst().isPresent()) {
            return getCotacaoDia(codMoeda, hoje);
        }

        return cotVO.stream().findFirst().get().asBigDecimalOrZero("COTACAO");
    }


    private static BigDecimal getCotacaoMediaPeriodo(BigDecimal codMoeda, Timestamp dtInicio, Timestamp dtFim) throws Exception {
        if (BigDecimalUtil.isNullOrZero(codMoeda)) return null;
        Collection<DynamicVO> cotVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COTACAO_MOEDA, "this.CODMOEDA = ? and this.DTMOV BETWEEN ? AND ?", new Object[] {codMoeda, dtInicio, dtFim}));
        BigDecimal soma = cotVO.stream().map(vo -> vo.asBigDecimalOrZero("COTACAO")).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(new BigDecimal(cotVO.size()), RoundingMode.HALF_UP);
    }

    private static BigDecimal getCotacaoMediaMes(BigDecimal codMoeda) throws Exception {
        Timestamp hoje = TimeUtils.getNow();
        return getCotacaoMediaPeriodo(codMoeda, TimeUtils.getMonthStart(hoje), TimeUtils.getMonthEnd(hoje));
    }

    private static BigDecimal getCotacaoMediaMesAnterior(BigDecimal codMoeda) throws Exception {
        if (BigDecimalUtil.isNullOrZero(codMoeda)) return null;
        Timestamp ultimoDiaMesPassado = TimeUtils.getUltimoDiaDoMesRefAnterior(TimeUtils.getNow());
        Timestamp primeiroDiaMesPassado = TimeUtils.getMonthStart(ultimoDiaMesPassado);

        return getCotacaoMediaPeriodo(codMoeda, primeiroDiaMesPassado, ultimoDiaMesPassado);
    }

    public static void verificaPTAX(DynamicVO cabVO, Boolean alterandoVlrMoeda) throws Exception {

        if (!BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODMOEDA"))) {
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            DynamicVO moedaVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO("Moeda", cabVO.asBigDecimal("CODMOEDA"));

            final boolean moedaTemPtaxMedio = moedaVO.asDymamicVO("Moeda_AD001") != null && moedaVO.containsProperty("AD_PTAXMEDIO") && "S".equals(StringUtils.getNullAsEmpty(moedaVO.asDymamicVO("Moeda_AD001").asString("AD_PTAXMEDIO")));
            final boolean topPtaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));
            final boolean ptaxFixo = cabVO.containsProperty("AD_PTAXFIXO") && "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("AD_PTAXFIXO")));
            final boolean ptaxMedio = cabVO.containsProperty("AD_PTAXMEDIO") && "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("AD_PTAXMEDIO"))) && Parceiro.temPtaxMedio(cabVO.asBigDecimalOrZero("CODPARC"));
            final boolean faturamentoFuturo = TimeUtils.compareOnlyDates(TimeUtils.getNow(), cabVO.asTimestamp("DTFATUR")) < -3;
            Timestamp dataReferencia = faturamentoFuturo ? TimeUtils.getNow() : cabVO.asTimestamp("DTFATUR");
            dataReferencia = dataReferencia == null ? TimeUtils.getNow() : dataReferencia;
            final boolean mesmaCotacao = cabVO.asBigDecimalOrZero("VLRMOEDA").compareTo(getCotacaoDiaAnterior(cabVO.asBigDecimal("CODMOEDA"), dataReferencia)) == 0;

            //if (true) throw new MGEModelException(String.valueOf(dataReferencia));

            if (topPtaxDiaAnterior && alterandoVlrMoeda && !ptaxFixo && !mesmaCotacao) {
                throw (BusinessException) SKError.registry(TSLevel.ERROR, "DINACO_REGRAS", new BusinessException(StringUtils.htmlScape("Não é permitido alterar Vlr. Moeda sem marcar PTAX Fixo.")));
            }

            if (topPtaxDiaAnterior && !ptaxFixo)
                cabVO.setProperty("VLRMOEDA", getCotacaoDiaAnterior(cabVO.asBigDecimal("CODMOEDA"), dataReferencia));

            if (ptaxMedio) {
                if (!moedaTemPtaxMedio) throw new MGEModelException("Moeda não tem PTAX médio. Verifique a rotina Valores de Moeda.");
                cabVO.setProperty("VLRMOEDA", getCotacaoDiaAnterior(moedaVO.asBigDecimal("AD_CODMOEDAMEDIO"), dataReferencia));
            }

        }

    }

    public static void confirmaNota(BigDecimal nuNota) throws Exception {
        BarramentoRegra barramentoConfirmacao = BarramentoRegra.build(CentralFaturamento.class, "regrasConfirmacaoSilenciosa.xml", AuthenticationInfo.getCurrent());
        barramentoConfirmacao.setValidarSilencioso(true);
        ConfirmacaoNotaHelper.confirmarNota(nuNota, barramentoConfirmacao);
    }

    public static void recalculaNota(BigDecimal nuNota) throws Exception {
        ImpostosHelpper impostosHelper = new ImpostosHelpper();
        impostosHelper.calcularImpostos(nuNota);
        impostosHelper.totalizarNota(nuNota);

        final CentralFinanceiro centralFinanceiro = new CentralFinanceiro();
        centralFinanceiro.inicializaNota(nuNota);
        centralFinanceiro.refazerFinanceiro();
    }

    public static BigDecimal getEmpresa(BigDecimal nuNota) throws MGEModelException {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;
        JapeSession.SessionHandle hnd = null;
        BigDecimal codEmp = BigDecimal.ZERO;

        try {
            hnd = JapeSession.open();
            hnd.setFindersMaxRows(-1);
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);

            sql.appendSql("SELECT CODEMP FROM TGFCAB WHERE NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nuNota);
            rset = sql.executeQuery();

            if (rset.next()) {
                codEmp = rset.getBigDecimal("NUNOTA");
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JdbcUtils.closeResultSet(rset);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }

        return codEmp;
    }

    public static boolean temProdutoPerigoso(BigDecimal nuNota) throws MGEModelException {

        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;
        JapeSession.SessionHandle hnd = null;
        BigDecimal codEmp = BigDecimal.ZERO;

        try {
            hnd = JapeSession.open();
            hnd.setFindersMaxRows(-1);
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);

            sql.appendSql("select count(*) PRODPERIGO\n" +
                    "FROM TGFCAB cab\n" +
                    "JOIN TGFITE ite ON ite.NUNOTA = cab.NUNOTA\n" +
                    "JOIN TGFPRO pro ON ite.CODPROD = pro.CODPROD\n" +
                    "WHERE pro.AD_PROPERIGO = 'S'\n"+
                    "AND cab.NUNOTA = :NUNOTA");
            sql.setNamedParameter("NUNOTA", nuNota);
            rset = sql.executeQuery();

            if (rset.next()) {
                int produtosPerigosos = rset.getInt("PRODPERIGO");
                return produtosPerigosos > 0;
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JdbcUtils.closeResultSet(rset);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }

        return false;

    }


}
