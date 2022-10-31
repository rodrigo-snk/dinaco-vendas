package br.com.sankhya.dinaco.vendas.schactions;

import br.com.sankhya.dwf.controller.util.DynamicUtil;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.Finder;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.getCotacaoDiaAnterior;
import static br.com.sankhya.dinaco.vendas.modelo.Custo.atualizaCustoMoeda;

public class AtualizaCustoMoeda implements ScheduledAction {

    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        try {
            FinderWrapper finder = new FinderWrapper(DynamicEntityNames.CUSTO, "this.DTATUAL > '01/07/2022' and (this.AD_CUSSEMICMSUSD is NULL or this.AD_CUSSEMICMSUSD = 0)");
            finder.setMaxResults(-1);
            Collection<DynamicVO> custos = dwfFacade.findByDynamicFinderAsVO(finder);

            custos.forEach(vo -> {
                try {
                    atualizaCustoMoeda(vo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
