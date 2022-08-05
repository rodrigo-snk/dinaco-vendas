package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.Estoque;
import br.com.sankhya.dinaco.vendas.modelo.Parceiro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

import static br.com.sankhya.dinaco.vendas.modelo.Financeiro.liberacaoLimite;


public class RegraLotes implements Regra {

    boolean incluindoAlterandoItem = JapeSession.getPropertyAsBoolean("ItemNota.incluindo.alterando.pela.central", false);

    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
            verificaValidade(itemVO);
            verificaLote(itemVO);
            String mensagem = verificaPrazoVencimento(itemVO);

            if (!mensagem.isEmpty()) contextoRegra.getBarramentoRegra().addMensagem(mensagem);
        }

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        final boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");
        final BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();


        if (isConfirmandoNota) {
            DynamicVO cabVO = contextoRegra.getPrePersistEntityState().getNewVO();
            Collection<ItemNotaVO> itensVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", cabVO.asBigDecimalOrZero("NUNOTA")), ItemNotaVO.class);
            final boolean topVerificaFEFO = "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_FEFO")));

            String observacao = "";
            String observacao2 = "";
            String observacaoFEFO = "";

            for (ItemNotaVO itemVO: itensVO) {

                final String controle = getControle(itemVO.asString("CONTROLE"));
                if (itemVO.containsProperty("AD_OBSFEFO")) {
                    observacaoFEFO = itemVO.asString("AD_OBSFEFO");
                }
                final BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
                final BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
                final BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");


                final boolean quebraFEFO = "S".equalsIgnoreCase(StringUtils.getNullAsEmpty(itemVO.getProperty("AD_QUEBRAFEFO")));
                final Timestamp validadeLote;
                final Timestamp menorValidade;
                final Timestamp dataLimiteQueParceiroAceitaVencimento = Parceiro.dataLimiteQueClienteAceitaVencimento(cabVO.asBigDecimalOrZero("CODPARC"));

                // Se TOP e Quebra FEFO no item estiverem marcados na regra
                if (topVerificaFEFO && quebraFEFO /* && codLocal &&  */) {

                    if (!controle.equals(" ")) {
                        //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));
                        validadeLote = Estoque.getValidadeLote(codProd,codEmp, codLocal,controle);
                        menorValidade = Estoque.getMenorValidade(codProd, codEmp, codLocal);

                        // Quando tiver um outro lote de validade menor, preenche observação da liberação
                        if (TimeUtils.compareOnlyDates(validadeLote, menorValidade) > 0) {
                        observacao = observacao.concat("Produto: " + itemVO.getCODPROD() + " / Lote: " +itemVO.getCONTROLE()+ " / Validade: " +TimeUtils.formataDDMMYYYY(validadeLote) + " Observação FEFO: " +observacaoFEFO+";");
                        }

                        // Quando tiver um lote com validade inferior ao limtite estipulado no parceiro, preenche observação da liberação
                        if (validadeLote != null && TimeUtils.compareOnlyDates(validadeLote, dataLimiteQueParceiroAceitaVencimento) < 0) {
                            //contextoRegra.getBarramentoRegra().addMensagem("Parceiro não aceita produtos com validade menor que " + DIASVENCITEM + " dias.");
                            observacao2 = observacao2.concat("Produto com validade menor que aceita pelo cliente: " + itemVO.getCODPROD() + " / Lote: " +itemVO.getCONTROLE()+ " / Validade: " +TimeUtils.formataDDMMYYYY(validadeLote) + ";");
                        }
                    }
                }
            }


            // Quando tiver um outro lote de validade menor, exige liberação
            if (!observacao.equals("")) {
                liberacaoLimite(contextoRegra, codUsuarioLogado, cabVO, observacao, 1001);
            } else {
                LiberacaoAlcadaHelper.apagaSolicitacoEvento(1001, cabVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
            }

            // Quando tiver um lote com validade inferior ao limtite estipulado no parceiro exige liberação
            if (!observacao2.equals("")) {
                liberacaoLimite(contextoRegra, codUsuarioLogado, cabVO, observacao, 1008);
            } else {
                LiberacaoAlcadaHelper.apagaSolicitacoEvento(1008, cabVO.asBigDecimalOrZero("NUNOTA"), "TGFCAB", null);
            }

        }

        if (isItemNota) {
            //final boolean isModifyingQuebraFEFO = contextoRegra.getPrePersistEntityState().getModifingFields().isModifing("AD_QUEBRAFEFO");
            //final boolean isModifyingControle = contextoRegra.getPrePersistEntityState().getModifingFields().isModifing("CONTROLE");

           // if (isModifyingQuebraFEFO || isModifyingControle) {
                verificaLote(contextoRegra.getPrePersistEntityState().getNewVO());
          //  }
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

        final boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            Collection<DynamicVO> itens = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA,"this.NUNOTA = ?", contextoRegra.getPrePersistEntityState().getOldVO().asBigDecimal("NUNOTA")));

            for (DynamicVO vo : itens) {
                verificaLote(vo);
            }

        }

    }

    public static String getControle(String controle) {
        return (StringUtils.getEmptyAsNull(controle) == null) ? " " : controle.trim();
    }

    private void verificaLote(DynamicVO itemVO) throws Exception {
        DynamicVO cabVO = itemVO.asDymamicVO("CabecalhoNota");

        final boolean topVerificaFEFO = "S".equals(StringUtils.getNullAsEmpty(TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER")).asString("AD_FEFO")));
        final boolean localVerificaFEFO = "S".equals(itemVO.asDymamicVO("LocalFinanceiro").asString("AD_FEFO"));
        final boolean quebraFEFO = "S".equals(StringUtils.getNullAsEmpty(itemVO.getProperty("AD_QUEBRAFEFO")));
        String controle = getControle(itemVO.asString("CONTROLE"));

        // Se TOP e Local estiverem marcados na regra
        if (topVerificaFEFO && localVerificaFEFO && !quebraFEFO && !controle.equals(" ")) {

            BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
            BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
            BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");
            //DynamicVO localVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.LOCAL_FINANCEIRO, codLocal);
            Timestamp validadeLote;
            Timestamp menorValidade;

            //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));
            validadeLote = Estoque.getValidadeLote(codProd,codEmp, codLocal,controle);
            menorValidade = Estoque.getMenorValidade(codProd, codEmp, codLocal);

            // Quando tiver um outro lote de validade menor
            if (menorValidade != null && TimeUtils.compareOnlyDates(validadeLote, menorValidade) > 0) {
                throw new MGEModelException(String.format("Existe um lote com validade menor: %s. Quebra FEFO precisa estar marcado.", TimeUtils.formataDDMMYYYY(menorValidade)));
            }

            if (!verificaPrazoVencimento(itemVO).isEmpty()) {
                BigDecimal prazoVencimentoItens = Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC")).asBigDecimalOrZero("AD_PRAZOVENCITENS");
                throw new MGEModelException("Cliente não aceita produtos com validade menor que " + prazoVencimentoItens + " dias. Quebra FEFO precisa estar marcado.");
            }
        }
    }

    private void verificaValidade(DynamicVO itemVO) throws Exception {
        DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));
        //DynamicVO parceiroVO = Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC"));

        if (ComercialUtils.ehPedidoOuVenda(cabVO.asString("TIPMOV"))) {
            String controle = getControle(itemVO.asString("CONTROLE"));
            BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
            BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
            BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");

            Timestamp validadeLote = Estoque.getValidadeLote(codProd,codEmp, codLocal,controle);

            if (validadeLote != null && TimeUtils.compareOnlyDates(validadeLote, TimeUtils.getNow()) < 0) {
                throw new MGEModelException(String.format("Validade do lote %s expirou em %s.", controle, TimeUtils.formataDDMMYYYY(validadeLote)));
            }
        }
    }

    private String verificaPrazoVencimento(DynamicVO itemVO) throws Exception {
        DynamicVO cabVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemVO.asBigDecimalOrZero("NUNOTA"));
        DynamicVO parceiroVO = Parceiro.getParceiroByPK(cabVO.asBigDecimalOrZero("CODPARC"));
        final int prazoVencimentoItens = Parceiro.prazoVencimentoItens(parceiroVO.asBigDecimalOrZero("CODPARC"));

        String controle = getControle(itemVO.asString("CONTROLE"));
        BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
        BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
        BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODLOCALORIG");

        Timestamp validadeLote = Estoque.getValidadeLote(codProd,codEmp, codLocal,controle);
        Timestamp dataLimiteQueClienteAceitaVencimento = TimeUtils.dataAdd(TimeUtils.getNow(), prazoVencimentoItens, 5);

        if (validadeLote != null && TimeUtils.compareOnlyDates(dataLimiteQueClienteAceitaVencimento, validadeLote) > 0) {
            return "Parceiro " +parceiroVO.asString("NOMEPARC")+" não aceita produtos com validade menor que " + prazoVencimentoItens + " dias.";
        }

        return "";
    }

}
