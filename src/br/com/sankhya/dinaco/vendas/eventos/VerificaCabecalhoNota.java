package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;


import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.*;
import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehCompra;
import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehVenda;

public class VerificaCabecalhoNota implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isModifyingFormaEntrega = persistenceEvent.getModifingFields().isModifing("AD_FORMAENTREGA");
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);


        if (isModifyingFormaEntrega) verificaFormaEntrega(cabVO);

        // Na confirmação da nota verifica se a TOP e Parceiro exigem OC.
        // Se OC na nota não estiver preenchido, impede confirmação da nota.
        if (isConfirmandoNota && CabecalhoNota.exigeOC(cabVO) && StringUtils.getEmptyAsNull(cabVO.asString("NUMPEDIDO2")) == null) {
            throw new MGEModelException("TOP e Parceiro exigem preenchimento do Nro. OC.");
        }

    }




    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        String tipMov = cabVO.asString("TIPMOV");
        BigDecimal codCenCusDoParceiro = Parceiro.getCodCenCus(cabVO.getProperty("CODPARC"));
        BigDecimal codNatdoParceiro = Parceiro.getCodNat(cabVO.getProperty("CODPARC"));

        // Preeenche com Centro de Custo do Parceiro (TGFPAR.AD_CODCENCUS)
        // Se TIPMOV in ('O','C','E','P','V', 'D')
        if ((ehCompra(tipMov) || ehVenda(tipMov))) {
            if (!BigDecimalUtil.isNullOrZero(codCenCusDoParceiro)) updateCodCenCus(cabVO);
            if (!BigDecimalUtil.isNullOrZero(codNatdoParceiro)) updateCodNat(cabVO);
        }
        verificaFormaEntrega(cabVO);
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
