package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.Estoque;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.regras.LiberacaoConfirmacao;
import br.com.sankhya.modelcore.comercial.util.LoteAutomaticoHelper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.tsi.LiberacaoLimiteVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.util.troubleshooting.SKError;
import br.com.sankhya.util.troubleshooting.TSLevel;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;

public class RegraLotes implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {

            verificaLote(contextoRegra);
            verificaValidade(contextoRegra);
        }

    }


    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();


        if (isConfirmandoNota) {
            DynamicVO notaVO = contextoRegra.getPrePersistEntityState().getNewVO();
            Collection<ItemNotaVO> itensVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", notaVO.asBigDecimalOrZero("NUNOTA")), ItemNotaVO.class);

            DynamicVO topVO = TipoOperacaoUtils.getTopVO(notaVO.asBigDecimalOrZero("CODTIPOPER"));

            String observacao = "";

            for (ItemNotaVO itemVO: itensVO) {

                String controle = getControle(itemVO.asString("CONTROLE"));
                BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
                BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
                BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");
                boolean quebraFEFO = StringUtils.getNullAsEmpty(itemVO.getProperty("AD_QUEBRAFEFO")).equalsIgnoreCase("S");
                Timestamp validadeLote;
                Timestamp menorValidade;

                // Se TOP e Local estiverem marcados na regra
                if (topVO.getProperty("AD_FEFO").equals("S") && !quebraFEFO /* && codLocal &&  */) {

                    if (!controle.equals(" ")) {
                        //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));
                        validadeLote = Estoque.getValidade(codProd,codEmp, codLocal,controle);
                        menorValidade = Estoque.getValidadeMinima(codProd, codEmp);

                        // Quando tiver um outro lote de validade menor
                        if (TimeUtils.compareOnlyDates(validadeLote, menorValidade) > 0) {
                            observacao = observacao.concat("Produto: " + itemVO.getCODPROD() + " / Lote: " +itemVO.getCONTROLE()+ " / Validade: " +TimeUtils.formataDDMMYYYY(validadeLote) + ";");
                        }
                    }
                }
            }

            if (!observacao.equals("")) {
                liberacaoLimite(contextoRegra, codUsuarioLogado, notaVO, observacao, 1001);
            } else {
                LiberacaoAlcadaHelper.apagaSolicitacoEvento(1001, notaVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
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

    public static String getControle(String controle) {
        return (StringUtils.getEmptyAsNull(controle) == null) ? " " : controle.trim();
    }

    private void verificaLote(ContextoRegra contextoRegra) throws Exception {
        DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
        final BigDecimal nuNota = itemVO.asBigDecimalOrZero("NUNOTA");
        DynamicVO notaVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
        DynamicVO topVO = TipoOperacaoUtils.getTopVO(notaVO.asBigDecimalOrZero("CODTIPOPER"));
        final boolean topVerificaFEFO = "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_FEFO")));
        DynamicVO produtoVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemVO.asBigDecimalOrZero("CODPROD"));

        String controle = getControle(itemVO.asString("CONTROLE"));
        BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");
        boolean quebraFEFO = StringUtils.getNullAsEmpty(itemVO.getProperty("AD_QUEBRAFEFO")).equalsIgnoreCase("S");
        //DynamicVO localVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.LOCAL_FINANCEIRO, codLocal);
        Timestamp validadeLote;
        Timestamp menorValidade;

        // Se TOP e Local estiverem marcados na regra
        if (topVerificaFEFO && !quebraFEFO /*  && codLocal &&  */) {

            if (!controle.equals(" ")) {
                //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));

                validadeLote = Estoque.getValidade(codProd,codEmp, codLocal,controle);
                menorValidade = Estoque.getValidadeMinima(codProd, codEmp);

                // Quando tiver um outro lote de validade menor
                if (TimeUtils.compareOnlyDates(validadeLote, menorValidade) > 0) {
                    contextoRegra.getBarramentoRegra().addMensagem("Existe um outro lote com validade menor: " + TimeUtils.formataDDMMYYYY(menorValidade));
                }

                //contextoRegra.getBarramentoRegra().addMensagem("Tem lote automático ligado? " + LoteAutomaticoHelper.temLoteAutomaticoLigado(notaVO, itemVO));
                //contextoRegra.getBarramentoRegra().addMensagem("Local Estoque: " + localVO.getProperty("DESCRLOCAL"));
                //EstoqueVO estoqueVO = (EstoqueVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance(DynamicEntityNames.ESTOQUE, EstoqueVO.class);
            }

        }
    }

    private void verificaValidade(ContextoRegra contextoRegra) throws Exception {
        DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
        DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));
        DynamicVO parceiroVO = Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC"));
        final int DIASVENCITEM = Parceiro.diasVencimentoItem(parceiroVO.asBigDecimalOrZero("CODPARC"));

        String controle = getControle(itemVO.asString("CONTROLE"));
        BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");

        Timestamp validadeLote = Estoque.getValidade(codProd,codEmp, codLocal,controle);
        Timestamp dataLimiteQueClienteAceitaVencimento = TimeUtils.dataAdd(TimeUtils.getNow(), DIASVENCITEM, 5);

        if (validadeLote != null && TimeUtils.compareOnlyDates(validadeLote, dataLimiteQueClienteAceitaVencimento) < 0) {
            //contextoRegra.getBarramentoRegra().addMensagem("Parceiro não aceita produtos com validade menor que " + DIASVENCITEM + " dias.");
            throw new MGEModelException("Parceiro " +parceiroVO.asString("NOMEPARC")+" não aceita produtos com validade menor que " + DIASVENCITEM + " dias.");

        }
    }

}
