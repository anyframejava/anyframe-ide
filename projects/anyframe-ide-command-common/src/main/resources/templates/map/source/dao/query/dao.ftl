<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
package ${package}.service.impl;

import java.util.Map;
import javax.inject.Inject;

import org.anyframe.pagination.Page;
import org.anyframe.query.QueryService;
import org.anyframe.query.dao.AbstractDao;
import org.anyframe.util.NumberUtil;
import org.anyframe.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * ${pojo.shortName}Dao implementation class with QueryService
 *
 */
@Repository("${pojoNameLower}Dao") 
public class ${pojo.shortName}Dao extends AbstractDao {

	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;
    
    @Inject
	public void setQueryService(QueryService queryService) {
		super.setQueryService(queryService);
	}

    public void create(Map targetMap) throws Exception {
        super.create("${pojo.shortName}", targetMap);
    }

    public void update(Map targetMap) throws Exception {
        super.update("${pojo.shortName}", targetMap);
    }

    public void remove(Map targetMap) throws Exception {
        super.remove("${pojo.shortName}", targetMap);		
    }

    public Map get(Map targetMap) throws Exception {
        return (Map) super.findByPk("${pojo.shortName}", targetMap);
    }

    public Page getPagingList(Map targetMap) throws Exception {
        int pageIndex = 1;
        if(targetMap.get("pageIndex") != null)
            pageIndex = StringUtil.string2integer(targetMap.get("pageIndex").toString());

        String condition = targetMap.get("searchCondition") != null ? 
        		StringUtil.null2str(targetMap.get("searchCondition").toString()) : "";
        String searchKeyword = targetMap.get("searchKeyword") != null ? 
        		StringUtil.null2str(targetMap.get("searchKeyword").toString()) : "";
        String isNumeric = NumberUtil.isNumber(searchKeyword) ? "true" : "false";

        // add search information to targetMap		
        targetMap.put("condition", condition);
        targetMap.put("keywordStr", "%" + searchKeyword + "%");
        targetMap.put("keywordNum", searchKeyword);
        targetMap.put("isNumeric", isNumeric);

        return super.findListWithPaging("${pojo.shortName}", targetMap, pageIndex, pageSize, pageUnit);
	}
}
