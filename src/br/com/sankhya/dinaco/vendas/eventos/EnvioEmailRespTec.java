package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.dinaco.vendas.modelo.CabecalhoNota;
import br.com.sankhya.dinaco.vendas.modelo.Empresa;
import br.com.sankhya.dinaco.vendas.modelo.RegraNegocio;
import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.dwfdata.vo.tmd.MSDFilaMensagemVO;
import br.com.sankhya.modelcore.metadata.DataDictionaryUtils;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.email.FilaMsgUtil;
import br.com.sankhya.pes.model.helpers.EmailHelper;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.CollectionUtils;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;

public class EnvioEmailRespTec implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {
        JapeSession.SessionHandle hnd = null;

        try {
            hnd = JapeSession.open();

            DynamicVO filaVO = (DynamicVO) persistenceEvent.getVo();
            BigDecimal nuNota = filaVO.asBigDecimalOrZero("NUCHAVE");

            if (!BigDecimalUtil.isNullOrZero(nuNota)) {
                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                DynamicVO cabVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, nuNota);
                BigDecimal codTipOper = cabVO.asBigDecimalOrZero("CODTIPOPER");
                BigDecimal codEmp = cabVO.asBigDecimalOrZero("CODEMP");

                final boolean naoFoiEnviadaNFe = StringUtils.getNullAsEmpty(filaVO.asString("ASSUNTO")).contains("Envio da NF-e") && CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.NUCHAVE = ? AND ASSUNTO LIKE '%Envio da NF-e%'", nuNota)));
                final boolean naoFoiEnviadaCancelada = StringUtils.getNullAsEmpty(filaVO.asString("ASSUNTO")).contains("NF-e CANCELADA") && CollectionUtils.isEmpty(dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.FILA_MSG, "this.NUCHAVE = ? AND ASSUNTO LIKE '%NF-e CANCELADA%'", nuNota)));

                DynamicVO empVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.EMPRESA, codEmp);
                String email = empVO.asString("AD_EMAILRESPTECNICO");

                if ((naoFoiEnviadaNFe || naoFoiEnviadaCancelada) && CabecalhoNota.temProdutoPerigoso(nuNota) && RegraNegocio.verificaRegra(BigDecimal.valueOf(13), codTipOper) && !StringUtils.isEmpty(email)) {
                   /* DynamicVO filaNovaVO = filaVO.buildClone();
                    filaNovaVO.setProperty("CODFILA", null);
                    filaNovaVO.setProperty("EMAIL", email);
                    dwfFacade.createEntity(DynamicEntityNames.FILA_MSG, (EntityVO) filaNovaVO);*/
                    filaVO.setProperty("EMAIL", filaVO.asString("EMAIL").concat(",").concat(email));
                    filaVO.setProperty("ASSUNTO", filaVO.asString("ASSUNTO").replace("Envio da NF-e -", "Envio da NF-e - produto com exigência de guia de trafégo -"));

                }
            }
        } finally {
            JapeSession.close(hnd);
        }

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

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
