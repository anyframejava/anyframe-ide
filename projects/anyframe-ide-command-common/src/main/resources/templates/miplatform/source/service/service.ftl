package ${package}.service;

import org.anyframe.mip.query.service.MiPService;

import com.tobesoft.platform.data.DatasetList;
import com.tobesoft.platform.data.VariableList;

public interface ${pojo.shortName}MiPService extends MiPService{

    public void getPagingList(VariableList inVl, DatasetList inDl,
			VariableList outVl, DatasetList outDl) throws Exception;

    public void saveAll(VariableList inVl, DatasetList inDl,
			VariableList outVl, DatasetList outDl) throws Exception;         
}