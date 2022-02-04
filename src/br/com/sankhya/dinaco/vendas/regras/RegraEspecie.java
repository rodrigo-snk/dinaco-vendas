package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.AtributosRegras;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static br.com.sankhya.dinaco.vendas.modelo.Produto.getEspecie;

public class RegraEspecie implements Regra {

    final boolean incluindoAlterandoItem = JapeSession.getPropertyAsBoolean(AtributosRegras.INC_UPD_ITEM_CENTRAL, false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        if (incluindoAlterandoItem) {
            validaEspecie(contextoRegra);
        }

    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

        if (incluindoAlterandoItem) {
            validaEspecie(contextoRegra);
        }

    }


    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {


    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }

    private void validaEspecie(ContextoRegra contextoRegra) throws Exception {
        DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
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


        if (especies.stream().findFirst().isPresent() && especies.size() > 1) {
            cabVO.setProperty("VOLUME", "Diversos");
        }
        if (especies.stream().findFirst().isPresent() && especies.size() == 1) {
            cabVO.setProperty("VOLUME", especies.stream().findFirst().get());
        }
        CabecalhoNota.updateEspecie(cabVO);
    }
}
