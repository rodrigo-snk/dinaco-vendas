package br.com.sankhya.dinaco.vendas.regras;

import br.com.sankhya.dinaco.vendas.modelo.Estoque;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.*;
import br.com.sankhya.modelcore.comercial.regras.EstoqueInsuficiente;
import br.com.sankhya.modelcore.comercial.regras.EstoqueItem;
import br.com.sankhya.modelcore.comercial.util.LoteAutomaticoHelper;
import br.com.sankhya.modelcore.comercial.util.LoteInfoUtil;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.EstoqueVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.totalcross.utils.StringUtils;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.LinkedList;

public class RegraLotes implements Regra {
    @Override
    public void beforeInsert(ContextoRegra contextoRegra) throws Exception {
        boolean isItemNota = contextoRegra.getPrePersistEntityState().getDao().getEntityName().equals("ItemNota");

        if (isItemNota) {
            DynamicVO itemVO = contextoRegra.getPrePersistEntityState().getNewVO();
            final BigDecimal nuNota = itemVO.asBigDecimalOrZero("NUNOTA");
            DynamicVO notaVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
            DynamicVO topVO = TipoOperacaoUtils.getTopVO(notaVO.asBigDecimalOrZero("CODTIPOPER"));
            DynamicVO produtoVO = (DynamicVO)EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemVO.asBigDecimalOrZero("CODPROD"));

            String controle = getControle(itemVO.asString("CONTROLE"));
            BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
            BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
            BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODEMP");
            boolean quebraFEFO = itemVO.asString("AD_QUEBRAFEFO").equalsIgnoreCase("S");
            DynamicVO localVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().findEntityByPrimaryKeyAsVO(DynamicEntityNames.LOCAL_FINANCEIRO, codLocal);
            Timestamp validadeLote;
            Timestamp menorValidade;

            // Se TOP e Local estiverem marcados na regra
            if (topVO.getProperty("AD_FEFO").equals("S") && !quebraFEFO /* && codLocal &&  */) {

                if (!controle.equals(" ")) {
                    //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));
                    validadeLote = Estoque.getValidade(codProd,codEmp, codLocal,controle);
                    menorValidade = Estoque.getValidadeMinima(codProd, codEmp);
                    assert menorValidade != null;
                    assert validadeLote != null;
                    // Quando tiver um outro lote de validade menor
                    if (TimeUtils.getDifference(validadeLote, menorValidade, false) > 0) {
                        contextoRegra.getBarramentoRegra().addMensagem("Existe um outro lote com validade menor: " + TimeUtils.formataDDMMYYYY(menorValidade));
                    }

                    contextoRegra.getBarramentoRegra().addMensagem("Tem lote automático ligado? " + LoteAutomaticoHelper.temLoteAutomaticoLigado(notaVO, itemVO));
                    contextoRegra.getBarramentoRegra().addMensagem("Local Estoque: " + localVO.getProperty("DESCRLOCAL"));
                    EstoqueVO estoqueVO = (EstoqueVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance(DynamicEntityNames.ESTOQUE, EstoqueVO.class);


                }

            }



        }

    }

    @Override
    public void beforeUpdate(ContextoRegra contextoRegra) throws Exception {

        boolean isConfirmandoNota = JapeSession.getPropertyAsBoolean("CabecalhoNota.confirmando.nota", false);
        BigDecimal codUsuarioLogado = AuthenticationInfo.getCurrent().getUserID();



        if (isConfirmandoNota) {
            DynamicVO notaVO = contextoRegra.getPrePersistEntityState().getNewVO();
            Collection<ItemNotaVO> itensVO = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", notaVO.asBigDecimalOrZero("NUNOTA")), ItemNotaVO.class);


            DynamicVO topVO = TipoOperacaoUtils.getTopVO(notaVO.asBigDecimalOrZero("CODTIPOPER"));

            for (ItemNotaVO itemVO: itensVO) {


                String controle = getControle(itemVO.asString("CONTROLE"));
                BigDecimal codEmp = itemVO.asBigDecimalOrZero("CODEMP");
                BigDecimal codProd = itemVO.asBigDecimalOrZero("CODPROD");
                BigDecimal codLocal = itemVO.asBigDecimalOrZero("CODEMP");
                boolean quebraFEFO = itemVO.asString("AD_QUEBRAFEFO").equalsIgnoreCase("S");
                Timestamp validadeLote;
                Timestamp menorValidade;

                // Se TOP e Local estiverem marcados na regra
                if (topVO.getProperty("AD_FEFO").equals("S") && !quebraFEFO /* && codLocal &&  */) {

                    if (!controle.equals(" ")) {
                        //throw new MGEModelException(String.valueOf(Estoque.getValidade(codProd,codEmp, codLocal,controle)));
                        validadeLote = Estoque.getValidade(codProd,codEmp, codLocal,controle);
                        menorValidade = Estoque.getValidadeMinima(codProd, codEmp);
                        assert menorValidade != null;
                        assert validadeLote != null;

                        // Quando tiver um outro lote de validade menor
                        if (TimeUtils.getDifference(validadeLote, menorValidade, false) > 0) {

                            LiberacaoSolicitada ls = new LiberacaoSolicitada(itemVO.getNUNOTA(),"TGFITE", 1001, BigDecimal.ZERO);
                            ls.setCodCenCus(BigDecimal.ZERO);
                            ls.setSolicitante(codUsuarioLogado);
                            ls.setLiberador(BigDecimal.ZERO);
                            ls.setVlrAtual(itemVO.getVLRTOT());
                            ls.setVlrTotal(itemVO.getVLRTOT());
                            ls.setCodTipOper(notaVO.asBigDecimalOrZero("CODTIPOPER"));
                            ls.setVlrLimite(BigDecimal.ZERO);
                            ls.setDhSolicitacao(TimeUtils.getNow());


                            if (LiberacaoAlcadaHelper.buscaTSILIB(itemVO.getNUNOTA(), "TGFCAB", 1001, BigDecimal.ZERO) != null) {
                                LiberacaoAlcadaHelper.processarLiberacao(ls);
                                contextoRegra.getBarramentoRegra().addLiberacaoSolicitada(ls);
                                break;
                            }
                        }
                    }
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

    public static String getEmptyAsNull(String s) {
        return ((s == null) || (s.trim().length() == 0)) ? null : s.trim();
    }

    public static String getControle(String controle) {
        return (getEmptyAsNull(controle) == null) ? " " : controle.trim();
    }
}
