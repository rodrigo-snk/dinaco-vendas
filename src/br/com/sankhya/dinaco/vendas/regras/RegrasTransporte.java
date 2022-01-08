package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.ComercialUtils;
import br.com.sankhya.modelcore.comercial.ContextoRegra;
import br.com.sankhya.modelcore.comercial.Regra;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.tgf.TipoOperacaoVO;

import java.math.BigDecimal;

public class RegrasTransporte implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {
        //final boolean isCabecalhoNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("CabecalhoNota");
        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);

        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final boolean isRedespacho = cabVO.asString("AD_REDESPACHO").equalsIgnoreCase("S");
            final boolean semRedespacho =  cabVO.asBigDecimalOrZero("CODPARCREDESPACHO").compareTo(BigDecimal.ZERO) == 0;
            final boolean semTransportadora =  cabVO.asBigDecimalOrZero("CODPARCTRANSP").compareTo(BigDecimal.ZERO) == 0;
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));
            final boolean obrigaTransportadora = topVO.asString("AD_OBRIGATRANSP").equalsIgnoreCase("S");


            if (isRedespacho && semRedespacho) {
                throw new MGEModelException("Campo redespacho é obrigatório.");
            }

            // Verifica se TOP obriga transportadora (AD_OBRIGATRANSP = 'S') e Parceiro Transportadora não preenchido
            if (obrigaTransportadora && semTransportadora) {
                CabecalhoNota.verificaTransportadoraObrigatoria(cabVO);
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
