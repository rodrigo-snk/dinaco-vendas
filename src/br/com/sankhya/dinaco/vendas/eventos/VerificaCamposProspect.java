package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.ParameterUtils;
import com.sankhya.util.StringUtils;

public class VerificaCamposProspect implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO parcVO = (DynamicVO) persistenceEvent.getVo();

        final boolean naoEhProspect = parcVO.containsProperty("AD_PROSPECT") && "N".equals(StringUtils.getNullAsEmpty(parcVO.asString("AD_PROSPECT")));

        if (naoEhProspect) {
            String[] camposObrigatorios = ((String) ParameterUtils.getParameter("OBRIGAPARC")).split(";");

            StringBuilder mensagem = new StringBuilder();

            mensagem.append("Seguintes campos são obrigatórios quando parceiro não é prospect:\n");

            for (String campo: camposObrigatorios) {

                if (parcVO.containsProperty(campo) && parcVO.getProperty(campo) == null) mensagem.append(campo).append("\n");
            }

            if (mensagem.length() > 65) throw new MGEModelException(mensagem.toString());
        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

        DynamicVO parcVO = (DynamicVO) persistenceEvent.getVo();

        final boolean isModifingProspect = persistenceEvent.getModifingFields().isModifing("AD_PROSPECT");
        final boolean naoEhProspect = parcVO.containsProperty("AD_PROSPECT") && "N".equals(StringUtils.getNullAsEmpty(parcVO.asString("AD_PROSPECT")));

        if (isModifingProspect && naoEhProspect) {
            String[] camposObrigatorios = ((String) ParameterUtils.getParameter("OBRIGAPARC")).split(";");

            StringBuilder mensagem = new StringBuilder();

            mensagem.append("Seguintes campos são obrigatórios quando prospect vira parceiro:\n");

            for (String campo: camposObrigatorios) {

                if (DataDictionaryUtils.campoExisteEmTabela(campo, "TGFPAR") && parcVO.getProperty(campo) == null) mensagem.append(campo).append("\n");
            }

            if (mensagem.length() > 65) throw new MGEModelException(mensagem.toString());
        }



    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {

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
}
