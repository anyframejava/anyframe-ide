<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
<#assign keyDataList = dbdata.getKeyDataList(pojo)> 
<#assign dataList = dbdata.getDataList(pojo)> 
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMapWithObject(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />
package ${package}.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.anyframe.pagination.Page;

/**
 * JUnit TestCase for ${pojo.shortName}Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:./src/main/resources/spring/context-*.xml" })
public class ${pojo.shortName}ServiceTest{

    @Inject
    @Named("${pojoNameLower}Service")
    private ${pojo.shortName}Service ${pojoNameLower}Service;

    @Test
	@Rollback(value = true)
	public void manage${pojo.shortName}() throws Exception {
		// 1. create new ${pojoNameLower}
		Map targetMap = getTargetMap();
		
		<#if c2j.isComponent(pojo.identifierProperty) >
        if(${pojoNameLower}Service.get(targetMap) != null)
			${pojoNameLower}Service.remove(targetMap); 
		</#if>
		
		${pojoNameLower}Service.create(targetMap);
		
		// 2. assert - create
		Map resultMap = ${pojoNameLower}Service.get(targetMap);
		assertNotNull("fail to fetch ${pojoNameLower}", resultMap);		
		
		// 3. update ${pojoNameLower}		
		<#foreach field in pojo.getAllPropertiesIterator()>
		    <#foreach column in field.getColumnIterator()>
		        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		            <#lt/>        targetMap.put("${pojo.getPropertyName(field).substring(0,1).toLowerCase()+pojo.getPropertyName(field).substring(1)}", ${data.getValueForJavaTest(column)});
		        </#if>
		    </#foreach>
		</#foreach>		
		${pojoNameLower}Service.update(targetMap);
		
		
		// 4. assert - update
		resultMap = ${pojoNameLower}Service.get(targetMap);
		assertNotNull("fail to fetch ${pojoNameLower}", resultMap);		
		
		// 5. get ${pojoNameLower} (page)
		Map searchTargetMap = new HashMap();
<#if c2j.isComponent(pojo.identifierProperty)>		
	<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
	<#foreach idfield in pojoIdentifier.getPropertyIterator()>
		<#assign column = dbdata.getColumn(idfield)>
		searchTargetMap.put("searchCondition", "${idfield.name}");
		<#break>	        
	</#foreach>
<#else>
		<#assign column = dbdata.getColumn(pojo.identifierProperty)>
        searchTargetMap.put("searchCondition", "${pojo.identifierProperty.name}");
</#if>
        searchTargetMap.put("searchKeyword", ${data.getValueForJavaTest(column)});
        searchTargetMap.put("pageIndex", "1");
	
        Page resultPage = ${pojoNameLower}Service.getPagingList(searchTargetMap);
        
        // 6. assert - getPagingList
        assertNotNull("result is not null", resultPage.getList());
		
		// 7. remove ${pojoNameLower}
		${pojoNameLower}Service.remove(targetMap);     
		
		// 8. assert - remove
		resultMap = ${pojoNameLower}Service.get(targetMap);
		assertNull("fail to remove ${pojoNameLower}", resultMap);
	}
	

	private Map getTargetMap() throws Exception {
		Map targetMap = new HashMap();
		
		// enter id fields
<#foreach field in pojo.getAllPropertiesIterator()>    
	<#foreach column in field.getColumnIterator()>
		<#if field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		targetMap.put("${field.name}", ${data.getValueForJavaTest(column)});    	
		</#if>
    </#foreach>    
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >       		
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
		<#assign setMethodName = 'set' + pojo.getPropertyName(idfield)>
		<#assign column = dbdata.getColumn(idfield)>
		targetMap.put("${idfield.name}", ${data.getExistingValueForJavaTest(column,keyDataList.get(column.getName()))});     	
		</#foreach>     		
	</#if>          	
</#foreach> 

        // enter all required fields
<#foreach field in pojo.getAllPropertiesIterator()>
	<#foreach column in field.getColumnIterator()>
		<#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field)>
		targetMap.put("${field.name}", ${data.getValueForJavaTest(column)});     	
		<#elseif !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>    	
          <#if columnFieldManyToOneKeyList?has_content>          
            <#list fieldManyToOneKeyList as propertyName>
              <#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
         targetMap.put("${propertyName}", ${data.getExistingValueForJavaTest(column,dataList.get(column.getName()))});
            </#list>          
          </#if>          	        	 		
		</#if>
    </#foreach>
</#foreach>
	
		return targetMap;
	}
}