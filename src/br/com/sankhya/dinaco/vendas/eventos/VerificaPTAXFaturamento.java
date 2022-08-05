package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.ItemNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.TipoOperacaoVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.sql.Timestamp;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.verificaPTAX;

/**
 * Evento no CompraVendaVariosPedido (TGFVAR)
 * Verifica as regras do PTAX
 */
public class VerificaPTAXFaturamento implements EventoProgramavelJava {

    private final JdbcWrapper jdbc = null;
    JapeSession.SessionHandle hnd = null;

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
        DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA,varVO.asBigDecimalOrZero("NUNOTA"));

        DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

        final boolean topPtaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));

        if (topPtaxDiaAnterior) {
            // Adiciona 1 segundo a Dt. Faturamento para forçar a chamado do evento Verifica PTAX na TGFCAB
            Timestamp dtFatur = TimeUtils.getValueOrNow(cabVO.asTimestamp("DTFATUR"));
            cabVO.setProperty("DTFATUR", TimeUtils.dataAdd(dtFatur,1,13));
            EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);
        }
    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
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
