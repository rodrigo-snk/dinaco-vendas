package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.comercial.LiberacaoSolicitada;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.LiberacaoLimiteVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.LinkedList;

public class Financeiro {

    public static DynamicVO getFinanceiroByPK(Object nuFin) throws MGEModelException {
        DynamicVO financeiroVO = null;
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper financeiroDAO = JapeFactory.dao(DynamicEntityNames.FINANCEIRO);
            financeiroVO = financeiroDAO.findByPK(nuFin);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return financeiroVO;
    }

    public static Collection<DynamicVO> getFinanceirosByNunota(BigDecimal nuNota) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        Collection<DynamicVO> financeirosVO = null;
        try {
            hnd = JapeSession.open();
            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.FINANCEIRO, "this.NUNOTA = ?", new Object[] { nuNota });
            finder.setOrderBy("CODEMP");
            finder.setMaxResults(-1);
            financeirosVO = dwfFacade.findByDynamicFinderAsVO(finder);
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
        return financeirosVO;

    }

   public static LocalDate calculaVencimento(LocalDate dtVenc, LinkedList<Object> diasSemana) throws MGEModelException {
        int diaDoVencimento = dtVenc.getDayOfWeek().getValue();
        int calculoDias = 0;
       for (Object dia : diasSemana) {
           if (diaDoVencimento > (Integer) diasSemana.peekLast()) {
               calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.peekFirst();
               break;
           } else if (((Integer) dia) > diaDoVencimento) {
               calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.get(diasSemana.indexOf(dia))-7;
               break;
           }
       }
        return dtVenc.plusDays(calculoDias);
    }

    public static LocalDate calculaVencimentoMes(LocalDate dtVenc, LinkedList<Object> diasMes) {
        int diaDoVencimento = dtVenc.getDayOfMonth();
        int tamanhodoMes = dtVenc.lengthOfMonth();

        for (Object dia : diasMes) {
            if (diaDoVencimento > (Integer) diasMes.peekLast()) {
                dtVenc = dtVenc.withDayOfMonth((Integer) diasMes.peekFirst()).plusMonths(1);
                break;
            } else if ((Integer) dia > diaDoVencimento && (Integer) dia <= tamanhodoMes) {
                dtVenc = dtVenc.withDayOfMonth((Integer) dia);
                break;
            }
        }
        return dtVenc;
    }

    public static void atualizaVencimento(Object nuFin, LocalDate dtVenc) throws Exception {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao(DynamicEntityNames.FINANCEIRO)
            .prepareToUpdateByPK(nuFin)
            .set("DTVENC", Timestamp.valueOf(dtVenc.atTime(LocalTime.MIDNIGHT)))
            .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static boolean fimDeSemana(LocalDate ld) {
        DayOfWeek d = ld.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    public void verificaRegras(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO fin = (DynamicVO) persistenceEvent.getVo();
        Timestamp data = (Timestamp) persistenceEvent.getModifingFields().getNewValue("DTVENC");
        BigDecimal codParc = fin.asBigDecimalOrZero("CODPARC");
        BigDecimal nuFin = fin.asBigDecimalOrZero("NUFIN");
        BigDecimal recDesp = fin.asBigDecimalOrZero("RECDESP");
        LocalDate dtVenc = data.toLocalDateTime().toLocalDate();
        boolean isReceita = recDesp.compareTo(BigDecimal.ONE) == 0;
        boolean isDespesa = recDesp.compareTo(BigDecimal.valueOf(-1)) == 0;

        boolean atualiza = Parceiro.tipoRegra(codParc).equalsIgnoreCase("A") || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("D")) && isDespesa) || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("R")) && isReceita);

        switch (Parceiro.tipoVencimento(codParc)){
            case "S":
                LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue()) && atualiza) {
                    atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                }
                break;

            case "M":
                int diaMesVencimento = dtVenc.getDayOfMonth();
                LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                if (!diasMes.contains(diaMesVencimento) && atualiza) {
                    atualizaVencimento(nuFin,calculaVencimentoMes(dtVenc, diasMes));
                }
                break;

            case "P":
                int diaFixo = Parceiro.maisDias(codParc).intValue();
                if ((diaFixo != 0) && atualiza) {
                    //atualizaVencimento(nuFin, dtVenc.plusMonths(1).withDayOfMonth(diaFixo));
                    atualizaVencimento(nuFin, dtVenc.plusDays(1));
                }
                break;
        }


    }

    public static void liberacaoLimite(ContextoRegra contextoRegra, BigDecimal codUsuarioLogado, DynamicVO notaVO, String observacao, int evento) {
        try {
            LiberacaoSolicitada ls = new LiberacaoSolicitada(notaVO.asBigDecimalOrZero("NUNOTA"),"TGFCAB", evento, BigDecimal.ZERO);
            ls.setCodCenCus(notaVO.asBigDecimalOrZero("CODCENCUS"));
            ls.setSolicitante(codUsuarioLogado);
            ls.setLiberador(BigDecimal.ZERO);
            ls.setObsLiberador(observacao);
            ls.setVlrAtual(notaVO.asBigDecimalOrZero("VLRNOTA"));
            ls.setVlrTotal(notaVO.asBigDecimalOrZero("VLRNOTA"));
            ls.setCodTipOper(notaVO.asBigDecimalOrZero("CODTIPOPER"));
            ls.setVlrLimite(BigDecimal.ZERO);
            ls.setDhSolicitacao(TimeUtils.getNow());

            LiberacaoLimiteVO liberacaoLimiteVO = LiberacaoAlcadaHelper.carregaLiberacao(ls.getChave(), ls.getTabela(), ls.getEvento(), ls.getSequencia());
            boolean semSolicitacao = liberacaoLimiteVO == null;

            if (semSolicitacao) {
                LiberacaoAlcadaHelper.validarLiberacoesPendentes(notaVO.asBigDecimalOrZero("NUNOTA"));
                LiberacaoAlcadaHelper.processarLiberacao(ls);
                contextoRegra.getBarramentoRegra().addLiberacaoSolicitada(ls);
            } else {
                if (liberacaoLimiteVO.getDHLIB() == null) {
                    contextoRegra.getBarramentoRegra().getLiberacoesSolicitadas();
                    contextoRegra.getBarramentoRegra().addLiberacaoSolicitada(ls);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
