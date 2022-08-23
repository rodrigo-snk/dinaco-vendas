package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.ItemNota;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.*;

/**
 * Evento no CabecalhoNota (TGFCAB)
 * Verifica as regras do PTAX
 */
public class VerificaPTAX implements EventoProgramavelJava {

    private final JdbcWrapper jdbc = null;
    JapeSession.SessionHandle hnd = null;

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        verificaPTAX(cabVO, false, false);

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isModifyingVlrMoeda = persistenceEvent.getModifingFields().isModifing("VLRMOEDA");
        final boolean isModifyingDtFatur = persistenceEvent.getModifingFields().isModifing("DTFATUR");
        final boolean isModifyingParametrosPTAX = persistenceEvent.getModifingFields().isModifing("AD_PTAXFIXO") || persistenceEvent.getModifingFields().isModifing("AD_PTAXMEDIO");
        final boolean ptaxFixoNaoMarcado = "N".equals(cabVO.asString("AD_PTAXFIXO"));
        if (isModifyingDtFatur || isModifyingVlrMoeda || isModifyingParametrosPTAX) {
            verificaPTAX(cabVO, isModifyingVlrMoeda, true);
            ItemNota.atualizaValoresItens(cabVO);
        }
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {


    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();

        final boolean isModifyingVlrMoeda = persistenceEvent.getModifingFields().isModifing("VLRMOEDA");
        final boolean isModifyingDtFatur = persistenceEvent.getModifingFields().isModifing("DTFATUR");
        final boolean isModifyingParametrosPTAX = persistenceEvent.getModifingFields().isModifing("AD_PTAXFIXO") || persistenceEvent.getModifingFields().isModifing("AD_PTAXMEDIO");

        if ((isModifyingDtFatur || isModifyingVlrMoeda || isModifyingParametrosPTAX) && !BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODMOEDA"))) {
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            final boolean topPtaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));
            if (topPtaxDiaAnterior && !BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("VLRNOTA"))) {
                final ImpostosHelpper impostosHelper = new ImpostosHelpper();
                impostosHelper.calcularImpostos(cabVO.asBigDecimalOrZero("NUNOTA"));
                impostosHelper.totalizarNota(cabVO.asBigDecimalOrZero("NUNOTA"));
                CentralFinanceiro financeiro = new CentralFinanceiro();
                financeiro.inicializaNota(cabVO.asBigDecimalOrZero("NUNOTA"));
                financeiro.refazerFinanceiro();
            }
        }
    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) {

    }

}
