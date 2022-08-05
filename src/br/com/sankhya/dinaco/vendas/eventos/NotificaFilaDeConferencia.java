package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.timimob.model.utils.AvisoSistemaHelper;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

public class NotificaFilaDeConferencia implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

        final boolean isModifingLIBCONF = persistenceEvent.getModifingFields().isModifing("LIBCONF");
        final boolean liberadoParaConferencia = "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("LIBCONF")));

        if(isModifingLIBCONF && liberadoParaConferencia) {
            Collection<AvisoSistemaHelper.Destinatario> destinatarios = new ArrayList<>();
            destinatarios.add(new AvisoSistemaHelper.Destinatario(BigDecimal.valueOf(15), AvisoSistemaHelper.Tipo.GRUPO)); // Grupo de Logística

            AvisoSistemaHelper.enviarAviso(BigDecimal.valueOf(2), destinatarios, "Fila de Conferência", String.format("Pedido Nro. Único %s foi enviado para fila de conferência.", cabVO.asBigDecimalOrZero("NUNOTA")), null);
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
