package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.LiberacaoAlcadaHelper;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

public class RegraFrete implements Regra {
    final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
    final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();

            DynamicVO topVO = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

            final boolean topVerificaFreteMinimo  = "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_FRETEMIN")));
            final boolean entregaAmostra = topVO.containsProperty("AD_ENTREGAAMOSTRA") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_ENTREGAAMOSTRA")));
            final boolean ignoraFormaEntrega = DataDictionaryUtils.campoExisteEmTabela("AD_IGNORAFORMAENTREGA", "TGFTOP") && "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(topVO.asString("AD_IGNORAFORMAENTREGA")));

            if (StringUtils.getNullAsEmpty(cabVO.asString("AD_FORMAENTREGA")).isEmpty() && ComercialUtils.ehVenda(cabVO.asString("TIPMOV")) && !entregaAmostra  && !ignoraFormaEntrega)
                throw new MGEModelException("Preenchimento da Forma de Entrega é obrigatório.");

            final boolean semFrete = "S".equals(StringUtils.getNullAsEmpty(cabVO.asString("CIF_FOB")));

            if (topVerificaFreteMinimo && !semFrete) {
                DynamicVO empVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA_FINANCEIRO, cabVO.asBigDecimal("CODEMP"));
                final BigDecimal vlrNota = cabVO.asBigDecimalOrZero("VLRNOTA");
                final BigDecimal vlrFreteNota = cabVO.asBigDecimalOrZero("VLRFRETE");
                final String observacaoFrete = StringUtils.getNullAsEmpty(cabVO.asString("AD_OBSFRETE"));
                final BigDecimal minimoFrete = BigDecimalUtil.getValueOrZero(empVO.asBigDecimalOrZero("AD_MINFRETE"));
                final BigDecimal vlrFrete = BigDecimalUtil.getValueOrZero(empVO.asBigDecimalOrZero("AD_VLRFRETE"));

                final boolean notaMenorQueMinimoFrete = vlrNota.compareTo(minimoFrete) < 0;
                final boolean freteMenorQueFreteMinimo = vlrFreteNota.compareTo(vlrFrete) < 0;

                //Verifica se valor da nota e valor do frete são menores que o valor mínimo para frete e valor do frete(fixado) nas preferências da Empresa
                if (notaMenorQueMinimoFrete && freteMenorQueFreteMinimo) {
                    //Se observação frete estiver vazia, exige preenchimento.
                    // Caso contrário, cria evento de liberação da nota
                    if (observacaoFrete.isEmpty()) {
                        throw new MGEModelException("Valor da nota menor que "+BigDecimalUtil.formatCurrency(minimoFrete, 2)+", o valor do frete precisa ser de no mínimo "+BigDecimalUtil.formatCurrency(vlrFrete, 2)+". Ajuste o valor do frete ou preencha Observação Frete para enviar liberação para o Financeiro.");
                    }
                    liberacaoLimite(contextoRegra, codUsuarioLogado, cabVO, observacaoFrete, 1003);
                } else {
                    LiberacaoAlcadaHelper.apagaSolicitacoEvento(1003, cabVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
                }
            }
        }

    }

    @Override
    public void beforeDelete(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterUpdate(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void afterDelete(ContextoRegra contextoRegra) throws Exception {

    }
}
