package br.com.sankhya.dinaco.vendas.schactions;

import br.com.sankhya.jape.core.JapeSession;
import org.cuckoo.core.ScheduledAction;
import org.cuckoo.core.ScheduledActionContext;

import static br.com.sankhya.dinaco.vendas.modelo.Estoque.carregaPainelEstoque;

public class RecarregaPainelEstoque implements ScheduledAction {
    @Override
    public void onTime(ScheduledActionContext scheduledActionContext) {

        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();
            hnd.setCanTimeout(false);

            hnd.execWithTX(new JapeSession.TXBlock() {
                public void doWithTx() throws Exception {
                   carregaPainelEstoque();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JapeSession.close(hnd);

        }


    }
}
