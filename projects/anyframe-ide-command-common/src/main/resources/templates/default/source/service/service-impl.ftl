<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${package}.service.impl;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.anyframe.pagination.Page;
import org.anyframe.datatype.SearchVO;

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

import ${package}.service.${pojo.shortName}Service;

/**
 * ${pojo.shortName}Service implementation class
 */
@Service("${pojoNameLower}Service")
@Transactional(rollbackFor = {Exception.class}, propagation = Propagation.REQUIRED)
public class ${pojo.shortName}ServiceImpl implements ${pojo.shortName}Service {
	       
    @Inject
    @Named("${pojoNameLower}Dao")
	private ${pojo.shortName}Dao ${pojoNameLower}Dao;	
	
	public void update(${pojo.shortName} ${pojoNameLower}) throws Exception {
    	this.${pojoNameLower}Dao.update(${pojoNameLower});
	}    
    
  	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		return this.${pojoNameLower}Dao.get(${pojo.identifierProperty.name});
	}
    
  	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
    	this.${pojoNameLower}Dao.remove(${pojo.identifierProperty.name});
	}    
    
  	public void create(${pojo.shortName} ${pojoNameLower}) throws Exception {
    	this.${pojoNameLower}Dao.create(${pojoNameLower});
	}
		    
	public Page getPagingList(SearchVO searchVO) throws Exception {
		return this.${pojoNameLower}Dao.getPagingList(searchVO);
	}              
}
