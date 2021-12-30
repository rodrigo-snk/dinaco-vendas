package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import com.sankhya.util.StringUtils;


import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehCompra;
import static br.com.sankhya.modelcore.comercial.ComercialUtils.ehVenda;

public class PreencheCodCenCus implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        final boolean isModifyingFormaEntrega = persistenceEvent.getModifingFields().isModifing("AD_FORMAENTREGA");

        if (isModifyingFormaEntrega) verificaFormaEntrega(cabVO);

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        DynamicVO cabVO = (DynamicVO) persistenceEvent.getVo();
        String tipMov = cabVO.asString("TIPMOV");
        // Preeenche com Centro de Custo do Parceiro (TGFPAR.AD_CODCENCUS)
        // Se TIPMOV in ('O','C','E','P','V', 'D')
        if (ehCompra(tipMov) || ehVenda(tipMov)) {
            //cabVO.setProperty("CODCENCUS", Parceiro.getCodCenCus(cabVO.getProperty("CODPARC")));
            //EntityFacadeFactory.getDWFFacade().saveEntity(DynamicEntityNames.CABECALHO_NOTA, (EntityVO) cabVO);
            CabecalhoNota.updateCodCenCus(cabVO);
        }

        verificaFormaEntrega(cabVO);

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }

    private void verificaFormaEntrega(DynamicVO notaVO) throws Exception {

        String formaEntrega = StringUtils.getNullAsEmpty(notaVO.asString("AD_FORMAENTREGA"));

        switch (formaEntrega) {
            //CIF 2-4-5-6-7
            case "2":
            case "6":
            case "7":
                notaVO.setProperty("AD_REDESPACHO", "N");
                notaVO.setProperty("CIF_FOB","C");
                break;
            case "4":
            case "5":
                notaVO.setProperty("AD_REDESPACHO", "S");
                notaVO.setProperty("CIF_FOB","C");
                break;
            case "1": //FOB 1
                notaVO.setProperty("CIF_FOB","F");
                notaVO.setProperty("AD_REDESPACHO", "N");
                break;
            case "3": //Sem Frete
            default:
                notaVO.setProperty("CIF_FOB","S");
                notaVO.setProperty("AD_REDESPACHO", "N");
                break;
        }
        CabecalhoNota.update(notaVO);
    }
}
