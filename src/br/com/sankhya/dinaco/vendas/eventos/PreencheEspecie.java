package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.SQLUtils;
import com.sankhya.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static br.com.sankhya.dinaco.vendas.modelo.Produto.getEspecie;

public class PreencheEspecie implements EventoProgramavelJava {

    private void validaEspecie(DynamicVO itemVO) throws Exception {
        Collection<ItemNotaVO> itensVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", new Object[] { itemVO.asBigDecimalOrZero("NUNOTA") }), ItemNotaVO.class);
        DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));
        DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

        final boolean preencheEspecieAutomatico = "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PREENCESPAUT")));

        if (preencheEspecieAutomatico) {
            preencheEspecie(itensVO, cabVO);
        }
    }

    private void preencheEspecie(Collection<ItemNotaVO> itensVO, DynamicVO cabVO) throws MGEModelException {
        Set<String> especies = new HashSet<>();

        itensVO.forEach(vo -> {
            try {
                especies.add(getEspecie(vo.asBigDecimalOrZero("CODPROD")));
            } catch (MGEModelException e) {
                e.printStackTrace();
            }
        });

        if (especies.size() > 1) {
            cabVO.setProperty("VOLUME", "Diversos");
        }
        if (especies.size() == 1) {
            String especie = especies.stream().findFirst().get();
            cabVO.setProperty("VOLUME", StringUtils.getEmptyAsNull(especie));
        }
        if (especies.size() == 0) {
            cabVO.setProperty("VOLUME", null);
        }
        CabecalhoNota.updateEspecie(cabVO);
    }

    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
            validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
            validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {
        validaEspecie((DynamicVO) persistenceEvent.getVo());
    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
