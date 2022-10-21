package br.com.sankhya.dinaco.vendas.schactions;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Custo.atualizaCustoMoeda;
import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.removeMoeda;

public class RemoveMoedaFinanceiro implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        try {
            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.FINANCEIRO, "this.DHMOV > SYSDATE - 2 and this.CODTIPTIT = 4 and this.CODMOEDA <> 0 and this.RECDESP = 1");
            finder.setMaxResults(-1);
            Collection<DynamicVO> financeiros = dwfFacade.findByDynamicFinderAsVO(finder);

            financeiros.stream().forEach(vo -> {
                try {
                    vo.setProperty("CODMOEDA", BigDecimal.ZERO);
                    vo.setProperty("VLRMOEDA", BigDecimal.ZERO);
                    EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.FINANCEIRO, (EntityVO) vo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
