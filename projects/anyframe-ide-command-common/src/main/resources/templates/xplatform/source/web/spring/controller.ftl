<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.web;

import javax.inject.Inject;
import javax.inject.Named;

import ${package}.service.${pojo.shortName}XPService;
import org.anyframe.xp.query.web.controller.AbstractXPDispatchController;

import com.tobesoft.xplatform.tx.HttpPlatformRequest;
import com.tobesoft.xplatform.data.DataSetList;
import com.tobesoft.xplatform.data.VariableList;

public class ${pojo.shortName}XPController extends AbstractXPDispatchController {

	@Inject
	@Named("${pojoNameLower}XPService")
	private ${pojo.shortName}XPService ${pojoNameLower}XPService;
    
    public void getPagingList(HttpPlatformRequest platformRequest,
                    VariableList inVl, DataSetList inDl, VariableList outVl,
                    DataSetList outDl) throws Exception {
            ${pojoNameLower}XPService.getPagingList(inVl, inDl, outVl, outDl);
    }

    public void saveAll(HttpPlatformRequest platformRequest, VariableList inVl,
                    DataSetList inDl, VariableList outVl, DataSetList outDl)
                    throws Exception {
            ${pojoNameLower}XPService.saveAll(inVl, inDl, outVl, outDl);
    }

}

