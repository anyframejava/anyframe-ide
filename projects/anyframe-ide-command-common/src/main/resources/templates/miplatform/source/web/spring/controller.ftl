<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.web;

import javax.inject.Inject;
import javax.inject.Named;

import ${package}.service.${pojo.shortName}MiPService;
import org.anyframe.mip.query.web.controller.AbstractMiPDispatchController;

import com.tobesoft.platform.PlatformRequest;
import com.tobesoft.platform.data.DatasetList;
import com.tobesoft.platform.data.VariableList;

public class ${pojo.shortName}MiPController extends AbstractMiPDispatchController {

	@Inject
	@Named("${pojoNameLower}MiPService")
	private ${pojo.shortName}MiPService ${pojoNameLower}MiPService;
    
    public void getPagingList(PlatformRequest platformRequest,
                    VariableList inVl, DatasetList inDl, VariableList outVl,
                    DatasetList outDl) throws Exception {
            ${pojoNameLower}MiPService.getPagingList(inVl, inDl, outVl, outDl);
    }

    public void saveAll(PlatformRequest platformRequest, VariableList inVl,
                    DatasetList inDl, VariableList outVl, DatasetList outDl)
                    throws Exception {
            ${pojoNameLower}MiPService.saveAll(inVl, inDl, outVl, outDl);
    }

}

