package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;

import com.sankhya.util.StringUtils;



import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.*;

public class VerificaCabecalhoNota implements EventoProgramavelJava {

    private JdbcWrapper jdbc = null;
    JapeSession.SessionHandle hnd = null;
    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

        verificaCRNaturezaDoParceiro(cabVO);
        verificaPTAX(cabVO);
        verificaFormaEntrega(cabVO);
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isModifyingFormaEntrega = persistenceEvent.getModifingFields().isModifing("AD_FORMAENTREGA");
        final boolean isModifyingVlrMoeda = persistenceEvent.getModifingFields().isModifing("VLRMOEDA");
        final boolean isModifyingParametrosPTAX = persistenceEvent.getModifingFields().isModifing("AD_PTAXFIXO") || persistenceEvent.getModifingFields().isModifing("AD_PTAXMEDIO");


        if (isModifyingFormaEntrega) verificaFormaEntrega(cabVO);
        if (isModifyingVlrMoeda || isModifyingParametrosPTAX) verificaPTAX(cabVO);

        // Na confirmação da nota verifica se a TOP e Parceiro exigem OC.
        // Se OC na nota não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota && exigeOC(cabVO) && StringUtils.getEmptyAsNull(cabVO.asString("NUMPEDIDO2")) == null) {
            throw new MGEModelException("TOP e Parceiro exigem preenchimento do Nro. OC.");
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) {

    }

}
