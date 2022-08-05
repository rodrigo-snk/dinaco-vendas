package br.com.sankhya.dinaco.vendas.eventos;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.helper.AnexoSistemaHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.SWRepositoryUtils;
import com.sankhya.util.TimeUtils;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

public class CopiaAnexosEstoque implements EventoProgramavelJava {
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
        DynamicVO estVO = (DynamicVO) persistenceEvent.getVo();
        String codEmp = estVO.asBigDecimal("CODEMP").toString();
        String codProd = estVO.asBigDecimal("CODPROD").toString();
        String codLocal = estVO.asBigDecimal("CODLOCAL").toString();
        String controle = estVO.asString("CONTROLE");
        String codParc = estVO.asBigDecimal("CODPARC").toString();
        String tipo = estVO.asString("TIPO").toString();

        Collection<DynamicVO> anexos = EntityFacadeFactory.getDWFFacade().findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ANEXO_SISTEMA, "this.ENTIDADE = 'Estoque' AND this.PKREGISTRO LIKE '_\\_"+codProd+"\\_%\\_"+controle+"\\_0\\_P\\_Estoque' ESCAPE '\\'"));

        String chaveArquivo = AnexoSistemaHelper.buildChaveArquivo(DynamicEntityNames.ESTOQUE, (EntityPrimaryKey) estVO.getPrimaryKey());

        String pkRegistro = String.format("%s_%s_%s_%s_%s_%s_Estoque", codEmp, codProd, codLocal, controle, codParc, tipo);

        for (DynamicVO anexoVO: anexos) {
            String instancia = anexoVO.asString("NOMEINSTANCIA");
            String chave = anexoVO.asString("CHAVEARQUIVO");

            DynamicVO anexoEstoqueVO = (DynamicVO) EntityFacadeFactory.getDWFFacade().getDefaultValueObjectInstance("AnexoSistema");
            anexoEstoqueVO.setProperty("NOMEINSTANCIA", "Estoque");
            anexoEstoqueVO.setProperty("PKREGISTRO", pkRegistro);
            anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("CHAVEARQUIVO", chaveArquivo);
            anexoEstoqueVO.setProperty("DESCRICAO", anexoVO.getProperty("DESCRICAO"));
            anexoEstoqueVO.setProperty("NOMEARQUIVO", anexoVO.getProperty("NOMEARQUIVO"));
            anexoEstoqueVO.setProperty("TIPOAPRES", anexoVO.getProperty("TIPOAPRES"));
            anexoEstoqueVO.setProperty("TIPOACESSO", anexoVO.getProperty("TIPOACESSO"));
            anexoEstoqueVO.setProperty("CODUSU", anexoVO.getProperty("CODUSU"));
            anexoEstoqueVO.setProperty("DHCAD", anexoVO.getProperty("DHCAD"));

            EntityFacadeFactory.getDWFFacade().createEntity("AnexoSistema", (EntityVO) anexoEstoqueVO);


            Path sourceDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath()+"/Sistema/Anexos/"+instancia+"/"+chave);
            Path targetDirectory = Paths.get(SWRepositoryUtils.getBaseFolder().getAbsolutePath()+"/Sistema/Anexos/"+anexoEstoqueVO.asString("NOMEINSTANCIA")+"/"+anexoEstoqueVO.asString("CHAVEARQUIVO"));

            Files.copy(sourceDirectory, targetDirectory);


        }





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
