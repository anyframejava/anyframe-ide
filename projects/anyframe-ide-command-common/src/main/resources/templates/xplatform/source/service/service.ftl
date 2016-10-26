package ${package}.service;

import org.anyframe.xp.query.service.XPService;

import com.tobesoft.xplatform.data.DataSetList;
import com.tobesoft.xplatform.data.VariableList;

public interface ${pojo.shortName}XPService extends XPService{

    public void getPagingList(VariableList inVl, DataSetList inDl,
			VariableList outVl, DataSetList outDl) throws Exception;

    public void saveAll(VariableList inVl, DataSetList inDl,
			VariableList outVl, DataSetList outDl) throws Exception;         
}