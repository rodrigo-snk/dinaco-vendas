package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedList;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.atualizaVencimento;
import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.calculaVencimento;


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
            Timestamp data = (Timestamp) fin.asTimestamp("DTVENC") ;
            BigDecimal codParc = (BigDecimal) fin.asBigDecimalOrZero("CODPARC");
            BigDecimal nuFin = (BigDecimal) fin.asBigDecimalOrZero("NUFIN");
            LocalDate dtVenc = data.toLocalDateTime().toLocalDate();

            switch (Parceiro.tipoVencimento(codParc)){
                case "S":
                    LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                    if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue())) {
                        atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                    }
                    break;

                case "M":
                    int diaMesVencimento = dtVenc.getDayOfMonth();
                    LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                    //if (!diasMes.get(diaMesVencimento)) Financeiro.calculaVencimento(dtVenc);
                    break;
            }

        }


    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        boolean isModifyingDTVENC = persistenceEvent.getModifingFields().isModifing("DTVENC");

        if (isModifyingDTVENC) {
            Timestamp data = (Timestamp) persistenceEvent.getModifingFields().getNewValue("DTVENC");
            BigDecimal codParc = (BigDecimal) persistenceEvent.getEntityProperty("CODPARC");
            BigDecimal nuFin = (BigDecimal) persistenceEvent.getEntityProperty("NUFIN");
            LocalDate dtVenc = data.toLocalDateTime().toLocalDate();

            switch (Parceiro.tipoVencimento(codParc)){
                case "S":
                    LinkedList<Object> diasSemana = Parceiro.diasSemana(codParc);
                    if (!diasSemana.contains(dtVenc.getDayOfWeek().getValue())) {
                        atualizaVencimento(nuFin, calculaVencimento(dtVenc, diasSemana));
                    }
                    break;

                case "M":
                    int diaMesVencimento = dtVenc.getDayOfMonth();
                    LinkedList<Object> diasMes = Parceiro.diasMes(codParc);
                    //if (!diasMes.get(diaMesVencimento)) Financeiro.calculaVencimento(dtVenc);
                    break;
            }

        }

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
