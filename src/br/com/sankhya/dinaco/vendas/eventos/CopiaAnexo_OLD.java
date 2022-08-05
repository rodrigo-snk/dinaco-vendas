package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.TimeUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CopiaAnexo_OLD implements EventoProgramavelJava {
    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

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

        if (persistenceEvent.getModifingFields().isModifing("CHAVEARQUIVO")) {
            DynamicVO anexoVO = (DynamicVO) persistenceEvent.getVo();
            String instancia = anexoVO.asString("NOMEINSTANCIA");
            String chave = anexoVO.asString("CHAVEARQUIVO");

            File file = SWRepositoryUtils.getFile("Repo://Sistema/Anexos/"+instancia+"/"+chave);

            DynamicVO anexoEstoqueVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance("AnexoSistema");
            //anexoVO.copyPersistentPropertiesTo(anexoEstoqueVO);

            if (instancia.equals("AD_TGFEST")) {
                anexoEstoqueVO.setProperty("NOMEINSTANCIA", "Estoque");
                anexoEstoqueVO.setProperty("PKREGISTRO", anexoVO.asString("PKREGISTRO").replace("AD_TGFEST", "Estoque"));
                anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
                anexoEstoqueVO.setProperty("CHAVEARQUIVO", anexoVO.getProperty("CHAVEARQUIVO"));
                anexoEstoqueVO.setProperty("DESCRICAO", anexoVO.getProperty("DESCRICAO"));
                anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
                anexoEstoqueVO.setProperty("TIPOAPRES", anexoVO.getProperty("TIPOAPRES"));
                anexoEstoqueVO.setProperty("TIPOACESSO", anexoVO.getProperty("TIPOACESSO"));
                anexoEstoqueVO.setProperty("CODUSU", anexoVO.getProperty("CODUSU"));
                anexoEstoqueVO.setProperty("DHCAD", TimeUtils.getNow());

                EntityFacadeFactory.getDWFFacade().createEntity("AnexoSistema", (EntityVO) anexoEstoqueVO);


                Path sourceDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath()+"/Sistema/Anexos/"+instancia+"/"+chave);
                Path targetDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath()+"/Sistema/Anexos/"+anexoEstoqueVO.asString("NOMEINSTANCIA")+"/"+anexoEstoqueVO.asString("CHAVEARQUIVO"));

                Files.copy(sourceDirectory, targetDirectory);
            }

        }

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
