package br.com.sankhya.dinaco.vendas.acoes;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;

public class VerificaUltimoPrecoTabela implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhas = contextoAcao.getLinhas();

        for (Registro linha: linhas) {


            DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, linha.getCampo("NUNOTA"));
            DynamicVO produtoVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, linha.getCampo("CODPROD"));
            final boolean validaUltimoPrecoVenda  = "S".equals(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_VALIDAULTPRE"));
            final BigDecimal ultimoPrecoVendaNFe = CabecalhoNota.ultimoPrecoVendaNFe((BigDecimal) linha.getCampo("NUNOTA"), cabVO.asBigDecimalOrZero("CODPARC"));
            final boolean ultimoPrecoMaiorQuePrecodoItem = ultimoPrecoVendaNFe.compareTo((BigDecimal) linha.getCampo("VLRUNITMOE")) > 0 && !BigDecimalUtil.isNullOrZero(ultimoPrecoVendaNFe);

            // Verifica ultimo preço de tabela VERIFICAR SE SERA NECESSARIO
           BigDecimal valorVendaTabela = ComercialUtils.obtemPreco(cabVO.asBigDecimalOrZero("CODEMP"), cabVO.asBigDecimalOrZero("CODPARC"),
                   null,
                   null,
                   (BigDecimal) linha.getCampo("CODPROD"),
                   (BigDecimal) linha.getCampo("CODLOCALORIG"),
                    TimeUtils.getNow(),
                    null,
                    cabVO.asString("TIPMOV"),
                    null).getValorVenda();

            contextoAcao.mostraErro("Valor de venda da tabela: " +valorVendaTabela);

        }



    }
}
