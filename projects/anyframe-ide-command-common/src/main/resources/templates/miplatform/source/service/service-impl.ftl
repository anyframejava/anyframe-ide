<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
package ${package}.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import ${package}.service.${pojo.shortName}MiPService;

import org.anyframe.mip.query.service.impl.MiPDao;
import org.anyframe.mip.query.service.impl.MiPServiceImpl;
import org.anyframe.mip.query.impl.MiPQueryServiceImpl;

import com.tobesoft.platform.data.DatasetList;
import com.tobesoft.platform.data.VariableList;


@Service("${pojoNameLower}MiPService")
public class ${pojo.shortName}MiPServiceImpl extends MiPServiceImpl implements ${pojo.shortName}MiPService {
    
    @Inject
	public ${pojo.shortName}MiPServiceImpl(MiPDao mipDao){
		super.mipDao = mipDao;
	}
    
	public void saveAll(VariableList inVl, DatasetList inDl,
                    VariableList outVl, DatasetList outDl) throws Exception {

            Map sqlMap = new HashMap();
            sqlMap.put(MiPQueryServiceImpl.QUERY_INSERT, "create${pojo.shortName}");
            sqlMap.put(MiPQueryServiceImpl.QUERY_UPDATE, "update${pojo.shortName}");
            sqlMap.put(MiPQueryServiceImpl.QUERY_DELETE, "remove${pojo.shortName}");

            super.mipDao.saveAll(sqlMap, inDl.get("dsSave"));
    }

    public void getPagingList(VariableList inVl, DatasetList inDl,
                    VariableList outVl, DatasetList outDl) throws Exception {
            
            outDl.add("dsResult",super.mipDao.getPagingList("find${pojo.shortName}List", inDl.get("dsSearch")));
    }  
}