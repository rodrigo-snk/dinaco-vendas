package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.LinkedList;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.*;


public class AjustaVencimento implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO fin = (DynamicVO) persistenceEvent.getVo();
        boolean hasDTVENC = fin.asTimestamp("DTVENC") != null;

        if (hasDTVENC) {
            Timestamp data = fin.asTimestamp("DTVENC") ;
            BigDecimal codParc = fin.asBigDecimalOrZero("CODPARC");
            BigDecimal nuFin = fin.asBigDecimalOrZero("NUFIN");
            BigDecimal recDesp = fin.asBigDecimalOrZero("RECDESP");
            LocalDate dtVenc = data.toLocalDateTime().toLocalDate();
            boolean isReceita = recDesp.compareTo(BigDecimal.ONE) == 0;
            boolean isDespesa = recDesp.compareTo(BigDecimal.valueOf(-1)) == 0;

            //Verifica regra para opção receita/despesa
            boolean atualiza = Parceiro.tipoRegra(codParc).equalsIgnoreCase("A") || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("D") && isDespesa) || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("R") && isReceita)));

            switch (Parceiro.tipoVencimento(codParc)){
                case "S":
                    LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                    if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue()) && atualiza) {
                        atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                    }
                    break;

                case "M":
                    LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                    if (!diasMes.contains(dtVenc.getDayOfMonth()) && atualiza) {
                        atualizaVencimento(nuFin,calculaVencimentoMes(dtVenc, diasMes));
                    }
                    break;

                case "P":
                    int dias = Parceiro.maisDias(codParc).intValue();
                    if ((dias != 0) && atualiza) {
                        atualizaVencimento(nuFin, dtVenc.withDayOfMonth(dias).plusMonths(1));
                    }
                    break;
            }

        }
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
       /* DynamicVO fin = (DynamicVO) persistenceEvent.getVo();
        boolean isModifyingDTVENC = persistenceEvent.getModifingFields().isModifing("DTVENC");

        if (isModifyingDTVENC) {
            Timestamp data = fin.asTimestamp("DTVENC") ;
            BigDecimal codParc = fin.asBigDecimalOrZero("CODPARC");
            BigDecimal nuFin = fin.asBigDecimalOrZero("NUFIN");
            BigDecimal recDesp = fin.asBigDecimalOrZero("RECDESP");
            LocalDate dtVenc = data.toLocalDateTime().toLocalDate();
            boolean isReceita = recDesp.compareTo(BigDecimal.ONE) == 0;
            boolean isDespesa = recDesp.compareTo(BigDecimal.valueOf(-1)) == 0;

            //Verifica regra para opção receita/despesa
            boolean atualiza = Parceiro.tipoRegra(codParc).equalsIgnoreCase("A") || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("D") && isDespesa) || ((Parceiro.tipoRegra(codParc).equalsIgnoreCase("R") && isReceita)));

            switch (Parceiro.tipoVencimento(codParc)){
                case "S":
                    LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                    if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue()) && atualiza) {
                        atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                    }
                    break;

                case "M":
                    LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                    if (!diasMes.contains(dtVenc.getDayOfMonth()) && atualiza) {
                        atualizaVencimento(nuFin,calculaVencimentoMes(dtVenc, diasMes));
                    }
                    break;

                case "P":
                    int dias = Parceiro.maisDias(codParc).intValue();
                    if ((dias != 0) && atualiza) {
                        //atualizaVencimento(nuFin, dtVenc.plusMonths(1).withDayOfMonth(dias));
                        atualizaVencimento(nuFin, dtVenc.withDayOfMonth(dias).plusMonths(1));

                        //if (true) throw new MGEModelException("DEURUIMPRACARALHO");
                    }
            }
        }*/
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }


}
