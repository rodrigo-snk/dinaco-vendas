package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class CadastraContato implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        String nomeContato = (String) contextoAcao.getParam("NOMECONTATO");
        Registro[] linhas = contextoAcao.getLinhas();

        if (linhas.length > 1) contextoAcao.mostraErro("Selecione apenas um registro.");

        for (Registro linha : linhas) {
            contextoAcao.setMensagemRetorno(Parceiro.cadastraContato((DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PARCEIRO, linha.getCampo("CODPARC")), nomeContato));
        }

    }
}
