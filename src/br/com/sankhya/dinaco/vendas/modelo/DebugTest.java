package br.com.sankhya.dinaco.vendas.modelo;

import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import com.sencha.gxt.fx.client.animation.SlideOut;
import org.junit.Test;

import javax.jws.Oneway;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class DebugTest {

    @Test
    public void blablablabla() {

        assertEquals(new BigDecimal(1030.00), BigDecimal.ZERO);

    }
    @Test
    public void setDataLimiteQueClienteAceitaVencimento() {

        Timestamp dataLimiteQueClienteAceitaVencimento = TimeUtils.dataAdd(TimeUtils.getNow(), 0, 5);

        assertEquals(dataLimiteQueClienteAceitaVencimento, TimeUtils.getNow());


    }




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

        final int AD_DIAS_VENC_ITEM = 0;

        Timestamp dtVencItem = TimeUtils.buildTimestamp("01/03/2022");

        System.out.println(dtVencItem);

        System.out.println(TimeUtils.dataAdd(TimeUtils.getNow(), AD_DIAS_VENC_ITEM, 5));

       // System.out.println(TimeUtils.getFinalPeriodo(TimeUtils.getNow(), AD_DIAS_VENC_ITEM));
        System.out.println(TimeUtils.getTimeOrZero(null));
        System.out.println(TimeUtils.getValueOrNow(null));
        // Se o vencimento do item for menor q
        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getTimeOrZero(null), TimeUtils.dataAdd(TimeUtils.getNow(), AD_DIAS_VENC_ITEM, 5)));


        System.out.println("4-5".contains(StringUtils.getNullAsEmpty(null)));


        System.out.println(StringUtils.getNullAsEmpty("8").isEmpty());
        System.out.println(StringUtils.getNullAsEmpty(null).isEmpty());

        Set<String> especies = new HashSet<String>();

        especies.add(null);
        especies.add(null);

        especies.add("Teste Especie");
        System.out.println(especies.size());

        especies.forEach(System.out::println);

        //System.out.println(especies.stream().findFirst().get());

        //System.out.println(BigDecimal.valueOf(3010005).compareTo(BigDecimal.valueOf(3010008)) != 0);
        //System.out.println(TimeUtils.compareOnlyDates(null, TimeUtils.getNow()));
        //System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getNow(),null));



















    }
}
