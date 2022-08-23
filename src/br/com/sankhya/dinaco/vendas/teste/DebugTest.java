package br.com.sankhya.dinaco.vendas.teste;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Financeiro;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.modelcore.MGEModelException;
import bsh.StringUtil;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;


public class DebugTest {


    @org.junit.jupiter.api.Test
    void setDataLimiteQueClienteAceitaVencimento() {

        Timestamp dataLimiteQueClienteAceitaVencimento = TimeUtils.dataAdd(TimeUtils.getNow(), 0, 5);

        Assertions.assertEquals(dataLimiteQueClienteAceitaVencimento, TimeUtils.getNow());
    }


    public static void main(String[] args) throws Exception {
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

        int diaDoVencimento = data.getDayOfWeek().getValue();
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

        System.out.println("Data de vencimento: "+ data);
        System.out.println("Nova data de vencimento: "+ data.plusDays(calculoDias));

        System.out.println("Nova data de vencimento proximo: "+ data.plusMonths(1).withDayOfMonth(5));


        final int AD_DIAS_VENC_ITEM = 0;

        LocalDate dtVencItem = TimeUtils.buildTimestamp("12/03/2022").toLocalDateTime().toLocalDate();

        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.buildTimestamp("12/03/2022"), TimeUtils.getNow()));


        while (Financeiro.fimDeSemana(dtVencItem)) {
            dtVencItem = dtVencItem.plusDays(1);
            System.out.println("VENC: "+dtVencItem);
        }


        System.out.println(TimeUtils.dataAdd(TimeUtils.getNow(), AD_DIAS_VENC_ITEM, 5));

       // System.out.println(TimeUtils.getFinalPeriodo(TimeUtils.getNow(), AD_DIAS_VENC_ITEM));
        System.out.println(TimeUtils.getTimeOrZero(null));
        System.out.println(TimeUtils.getValueOrNow(null));
        // Se o vencimento do item for menor q
        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getTimeOrZero(null), TimeUtils.dataAdd(TimeUtils.getNow(), AD_DIAS_VENC_ITEM, 5)));


        System.out.println("4-5".contains(StringUtils.getNullAsEmpty(null)));


        System.out.println(StringUtils.getNullAsEmpty("8").isEmpty());
        System.out.println(StringUtils.getNullAsEmpty(null).isEmpty());

        Set<String> especies = new HashSet<>();

        //especies.add(null);
        especies.add(null);

        //especies.add("Teste Especie");
        System.out.println(especies.size());

        especies.forEach(System.out::println);


        Timestamp hoje = TimeUtils.getNow();

        final Timestamp ontem = TimeUtils.dataAdd(hoje,-1, 5);
        final Timestamp amanha = TimeUtils.dataAdd(hoje,1, 5);

        final Timestamp mesPassado = TimeUtils.dataAdd(hoje,-1, 5);

        final Timestamp ultimoDiaMesPassado = TimeUtils.getUltimoDiaDoMesRefAnterior(hoje);



        System.out.println("Ontem: " +ontem + "Final de semana: " +TimeUtils.isWeekend(ontem.getTime()));
        System.out.println("Amanha: " +amanha + "Final de semana: " +TimeUtils.isWeekend(amanha.getTime()));


        Timestamp primeiroDiaMes = TimeUtils.getMonthStart(hoje);
        Timestamp ultimoDiaMes = TimeUtils.getMonthEnd(hoje);

        System.out.println(primeiroDiaMes);
        System.out.println(ultimoDiaMes);
        System.out.println("Ultimo dia do mês passado: " +ultimoDiaMesPassado);

        String pkRegistro = "3_6697_3_10_0_P_AD_TGFEST";
        System.out.println(pkRegistro);
        pkRegistro = pkRegistro.replace("AD_TGFEST", "Estoque");
        System.out.println(pkRegistro);

        BigDecimal precoTabela = BigDecimal.TEN;
/*
        BigDecimal QUATROPORCENTO = BigDecimal.valueOf(-0.04);
        BigDecimal SETEPORCENTO = BigDecimal.valueOf(-0.07);
        BigDecimal DEZOITOPORCENTO = BigDecimal.valueOf(-0.18);

        // Vlr. Venda 7% = (((Vlr. Venda 12%/1,03) * 0,7875) / 0,8375 ) * 1,03
        BigDecimal expressaoCalculo = precoTabela.divide(BigDecimal.valueOf(1.03), MathContext.DECIMAL32).multiply(BigDecimal.valueOf(0.7875));
        BigDecimal precoTabela7p = expressaoCalculo.divide(BigDecimal.valueOf(0.9075).add(QUATROPORCENTO), MathContext.DECIMAL32).multiply(BigDecimal.valueOf(1.03));
        // Vlr. Venda 4% = (((Vlr. Venda 12%/1,03) * 0,7875) /  0,8675 ) * 1,03
        BigDecimal precoTabela4p = expressaoCalculo.divide(BigDecimal.valueOf(0.9075).add(SETEPORCENTO), MathContext.DECIMAL32).multiply(BigDecimal.valueOf(1.03));
        BigDecimal precoTabela18p = expressaoCalculo.divide(BigDecimal.valueOf(0.9075).add(DEZOITOPORCENTO), MathContext.DECIMAL32).multiply(BigDecimal.valueOf(1.03));*/


        BigDecimal QUATROPORCENTO = BigDecimal.valueOf(0.04);
        BigDecimal SETEPORCENTO = BigDecimal.valueOf(0.07);
        BigDecimal NOVEVINTECINCOPORCENTO = BigDecimal.valueOf(0.0925);
        BigDecimal DOZEPORCENTO = BigDecimal.valueOf(0.12);
        BigDecimal DEZOITOPORCENTO = BigDecimal.valueOf(0.18);

        BigDecimal custo = BigDecimal.valueOf(0.03);
        BigDecimal margemMin = BigDecimal.valueOf(0.235);
        BigDecimal lcProd = BigDecimal.valueOf(8.08);


        BigDecimal fator7 = BigDecimal.ONE.subtract(SETEPORCENTO).subtract(NOVEVINTECINCOPORCENTO).subtract(custo).subtract(margemMin);
        BigDecimal fator4 = BigDecimal.ONE.subtract(QUATROPORCENTO).subtract(NOVEVINTECINCOPORCENTO).subtract(custo).subtract(margemMin);
        BigDecimal fator12 = BigDecimal.ONE.subtract(DOZEPORCENTO).subtract(NOVEVINTECINCOPORCENTO).subtract(custo).subtract(margemMin);

        System.out.println(fator7);
        System.out.println(fator7.multiply(BigDecimal.valueOf(1.03)));


        System.out.println("NETMIN7: " +lcProd.divide(fator7, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(1.03)));
        System.out.println("NETMIN4: " +lcProd.divide(fator4, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(1.03)));
        System.out.println("NETMIN12: " +lcProd.divide(fator12, MathContext.DECIMAL128).multiply(BigDecimal.valueOf(1.03)));

        /*System.out.println(precoTabela7p);
        System.out.println(precoTabela4p);
        System.out.println(precoTabela18p);*/

        System.out.println(CabecalhoNota.ehPedidoVenda("P"));
        System.out.println(CabecalhoNota.ehPedidoVenda("V"));

        System.out.println(StringUtils.getNullAsEmpty("").isEmpty());
        System.out.println(StringUtils.getNullAsEmpty(" ").trim().isEmpty());

        BigDecimal testeTab = BigDecimal.valueOf(16.523479);

        System.out.println(BigDecimalUtil.getRounded(testeTab, 2));

        System.out.println(StringUtils.isEmpty(null));

        System.out.println("Envio da NF-e anskdasdajb".contains("Envio da NF-e"));




        String assunto = "Envio da NF-e - N.Doc 000012398 - Série 1";

        assunto = assunto.replace("Envio da NF-e -", "Envio da NF-e com produto perigoso -");

        System.out.println(assunto);


        BigDecimal qtdNeg = BigDecimal.valueOf(9.1);

        BigDecimal estoque = BigDecimal.valueOf(20);

        BigDecimal reservado = BigDecimal.valueOf(11.6);

        BigDecimal disponivel = estoque.subtract(reservado);

        final boolean naoTemEstoque = disponivel.subtract(qtdNeg).compareTo(BigDecimal.ZERO) < 0;

        System.out.println("Não tem estoque disponível para este item neste local.\n Disponível: " + disponivel);


        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getNow(), TimeUtils.dataAdd(TimeUtils.getNow(),-1,5)));

        System.out.println(String.format("Existe um lote com validade menor: %s. Quebra FEFO precisa estar marcado.", TimeUtils.formataDDMMYYYY(null)));

        System.out.println("AGORA " +TimeUtils.getNow() + " TESTE DTFATUR SEGUNDOS: " + TimeUtils.dataAdd(TimeUtils.getNow(),1,13));

        String chave = "3_810_312_27_0_P_Estoque";
        String[] estoqueString = chave.split("_");

        System.out.println("Produto: " +estoqueString[1]);
        System.out.println("Controle: " +estoqueString[3]);



        System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getNow(), null));

        final boolean faturamentoFuturo = TimeUtils.compareOnlyDates(TimeUtils.getNow(), TimeUtils.getMonthEnd(TimeUtils.getNow())) < 0;


        BigDecimal decimais = BigDecimal.valueOf(10.8675768055400);
        System.out.println(decimais.toString());
        System.out.println(BigDecimalUtil.valueOf(decimais.toString()).doubleValue());

        System.out.println(BigDecimalUtil.toCurrency(decimais));









        //System.out.println(especies.stream().findFirst().get());

        //System.out.println(BigDecimal.valueOf(3010005).compareTo(BigDecimal.valueOf(3010008)) != 0);
        //System.out.println(TimeUtils.compareOnlyDates(null, TimeUtils.getNow()));
        //System.out.println(TimeUtils.compareOnlyDates(TimeUtils.getNow(),null));



















    }
}
