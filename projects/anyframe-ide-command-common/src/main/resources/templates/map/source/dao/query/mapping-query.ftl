<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign pojoNameUpper = pojo.shortName.toUpperCase()> 
<#assign tableName = pojo.decoratedObject.table.name>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numPropertySet = keyAndRequiredFieldList.get("NUM") />
<#assign strPropertySet = keyAndRequiredFieldList.get("STR") />		
<#assign columnFieldMap = dbdata.getColumnFieldMap(pojo) />
<#assign columnList = columnFieldMap.keyList() />
<#assign fieldList = columnFieldMap.valueList() />
<#assign columnFieldWithoutIdMap = dbdata.getColumnFieldWithoutIdMap(pojo) />
<#assign columnWOIdList = columnFieldWithoutIdMap.keyList() />
<#assign columnFieldOnlyIdMap = dbdata.getColumnFieldOnlyIdMap(pojo) />
<#assign columnOnlyIdList = columnFieldOnlyIdMap.keyList() />
<#assign columnFieldCompositeKeyIdMap = dbdata.getColumnFieldCompositeKeyIdMap(pojo) />
<#assign columnCompositeKeyIdList = columnFieldCompositeKeyIdMap.keyList() />
<#assign fieldCompositeKeyIdList = columnFieldCompositeKeyIdMap.valueList() />
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMap(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />
<?xml version="1.0" encoding="UTF-8"?>
<queryservice xmlns="http://www.anyframejava.org/schema/query/mapping" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:schemaLocation="http://www.anyframejava.org/schema/query/mapping http://www.anyframejava.org/schema/query/mapping/anyframe-query-mapping-1.0.xsd ">

  <queries>
	<query id="create${pojo.shortName}">
		<statement>
		<![CDATA[
			INSERT INTO ${tableName} ( 			
			<#lt/>				<#list columnList as columnName>${columnName}<#if columnName_has_next>, 
			<#lt/>				</#if></#list>) 
			VALUES (<#list fieldList as fieldName> :${fieldName.substring(fieldName.lastIndexOf(".")+1)}<#if fieldName_has_next>, 
			<#lt/>				</#if></#list>)
		]]>
		</statement>
	</query>
		
	<query id="update${pojo.shortName}">
		<statement>
		<![CDATA[
			UPDATE ${tableName}
			SET 
				<#list columnWOIdList as columnName>${columnName} = :${columnFieldWithoutIdMap.get(columnName).substring(columnFieldWithoutIdMap.get(columnName).lastIndexOf(".")+1)}<#if columnName_has_next>, 
				<#lt/>				</#if></#list><#if !c2j.isComponent(pojo.identifierProperty) && columnWOIdList?has_content && columnFieldManyToOneKeyList?has_content>,</#if>
				<#if !c2j.isComponent(pojo.identifierProperty)><#list columnFieldManyToOneKeyList as columnManyToOneName>${columnManyToOneName} = :${columnFieldManyToOneMap.get(columnManyToOneName).substring(columnFieldManyToOneMap.get(columnManyToOneName).lastIndexOf(".")+1)}<#if columnManyToOneName_has_next>,
    			<#lt/>				</#if></#list></#if>	
			WHERE
				<#list columnOnlyIdList as columnName>${columnName} = :${columnFieldOnlyIdMap.get(columnName).substring(columnFieldOnlyIdMap.get(columnName).lastIndexOf(".")+1)}<#if columnName_has_next> AND </#if></#list> 
		]]>
		</statement>
	</query>
	
	<query id="remove${pojo.shortName}">
		<statement>
		<![CDATA[
			DELETE FROM ${tableName} 
			WHERE 
				<#list columnOnlyIdList as columnName>${columnName} = :${columnFieldOnlyIdMap.get(columnName).substring(columnFieldOnlyIdMap.get(columnName).lastIndexOf(".")+1)}<#if columnName_has_next> AND </#if></#list>
		]]>
		</statement>
	</query>
	
	<query id="find${pojo.shortName}ByPk">
		<statement>
		<![CDATA[
			SELECT 
				<#list columnList as columnName>${pojoNameLower}.${columnName}<#if columnName_has_next>, </#if></#list>
			FROM ${tableName} ${pojoNameLower}
			WHERE 
				<#list columnOnlyIdList as columnName>${pojoNameLower}.${columnName} = :${columnFieldOnlyIdMap.get(columnName).substring(columnFieldOnlyIdMap.get(columnName).lastIndexOf(".")+1)}<#if columnName_has_next> AND </#if></#list>
		]]>
		</statement>
	</query>

	<query id="find${pojo.shortName}List" isDynamic="true">
	<statement>
	<![CDATA[
		SELECT 
			<#list columnList as columnName>${pojoNameLower}.${columnName}<#if columnName_has_next>, </#if></#list> 
		FROM ${tableName} ${pojoNameLower}
		#if ($keywordNum != "")			
			WHERE 		
			#if ($condition == "All" || $condition == "")
				(
				 <#if strPropertySet?has_content>
				 <#list strPropertySet as strProperty> ${pojoNameLower}.${dbdata.getColumnName(strProperty)} like :keywordStr<#if strProperty_has_next> or </#if></#list>
				 </#if>						
				 <#if numPropertySet?has_content>		 			 
				 #if($isNumeric == "true")
				 	or <#list numPropertySet as numProperty>${pojoNameLower}.${dbdata.getColumnName(numProperty)} = {{keywordNum}} <#if numProperty_has_next> or </#if></#list>
				 #end
				 </#if>		
				)	
			<#if numPropertySet?has_content>
				<#list numPropertySet as numProperty>
			#elseif($condition == "${numProperty.name}")
				${pojoNameLower}.${dbdata.getColumnName(numProperty)} = {{keywordNum}}	
				</#list>			
			</#if>	
			<#if strPropertySet?has_content>
				<#list strPropertySet as strProperty>
			#elseif($condition == "${strProperty.name}")
				${pojoNameLower}.${dbdata.getColumnName(strProperty)} like :keywordStr			
				</#list>
			</#if>						
			#end
		#end			
		order by						
<#foreach field in pojo.getAllPropertiesIterator()>
	<#if field.equals(pojo.identifierProperty)>
		<#if !c2j.isComponent(field) >
			${pojoNameLower}.${dbdata.getColumnName(field)}
		<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
			      ${pojoNameLower}.${columnName}
		 	<#break/>
			</#if>
		</#foreach>     			
	    </#if>
	</#if>
</#foreach>									
	]]>
	</statement>		
	</query>
  </queries>
</queryservice>
