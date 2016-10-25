<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${basepackage}.service.impl;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import anyframe.common.Page;
import anyframe.common.util.StringUtil;
import anyframe.core.query.AbstractDAO;
import anyframe.core.query.IQueryService;
import anyframe.common.util.SearchVO;

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
public class ${pojo.shortName}Dao extends AbstractDAO {
  
  	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;
		
	@Inject
	public void setQueryService(IQueryService queryService) {
		super.setQueryService(queryService);
	}
	
	/** {@inheritDoc} */
	public void create(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.create("${pojo.shortName}", ${pojoNameLower});
	}
	/** {@inheritDoc} */
	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();        
    	${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});    
		super.remove("${pojo.shortName}", ${pojoNameLower});
	}
	
	/** {@inheritDoc} */
	public void update(${pojo.shortName} ${pojoNameLower}) throws Exception {
		super.update("${pojo.shortName}", ${pojoNameLower});
	}
	
	/** {@inheritDoc} */
	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();		
		${pojoNameLower}.set${pojo.getPropertyName(pojo.identifierProperty)}(${pojo.identifierProperty.name});
				
		return (${pojo.shortName}) super.findByPk("${pojo.shortName}", ${pojoNameLower});
	}
	
    /** {@inheritDoc} */
	public Page getPagingList(SearchVO searchVO) throws Exception {
        int pageIndex = searchVO.getPageIndex();

        String searchCondition = StringUtil.null2str(searchVO.getSearchCondition());
        String searchKeyword = StringUtil.null2str(searchVO.getSearchKeyword());
        String isNumeric = StringUtil.isNumeric(searchKeyword) ? "true" : "false";
        
        Object[] args = new Object[4];		            
        args[0] = "condition=" + searchCondition;
        args[1] = "keywordStr=%" + searchKeyword + "%";
        args[2] = "keywordNum=" + searchKeyword + "";
        args[3] = "isNumeric=" + isNumeric;

        return super.findListWithPaging("${pojo.shortName}", args, pageIndex, pageSize, pageUnit);
	}    
}