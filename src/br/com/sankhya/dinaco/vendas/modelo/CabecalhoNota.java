package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Optional;

import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehCompra;
import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehVenda;

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

    public static boolean ehPedidoOuVenda(String tipMov) {
        return "P-V".contains(tipMov);
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
        return item.isPresent() ? item.get().asBigDecimalOrZero("VLRUNITMOE") : BigDecimal.ZERO;
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

    public static void updateEspecie(DynamicVO cabVO) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.CABECALHO_NOTA)
                    .prepareToUpdateByPK(cabVO.asBigDecimalOrZero("NUNOTA"))
                    .set("VOLUME",cabVO.getProperty("VOLUME"))
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
        }
    }

    public static void verificaCRNaturezaDoParceiro(DynamicVO cabVO) throws MGEModelException {
        final String tipMov = cabVO.asString("TIPMOV");
        final BigDecimal codCenCusParceiro = Parceiro.getCodCenCus(cabVO.getProperty("CODPARC"));
        final BigDecimal codNatParceiro = Parceiro.getCodNat(cabVO.getProperty("CODPARC"));

        // Preeenche com Centro de Custo do Parceiro (TGFPAR.AD_CODCENCUS)
        // Se TIPMOV in ('O','C','E','P','V', 'D')
        if ((ehCompra(tipMov) || ehVenda(tipMov))) {
            if (!BigDecimalUtil.isNullOrZero(codCenCusParceiro)) cabVO.setProperty("CODCENCUS", codCenCusParceiro);
            if (!BigDecimalUtil.isNullOrZero(codNatParceiro)) cabVO.setProperty("CODNAT", codNatParceiro);;
        }
    }

    /**
     *  Verifica se frete diferente de FOB ou Sem Frete e se Parceiro Transportadora não está preenchido
     */
    public static void verificaTransportadoraObrigatoria(DynamicVO cabVO) throws Exception {
        final String cifFob = StringUtils.getNullAsEmpty(cabVO.asString("CIF_FOB"));
        final BigDecimal codParcTransp = cabVO.asBigDecimalOrZero("CODPARCTRANSP");
        final boolean naoPrecisaTransportadora = "S".equalsIgnoreCase(cifFob) || "F".equalsIgnoreCase(cifFob);

        if (!naoPrecisaTransportadora && BigDecimalUtil.isNullOrZero(codParcTransp)){
            //throw new MGEModelException("Transportadora obrigatória, Forma Entrega: " +cabVO.asString("AD_FORMAENTREGA")+ ", CIF/FOB: " +cabVO.asString("CIF_FOB")+ ", Transportadora: " +codParcTransp);
            throw new MGEModelException("Transportadora obrigatória para a forma de entrega selecionada. Verifique na aba Transporte.");
        }
    }
    public static boolean exigeOC(DynamicVO cabVO) throws Exception {
        return StringUtils.getNullAsEmpty(Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asString("AD_EXIGEOC")).equalsIgnoreCase("S")
                && StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_EXIGEOC")).equalsIgnoreCase("S");

    }

    public static boolean negociacaoDiferenteDaSugerida(ContextoRegra contextoRegra, DynamicVO cabVO) throws Exception {
        BigDecimal codTipVenda = cabVO.asBigDecimal("CODTIPVENDA");
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean validaSugestao = topVO.containsProperty("AD_VALIDATIPNEG") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_VALIDATIPNEG")));
        final boolean ehVenda = CabecalhoNota.ehPedidoOuVenda(topVO.asString("TIPMOV"));
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
        final boolean ehVenda = CabecalhoNota.ehPedidoOuVenda(topVO.asString("TIPMOV"));
        final boolean ehCompra = ComercialUtils.ehCompra(topVO.asString("TIPMOV"));

        DynamicVO complementoParcVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.COMPLEMENTO_PARCEIRO, cabVO.asBigDecimal("CODPARC"));
        BigDecimal sugestaoEntrada = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGENTR");
        BigDecimal sugestaoSaida = complementoParcVO.asBigDecimalOrZero("SUGTIPNEGSAID");

        if (validaSugestao && ehVenda && !BigDecimalUtil.isNullOrZero(sugestaoSaida)) {
            if (codTipVenda.compareTo(sugestaoSaida) != 0) {
                return true;
            }
        }
        if (validaSugestao && ehCompra && !BigDecimalUtil.isNullOrZero(sugestaoEntrada)) {
            return codTipVenda.compareTo(sugestaoEntrada) != 0;
        }
        return false;
    }

    private static BigDecimal getCotacaoDiaAnterior(BigDecimal codMoeda, Timestamp hoje) throws Exception {
        final Timestamp ontem = TimeUtils.dataAdd(hoje,-1, 5);
        Collection<DynamicVO> cotVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COTACAO_MOEDA, "this.CODMOEDA = ? and this.DTMOV = ?", new Object[] {codMoeda, TimeUtils.clearTime(ontem)}));

        if (!cotVO.stream().findFirst().isPresent()) {
            return getCotacaoDiaAnterior(codMoeda, ontem);
        }

        return cotVO.stream().findFirst().get().asBigDecimalOrZero("COTACAO");
    }


    private static BigDecimal getCotacaoMediaPeriodo(BigDecimal codMoeda, Timestamp dtInicio, Timestamp dtFim) throws Exception {
        Collection<DynamicVO> cotVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.COTACAO_MOEDA, "this.CODMOEDA = ? and this.DTMOV BETWEEN ? AND ?", new Object[] {codMoeda, dtInicio, dtFim}));
        BigDecimal soma = cotVO.stream().map(vo -> vo.asBigDecimalOrZero("COTACAO")).reduce(BigDecimal.ZERO, BigDecimal::add);
        return soma.divide(new BigDecimal(cotVO.size()), RoundingMode.HALF_UP);
    }

    private static BigDecimal getCotacaoMediaMes(BigDecimal codMoeda) throws Exception {
        Timestamp hoje = TimeUtils.getNow();
        return getCotacaoMediaPeriodo(codMoeda, TimeUtils.getMonthStart(hoje), TimeUtils.getMonthEnd(hoje));
    }

    private static BigDecimal getCotacaoMediaMesAnterior(BigDecimal codMoeda) throws Exception {
        Timestamp ultimoDiaMesPassado = TimeUtils.getUltimoDiaDoMesRefAnterior(TimeUtils.getNow());
        Timestamp primeiroDiaMesPassado = TimeUtils.getMonthStart(ultimoDiaMesPassado);

        return getCotacaoMediaPeriodo(codMoeda, primeiroDiaMesPassado, ultimoDiaMesPassado);
    }

    public static void verificaPTAX(DynamicVO cabVO) throws Exception {
        DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

        final boolean ptaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));
        final boolean ptaxFixo = cabVO.containsProperty("AD_PTAXFIXO") && "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("AD_PTAXFIXO")));
        final boolean ptaxMedio = cabVO.containsProperty("AD_PTAXMEDIO") && "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("AD_PTAXMEDIO")));

        if (ptaxDiaAnterior && !ptaxFixo) cabVO.setProperty("VLRMOEDA", getCotacaoDiaAnterior(cabVO.asBigDecimalOrZero("CODMOEDA"), TimeUtils.getNow()));

        if (ptaxMedio) cabVO.setProperty("VLRMOEDA", getCotacaoMediaMesAnterior(cabVO.asBigDecimalOrZero("CODMOEDA")));
    }
}
