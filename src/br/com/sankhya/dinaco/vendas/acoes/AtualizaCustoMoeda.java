package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.TimeUtils;

import java.sql.Timestamp;

import static br.com.sankhya.dinaco.vendas.modelo.Custo.atualizaCustoMoeda;

public class AtualizaCustoMoeda implements AcaoRotinaJava {


    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();


        for(Registro linha: linhas) {
            Object codProd = linha.getCampo("CODPROD");
            Object codEmp = linha.getCampo("CODEMP");
            Object dtAtual = linha.getCampo("DTATUAL");
            Object codLocal = linha.getCampo("CODLOCAL");
            Object controle = linha.getCampo("CONTROLE");
            Object nuNota = linha.getCampo("NUNOTA");
            Object sequencia = linha.getCampo("SEQUENCIA");

            final boolean naoEhDia1Julho2022 = TimeUtils.compareOnlyDates((Timestamp) dtAtual,TimeUtils.buildData(1,6,2022)) != 0;

            //if (true) throw new MGEModelException(codProd.toString() + " " + codEmp.toString() + " " + dtAtual.toString() + " " + codLocal.toString() + " "+ controle.toString() + " " + nuNota.toString() +" " + sequencia.toString());

            EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

            DynamicVO custoVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CUSTO, new Object[] {codProd, codEmp, dtAtual, codLocal, controle, nuNota, sequencia });
            if (naoEhDia1Julho2022) atualizaCustoMoeda(custoVO);
        }
    }
}
