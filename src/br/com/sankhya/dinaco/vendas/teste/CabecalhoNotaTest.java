package br.com.sankhya.dinaco.vendas.teste;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import com.sankhya.util.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CabecalhoNotaTest {

    @Test
    void ehPedido() {
        final boolean sim = CabecalhoNota.ehPedidoOuVenda("P");

        assertTrue(sim);
    }

    @Test
    void ehVenda() {
        final boolean sim = CabecalhoNota.ehPedidoOuVenda("V");

        assertTrue(sim);
    }

    @Test
    void ehVazio() {
        final boolean sim = CabecalhoNota.ehPedidoOuVenda("");

        assertTrue(sim);
    }

    @Test
    void ehVNull() {
        final boolean sim = CabecalhoNota.ehPedidoOuVenda(StringUtils.getNullAsEmpty(null));

        assertTrue(sim);
    }
}