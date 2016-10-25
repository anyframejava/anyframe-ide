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

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.anyframe.datatype.SearchVO;
import org.anyframe.pagination.Page;

import ${pojo.packageName}.${pojo.shortName};
<#if c2j.isComponent(pojo.identifierProperty) >
import ${pojo.packageName}.${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)};
</#if>

<#foreach field in pojo.getAllPropertiesIterator()>
	<#foreach column in field.getColumnIterator()> 
		<#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
import ${pojo.packageName}.${pojo.getJavaTypeName(field, jdk5)};
		</#if>
	</#foreach>	
</#foreach>	
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:./src/main/resources/spring/context-*.xml" })
public class ${pojo.shortName}ServiceTest{
    
    @Inject
	@Named("${pojoNameLower}Service")
    private ${pojo.shortName}Service ${pojoNameLower}Service;    

	@Test	 
	@Rollback(value=true)	    
    public void manage${pojo.shortName}() throws Exception {
        // 1. create a new ${pojoNameLower}
        ${pojo.shortName} ${pojoNameLower} = get${pojo.shortName}();
			
        try {			
        if(${pojoNameLower}Service.get(${pojoNameLower}.${getIdMethodName}()) != null)
			${pojoNameLower}Service.remove(${pojoNameLower}.${getIdMethodName}());
		}catch(Exception e) { //ignore this exception 
		}				
        ${pojoNameLower}Service.create(${pojoNameLower});
        <#lt/><#if daoframework == "daoframework">flush();</#if><#rt/>        
        
        // 2. assert - create
		${pojoNameLower} = ${pojoNameLower}Service.get(${pojoNameLower}.${getIdMethodName}());
		assertNotNull("fail to fetch a ${pojoNameLower}", ${pojoNameLower});
				
		// 3. update a title of ${pojoNameLower}
		<#foreach field in pojo.getAllPropertiesIterator()>
		    <#foreach column in field.getColumnIterator()>
		        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		            <#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${data.getValueForJavaTest(column)});
		        </#if>
		    </#foreach>
		</#foreach>
		${pojoNameLower}Service.update(${pojoNameLower});

		// 4. assert - update
		${pojoNameLower} = ${pojoNameLower}Service.get(${pojoNameLower}.${getIdMethodName}());
		assertNotNull("fail to fetch a ${pojoNameLower}", ${pojoNameLower});

		// 5. remove a ${pojoNameLower}
		${pojoNameLower}Service.remove(${pojoNameLower}.${getIdMethodName}());

      }
    
    private ${pojo.shortName} get${pojo.shortName}() throws Exception {
		${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();
        // enter id fields        
<#foreach field in pojo.getAllPropertiesIterator()>    
    <#foreach column in field.getColumnIterator()>
        <#if field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
            <#lt/>        ${pojoNameLower}.${setIdMethodName}(${data.getExistingValueForJavaTest(column,keyDataList.get(column.getName()))});                                                                    
        </#if>
    </#foreach>    
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
            <#lt/>        ${identifierType} ${pojo.identifierProperty.name} = new ${identifierType}();
        		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
        		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
        		<#assign setMethodName = 'set' + pojo.getPropertyName(idfield)>
        		<#assign column = dbdata.getColumn(idfield)>
            <#lt/>        ${pojo.identifierProperty.name}.${setMethodName}(${data.getExistingValueForJavaTest(column,keyDataList.get(column.getName()))});            	
          		</#foreach>     		
          	<#lt/>        ${pojoNameLower}.${setIdMethodName}(${pojo.identifierProperty.name});
    </#if>          	
</#foreach> 
       
        // enter all required fields
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>   
        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field)>
			<#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${data.getExistingValueForJavaTest(column,dataList.get(column.getName()))});     
        <#elseif !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
		  <#assign fieldType = pojo.getJavaTypeName(field, jdk5)>
          <#assign fieldTypeLower = fieldType.substring(0,1).toLowerCase()+fieldType.substring(1)>
        	<#lt/>        ${fieldType} ${fieldTypeLower} = new ${fieldType}();        	    	
          <#if columnFieldManyToOneKeyList?has_content>          
            <#list fieldManyToOneKeyList as propertyName>
              <#assign setMethodName = 'set' + propertyName.substring(0,1).toUpperCase()+propertyName.substring(1)>
              <#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
            <#lt/>        ${fieldTypeLower}.${setMethodName}(${data.getExistingValueForJavaTest(column,dataList.get(column.getName()))});    
            </#list>          
          </#if>    	        	 		
          <#lt/>        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${fieldTypeLower});
        </#if>
    </#foreach>
</#foreach>

		return ${pojoNameLower};
	}
	
	@Test
	public void find${pojo.shortName}List() throws Exception {
		SearchVO searchVO = new SearchVO();
        searchVO.setPageIndex(1);
		Page page = ${pojoNameLower}Service.getPagingList(searchVO);

		assertNotNull("page is not null", page);
	}		
}