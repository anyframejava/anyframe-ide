<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${package}.service.impl;

import java.util.List;

import org.anyframe.pagination.Page;
import org.anyframe.datatype.SearchVO;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.support.SqlSessionDaoSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

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
 * ${pojo.shortName}Dao implementation class with mybatis
 *
 */
@Repository("${pojoNameLower}Dao")
public class ${pojo.shortName}Dao extends SqlSessionDaoSupport {
  
  	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;
		
	public void create(${pojo.shortName} ${pojoNameLower}) {
		getSqlSession().insert("${pojo.shortName}.insert${pojo.shortName}", ${pojoNameLower});
	}
	
	public void update(${pojo.shortName} ${pojoNameLower}) {
		getSqlSession().update("${pojo.shortName}.update${pojo.shortName}", ${pojoNameLower});
	}
	
	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) {
		getSqlSession().delete("${pojo.shortName}.delete${pojo.shortName}", ${pojo.identifierProperty.name});
	}
	
	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) {
		return (${pojo.shortName}) getSqlSession().selectOne("${pojo.shortName}.get${pojo.shortName}", ${pojo.identifierProperty.name});
	}

	public Page getPagingList(SearchVO searchVO) {
      int pageIndex = searchVO.getPageIndex();		
	 
	  List<${pojo.shortName}> list=getSqlSession().selectList("${pojo.shortName}.get${pojo.shortName}List", searchVO, new RowBounds(pageSize * (pageIndex - 1), pageSize));
	  int rowCount= (Integer)getSqlSession().selectOne("${pojo.shortName}.get${pojo.shortName}ListCnt");
	  
	  return new Page(list, pageIndex, rowCount, pageUnit, pageSize);
  }	
}
