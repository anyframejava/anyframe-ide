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
<#assign columnFieldOnlyIdWONestedMap = dbdata.getColumnFieldOnlyIdWithoutNestedPropertyMap(pojo) />
<#assign columnOnlyIdList = columnFieldOnlyIdMap.keyList() />
<#assign columnOnlyIdWONestedList = columnFieldOnlyIdWONestedMap.keyList() />
<#assign columnFieldCompositeKeyIdMap = dbdata.getColumnFieldCompositeKeyIdMap(pojo) />
<#assign columnCompositeKeyIdList = columnFieldCompositeKeyIdMap.keyList() />
<#assign fieldCompositeKeyIdList = columnFieldCompositeKeyIdMap.valueList() />
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMap(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE sqlMap PUBLIC "-//ibatis.apache.org//DTD SQL Map 2.0//EN"
"http://ibatis.apache.org/dtd/sql-map-2.dtd">

<sqlMap namespace="${pojo.shortName}">

	<typeAlias alias="${pojo.shortName}" type="${pojo.packageName}.${pojo.shortName}" />

	<resultMap id="${pojoNameLower}Result" class="${pojo.shortName}">  
	  <#assign field = pojo.getIdentifierProperty()/>
	  <#if !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
	  <result property="${pojo.getIdentifierProperty().getName()}" resultMap="${pojo.shortName}.idResult" />
	  <#else>
	  <#list columnOnlyIdList as columnName>
       <result property="${columnFieldOnlyIdMap.get(columnName)}" column="${columnName}" />
	  </#list>		  
	  </#if>
	<#list columnWOIdList as columnName>
      <result property="${columnFieldWithoutIdMap.get(columnName)}" column="${columnName}" />
	</#list>	
	</resultMap>

<#foreach field in pojo.getAllPropertiesIterator()>     
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
    <typeAlias alias="${identifierType}" type="${pojo.packageName}.${identifierType}" />
	<resultMap id="idResult" class="${identifierType}">
	<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
	<#foreach idfield in pojoIdentifier.getPropertyIterator()>        	 	
	  <result property="${idfield.name}" column="${dbdata.getColumn(idfield).getName()}" />
    </#foreach> 
    </resultMap>  	 
    </#if>          	
</#foreach> 

	<insert id="insert${pojo.shortName}" parameterClass="${pojo.shortName}">
		INSERT INTO ${tableName}
		(<#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list>) 
		VALUES (<#list fieldList as fieldName>#${fieldName}#<#if fieldName_has_next>, </#if></#list>)
     </insert>

	<update id="update${pojo.shortName}" parameterClass="${pojo.shortName}">
    UPDATE ${tableName} SET
    <#list columnWOIdList as columnName>${columnName} = #${columnFieldWithoutIdMap.get(columnName)}#<#if columnName_has_next>, </#if></#list><#if !c2j.isComponent(pojo.identifierProperty) && columnWOIdList?has_content && columnFieldManyToOneKeyList?has_content>,</#if>
	<#if !c2j.isComponent(pojo.identifierProperty)><#list columnFieldManyToOneKeyList as columnManyToOneName>${columnManyToOneName} = #${columnFieldManyToOneMap.get(columnManyToOneName)}#<#if columnManyToOneName_has_next>,</#if></#list></#if>	WHERE <#list columnOnlyIdList as columnName>${columnName} = #${columnFieldOnlyIdMap.get(columnName)}#<#if columnName_has_next> AND </#if></#list> 
    </update>

	<delete id="delete${pojo.shortName}" parameterClass="${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}">
		DELETE FROM ${tableName} WHERE <#list columnOnlyIdWONestedList as columnName>${columnName} = #${columnFieldOnlyIdWONestedMap.get(columnName)}#<#if columnName_has_next> AND </#if></#list>
    </delete>		
	
	<select id="get${pojo.shortName}" parameterClass="${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}" resultMap="${pojoNameLower}Result">
		SELECT <#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list>
		FROM ${tableName}
		WHERE <#list columnOnlyIdWONestedList as columnName>${columnName} = #${columnFieldOnlyIdWONestedMap.get(columnName)}#<#if columnName_has_next> AND </#if></#list>
    </select>
    
	<select id="get${pojo.shortName}List" resultMap="${pojoNameLower}Result">
		SELECT  <#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list> 
		FROM ${tableName}
	</select>    
    
	<select id="get${pojo.shortName}ListCnt" resultClass="int">
		SELECT count(*)
		FROM ${tableName}
	</select>
	
</sqlMap>	