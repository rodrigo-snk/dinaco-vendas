package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.ant.util.StringUtils;
import com.sankhya.util.TimeUtils;

import javax.jws.Oneway;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DebugTest {

    public static void main(String[] args) {
        // Inicializacao objeto dias do mes
        Map<Object, Boolean> mapDiasMes = new HashMap<>();

        mapDiasMes.put(1, true);
        mapDiasMes.put(2, false);
        mapDiasMes.put(3, false);
        mapDiasMes.put(4, false);
        mapDiasMes.put(5, true);

        //Simular outros dias do mês
        for (int i = 6; i <= 31; i++) {
            if (i != 26) mapDiasMes.put(i, false);
            else mapDiasMes.put(i, true);
        }

        // Inicializacao objeto dias da semana
        Map<Object, Boolean> mapDiasSemana = new HashMap<>();

        mapDiasSemana.put(DayOfWeek.MONDAY.getValue(), true);
        mapDiasSemana.put(DayOfWeek.TUESDAY.getValue(), false);
        mapDiasSemana.put(DayOfWeek.WEDNESDAY.getValue(), false);
        mapDiasSemana.put(DayOfWeek.THURSDAY.getValue(), false);
        mapDiasSemana.put(DayOfWeek.FRIDAY.getValue(), false);
        mapDiasSemana.put(DayOfWeek.SATURDAY.getValue(), false);
        mapDiasSemana.put(DayOfWeek.SUNDAY.getValue(), false);

        //SimpleDateFormat fmt = new SimpleDateFormat("dd");
        //System.out.println(fmt.format(Timestamp.valueOf(LocalDateTime.now())));

        // increment days by 7
        LocalDate date = LocalDate.now();
        System.out.println("Current Date: " + date);
        //date = date.plusDays(7);
        System.out.println("Date after increment: " + date.withDayOfMonth(11));

        LocalDate data = LocalDate.parse("2021-11-09");
        System.out.println(data.getDayOfWeek());
        System.out.println(data.lengthOfMonth());


        LinkedList<Object> diasMes = Parceiro.getDias(mapDiasMes);
        LinkedList<Object> diasSemana =  Parceiro.getDias(mapDiasSemana);

        System.out.println("Parceiro paga nos dias do mês " +diasMes);
        System.out.println("Parceiro paga nos dias da semana " + diasSemana);
        System.out.println(diasMes.contains(5));


        //int dia = 30;
        LocalDate novoDtVenc = data;

        int diaDoVencimento = novoDtVenc.getDayOfWeek().getValue();
        int calculoDias = 0;

        if (!diasSemana.contains(diaDoVencimento)) {
            System.out.println("Parceiro NÃO pode pagar neste dia.");
            for (Object dia : diasSemana) {
                if (diaDoVencimento > (Integer) diasSemana.peekLast()) {
                    calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.peekFirst();
                    break;
                } else if (((Integer) dia) > diaDoVencimento) {
                    calculoDias = 7 - diaDoVencimento + (Integer) diasSemana.get(diasSemana.indexOf(dia))-7;
                    break;
                }
            }
        } else System.out.println("Parceiro pode pagar neste dia.");



        //int calculoDias = 7 - date.getDayOfWeek().getValue() + (int) diasSemana.get(diasSemana.indexOf(5));
        System.out.println("Equation result: " +calculoDias);

        System.out.println("Data de vencimento: "+ novoDtVenc);
        System.out.println("Nova data de vencimento: "+ novoDtVenc.plusDays(calculoDias));

        System.out.println("Nova data de vencimento proximo: "+ novoDtVenc.plusMonths(1).withDayOfMonth(5));

        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getMonthEnd(TimeUtils.getNow()), TimeUtils.getNow()));

        String observacao = null;


        System.out.println();







    }
}
