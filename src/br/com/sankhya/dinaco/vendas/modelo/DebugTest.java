package br.com.sankhya.dinaco.vendas.modelo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DebugTest {

    private static List<Integer> getDias(Map<Integer, Boolean> map) {

        List<Integer> result = new LinkedList<>();
            for (Map.Entry<Integer, Boolean> entry : map.entrySet()) {
                if (Objects.equals(entry.getValue(), true)) {
                    result.add(entry.getKey());
                }
                // we can't compare like this, null will throws exception
              /*(if (entry.getValue().equals(value)) {
                  result.add(entry.getKey());
              }*/
            }

        return result;
    }

    public static void main(String[] args) {
        Map<Integer, Boolean> mapDias = new HashMap<>();

        mapDias.put(1, true);
        mapDias.put(2, false);
        mapDias.put(3, true);
        mapDias.put(4, false);
        mapDias.put(5, true);

        //Simular outros dias do mês
        for (int i = 6; i <= 31; i++) {
            if (i != 26) mapDias.put(i, false);
            else mapDias.put(i, true);
        }

        //SimpleDateFormat fmt = new SimpleDateFormat("dd");
        //System.out.println(fmt.format(Timestamp.valueOf(LocalDateTime.now())));

        // increment days by 7
        LocalDate date = LocalDate.now();
        System.out.println("Current Date: " + date);
        date = date.plusDays(7);
        System.out.println("Date after Increment: " + date.withDayOfMonth(11));

        LocalDate data = LocalDate.parse("2021-10-26");
        System.out.println(data.getDayOfWeek());
        System.out.println(data.lengthOfMonth());

        if(!mapDias.get(data.getDayOfMonth())) System.out.println("Parceiro não possui opção de pagamento neste dia!");


        List<Integer> setDias = getDias(mapDias);
        System.out.println(setDias);








      /*  for (Map.Entry<Integer, Boolean> set :
                mapDias.entrySet()) {

            System.out.println(set.getKey() + " = "
                    + set.getValue());

        }
*/

    }
}
