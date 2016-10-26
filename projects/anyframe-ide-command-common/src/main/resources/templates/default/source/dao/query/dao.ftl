<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${package}.service.impl;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import org.anyframe.pagination.Page;
import org.anyframe.datatype.SearchVO;
import org.anyframe.util.NumberUtil;
import org.anyframe.util.StringUtil;
import org.anyframe.query.dao.QueryServiceDaoSupport;
import org.anyframe.query.QueryService;

import ${pojo.packageName}.${pojo.shortName};
<#if c2j.isComponent(pojo.identifierProperty) >
import ${pojo.packageName}.${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)};
<#elseif !data.isPrimitive(valueType)>
  <#foreach field in pojo.getAllPropertiesIterator()>	  
    <#if field.equals(pojo.identifierProperty)>
import ${field.value.type.returnedClass.name};
    </#if>
  </#foreach>
</#if>

/**
 * ${pojo.shortName}Dao implementation class with QueryService
 *
 */
@Repository("${pojoNameLower}Dao")
public class ${pojo.shortName}Dao extends QueryServiceDaoSupport {
  
  	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;
		
	@Inject
	public void setQueryService(QueryService queryService) {
		super.setQueryService(queryService);
	}
	
	public void create(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.create("create${pojo.shortName}", ${pojoNameLower});
	}
	
	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();        
    	${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});    
		super.remove("remove${pojo.shortName}", ${pojoNameLower});
	}

	public void update(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.update("update${pojo.shortName}", ${pojoNameLower});
	}

	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();		
		${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});
				
		return (${pojo.shortName}) super.findByPk("find${pojo.shortName}ByPk", ${pojoNameLower});
	}
	
	public Page getPagingList(SearchVO searchVO) throws Exception {
        int pageIndex = searchVO.getPageIndex();

        String searchCondition = StringUtil.nullToString(searchVO.getSearchCondition());
        String searchKeyword = StringUtil.nullToString(searchVO.getSearchKeyword());
        String isNumeric = NumberUtil.isNumber(searchKeyword) ? "true" : "false";
        
        Object[] args = new Object[4];		            
        args[0] = "condition=" + searchCondition;
        args[1] = "keywordStr=%" + searchKeyword + "%";
        args[2] = "keywordNum=" + searchKeyword + "";
        args[3] = "isNumeric=" + isNumeric;

        return super.findListWithPaging("find${pojo.shortName}List", args, pageIndex, pageSize, pageUnit);
	}    
}
