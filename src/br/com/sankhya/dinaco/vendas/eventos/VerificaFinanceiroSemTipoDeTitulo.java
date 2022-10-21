package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import com.sankhya.util.BigDecimalUtil;

import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.getFinanceirosByNunota;

/**
 * Evento criado para o CabecalhoNota (TGFCAB)
 * Verifica se existe algum título no Financeiro com tipo de título 0 - SEM TIPO DE TÍTULO
 */

public class VerificaFinanceiroSemTipoDeTitulo implements EventoProgramavelJava {


    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);


        if (isConfirmandoNota) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            Collection<DynamicVO> finsVO = getFinanceirosByNunota(cabVO.asBigDecimalOrZero("NUNOTA"));

            final boolean tipoTituloZero = finsVO.stream().anyMatch(vo -> BigDecimalUtil.isNullOrZero(vo.asBigDecimalOrZero("CODTIPTIT")));

            if (ComercialUtils.ehCompra(cabVO.asString("TIPMOV")) && tipoTituloZero) {
                throw new MGEModelException("Tipo de título não pode ser 0 - <SEM TIPO DE TITULO> para movimentações geradas de compras. Verifique o Financeiro.");
            }
        }

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

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
