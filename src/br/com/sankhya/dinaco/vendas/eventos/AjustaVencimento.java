package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.Financeiro;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        boolean isModifyingDTVENC = persistenceEvent.getModifingFields().isModifing("DTVENC");

        if (isModifyingDTVENC) {
            LocalDate dtVenc = (LocalDate) persistenceEvent.getModifingFields().getNewValue("DTVENC");
            BigDecimal codParc = (BigDecimal) persistenceEvent.getEntityProperty("CODPARC");
            BigDecimal nuFin = (BigDecimal) persistenceEvent.getEntityProperty("NUFIN");

            Integer diaMesVencimento = dtVenc.getDayOfMonth();
            DayOfWeek diaSemanaVencimento = dtVenc.getDayOfWeek();

            switch (Parceiro.tipoVencimento(codParc)){
                case "S":
                    List<Integer> diasSemana = Parceiro.diasSemana(codParc);
                    // (!diasSemana.get(diaSemanaVencimento)) Financeiro.calculaVencimento(dtVenc);



                case "M":
                    List<Integer> diasMes = Parceiro.diasMes(codParc);
                    //if (!diasMes.get(diaMesVencimento)) Financeiro.calculaVencimento(dtVenc);
                    break;
            }





        }

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
