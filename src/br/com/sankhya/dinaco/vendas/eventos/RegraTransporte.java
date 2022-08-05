package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

public class RegraTransporte implements EventoProgramavelJava {


    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
            String mensagem = "";
            mensagem = mensagem.concat(CabecalhoNota.verificaRedespacho(cabVO));
            // Verifica se TOP obriga transportadora (AD_OBRIGATRANSP = 'S') e Parceiro Transportadora não preenchido
            mensagem = mensagem.concat(CabecalhoNota.verificaTransportadoraObrigatoria(cabVO));
            if (!mensagem.isEmpty()) {
                mensagem = mensagem.concat("\nVerifique a aba Transporte.");
                throw new MGEModelException(mensagem);
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
