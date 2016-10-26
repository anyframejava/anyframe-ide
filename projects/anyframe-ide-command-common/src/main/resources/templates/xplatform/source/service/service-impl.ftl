<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
package ${package}.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import ${package}.service.${pojo.shortName}XPService;

import org.anyframe.xp.query.service.impl.XPDao;
import org.anyframe.xp.query.service.impl.XPServiceImpl;
import org.anyframe.xp.query.impl.XPQueryServiceImpl;

import com.tobesoft.xplatform.data.DataSet;
import com.tobesoft.xplatform.data.DataSetList;
import com.tobesoft.xplatform.data.VariableList;


@Service("${pojoNameLower}XPService")
public class ${pojo.shortName}XPServiceImpl extends XPServiceImpl implements ${pojo.shortName}XPService {
    
    @Inject
	public ${pojo.shortName}XPServiceImpl(XPDao xpDao){
		super.xpDao = xpDao;
	}
    
	public void saveAll(VariableList inVl, DataSetList inDl,
                    VariableList outVl, DataSetList outDl) throws Exception {

            Map<String, String> sqlMap = new HashMap<String, String>();
            sqlMap.put(XPQueryServiceImpl.QUERY_INSERT, "createXP${pojo.shortName}");
            sqlMap.put(XPQueryServiceImpl.QUERY_UPDATE, "updateXP${pojo.shortName}");
            sqlMap.put(XPQueryServiceImpl.QUERY_DELETE, "removeXP${pojo.shortName}");

            super.xpDao.saveAll(sqlMap, inDl.get("dsSave"));
    }

    public void getPagingList(VariableList inVl, DataSetList inDl,
                    VariableList outVl, DataSetList outDl) throws Exception {
            
             DataSet outDs = super.xpDao.getPagingList("findXP${pojo.shortName}List", inDl.get("dsSearch"));
             outDs.setName("dsResult");
             outDl.add(outDs);
    }  
}