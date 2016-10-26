<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)> 
<#assign tableName = pojo.decoratedObject.table.name>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numPropertySet = keyAndRequiredFieldList.get("NUM") />
<#assign strPropertySet = keyAndRequiredFieldList.get("STR") />		
<#assign columnFieldMap = dbdata.getColumnFieldMap(pojo) />
<#assign columnList = columnFieldMap.keyList() />
<#assign fieldList  = columnFieldMap.valueList() />
<#assign columnFieldWithoutIdMap = dbdata.getColumnFieldWithoutIdMap(pojo) />
<#assign columnWOIdList = columnFieldWithoutIdMap.keyList() />
<#assign columnFieldOnlyIdMap = dbdata.getColumnFieldOnlyIdMap(pojo) />
<#assign columnOnlyIdList = columnFieldOnlyIdMap.keyList() />
<?xml version="1.0" encoding="UTF-8"?>
<queryservice xmlns="http://www.anyframejava.org/schema/query/mapping" 
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
	xsi:schemaLocation="http://www.anyframejava.org/schema/query/mapping http://www.anyframejava.org/schema/query/mapping/anyframe-query-mapping-1.0.xsd ">

  <queries>
	<query id="createXP${pojo.shortName}" isDynamic="true" mappingStyle="upper">
		<statement>
		<![CDATA[
			INSERT INTO ${tableName.toUpperCase()} ( 			
			<#lt/>				<#list columnList as columnName>${columnName.toUpperCase()}<#if columnName_has_next>, 
			<#lt/>				</#if></#list>) 
			VALUES (<#list columnList as columnName>:${columnName.toUpperCase()}<#if columnName_has_next>, 
			<#lt/>				</#if></#list>)
		]]>
		</statement>
	</query>
		
	<query id="updateXP${pojo.shortName}" isDynamic="true" mappingStyle="upper">
		<statement>
		<![CDATA[
			UPDATE ${tableName.toUpperCase()}
			SET 
				<#list columnWOIdList as columnName>${columnName.toUpperCase()} = :${columnName.toUpperCase()}<#if columnName_has_next>, 
				<#lt/>				</#if></#list>
			WHERE
				<#list columnOnlyIdList as columnName>${columnName.toUpperCase()} = :${columnName.toUpperCase()}<#if columnName_has_next> AND </#if></#list> 
		]]>
		</statement>
	</query>
	
	<query id="removeXP${pojo.shortName}" isDynamic="true" mappingStyle="upper"> 
		<statement>
		<![CDATA[
			DELETE FROM ${tableName.toUpperCase()} 
			WHERE 
				<#list columnOnlyIdList as columnName>${columnName.toUpperCase()} = :${columnName.toUpperCase()}<#if columnName_has_next> AND </#if></#list>
		]]>
		</statement>
	</query>
	
	<query id="findXP${pojo.shortName}ByPk" isDynamic="true" mappingStyle="upper">
		<statement>
		<![CDATA[
			SELECT 
				<#list columnList as columnName>${pojoNameLower}.${columnName.toUpperCase()}<#if columnName_has_next>, </#if></#list>
			FROM ${tableName.toUpperCase()} ${pojoNameLower}
			WHERE 
				<#list columnOnlyIdList as columnName>${pojoNameLower}.${columnName.toUpperCase()} = :${columnName.toUpperCase()}<#if columnName_has_next> AND </#if></#list>
		]]>
		</statement>							
	</query>

	<query id="findXP${pojo.shortName}List" isDynamic="true" mappingStyle="upper">
		<statement>
		<![CDATA[
			SELECT 
				<#list columnList as columnName>${pojoNameLower}.${columnName.toUpperCase()}<#if columnName_has_next>, </#if></#list> 
			FROM ${tableName.toUpperCase()} ${pojoNameLower}
			#if($SEARCH_CONDITION && $SEARCH_CONDITION != ""  && $SEARCH_KEYWORD && $SEARCH_KEYWORD != "" )
			WHERE 		
				#if ($SEARCH_CONDITION == "All")
					(
					 <#if strPropertySet?has_content>
					 <#list strPropertySet as strProperty> ${pojoNameLower}.${dbdata.getColumnName(strProperty).toUpperCase()} like :SEARCH_KEYWORD <#if strProperty_has_next> or </#if></#list>
					 </#if>						
					 <#if numPropertySet?has_content>		 			 
					 	or <#list numPropertySet as numProperty>${pojoNameLower}.${dbdata.getColumnName(numProperty).toUpperCase()} = :SEARCH_KEYWORD <#if numProperty_has_next> or </#if></#list>
					 </#if>		
					)	
				<#if numPropertySet?has_content>
					<#list numPropertySet as numProperty>
				#elseif($SEARCH_CONDITION == "${dbdata.getColumnName(numProperty).toUpperCase()}")
					${pojoNameLower}.${dbdata.getColumnName(numProperty).toUpperCase()} = :SEARCH_KEYWORD
					</#list>			
				</#if>	
				<#if strPropertySet?has_content>
					<#list strPropertySet as strProperty>
				#elseif($SEARCH_CONDITION == "${dbdata.getColumnName(strProperty).toUpperCase()}")
					${pojoNameLower}.${dbdata.getColumnName(strProperty).toUpperCase()} like :SEARCH_KEYWORD		
					</#list>
				</#if>						
				#end
			#end			
				order by							
				<#foreach field in pojo.getAllPropertiesIterator()>
				<#if field.equals(pojo.identifierProperty)>
					<#if !c2j.isComponent(field) >
								${pojoNameLower}.${dbdata.getColumnName(field).toUpperCase()}
					<#else>		        	    
						<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
						<#assign keyList = dbdata.getKeyList(pojo)/>
						<#foreach idfield in keyList.keyList().iterator()>          		 	
							<#assign columnName = dbdata.getColumnName(idfield)>
							<#if keyList.get(idfield) != "FK" >
				    	 <#lt/>				${pojoNameLower}.${columnName.toUpperCase()}
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



