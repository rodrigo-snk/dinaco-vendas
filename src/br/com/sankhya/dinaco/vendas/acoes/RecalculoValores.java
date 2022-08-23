package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.ItemNota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota.verificaPTAX;

public class RecalculoValores implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        for(Registro linha: linhas) {

            BigDecimal nuNota = (BigDecimal) linha.getCampo("NUNOTA");

            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);

            ItemNota.atualizaValoresItens(cabVO);

            if (!BigDecimalUtil.isNullOrZero(cabVO.asBigDecimal("CODMOEDA"))) {
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


        }
}
