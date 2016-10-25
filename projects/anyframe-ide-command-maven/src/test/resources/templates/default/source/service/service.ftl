<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
package ${basepackage}.service;

import anyframe.common.Page;
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
 * ${pojo.shortName}Service interface.
 */
public interface ${pojo.shortName}Service{

  	void create(${pojo.shortName} ${pojoNameLower}) throws Exception;
	
	void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception;
		
	void update(${pojo.shortName} ${pojoNameLower}) throws Exception;
	
  	${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception;
	
	Page getPagingList(SearchVO searchVO) throws Exception;        
}