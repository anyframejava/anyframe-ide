<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign pojoNameUpper = pojo.shortName.toUpperCase()> 
<#assign tableName = pojo.decoratedObject.table.name>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numPropertySet = keyAndRequiredFieldList.get("NUM") />
<#assign strPropertySet = keyAndRequiredFieldList.get("STR") />		

<#assign columnFieldMap = dbdata.getColumnFieldMap(pojo) />
<#assign columnList = columnFieldMap.keyList() />
<#assign fieldList = columnFieldMap.valueList() />

<#assign columnFieldMapMybatis = dbdata.getColumnFieldMapMybatis(pojo) />
<#assign columnListMybatis = columnFieldMapMybatis.keyList() />
<#assign fieldListMybatis = columnFieldMapMybatis.valueList() />

<#assign columnFieldWithoutIdMap = dbdata.getColumnFieldWithoutIdMap(pojo) />
<#assign columnWOIdList = columnFieldWithoutIdMap.keyList() />

<#assign columnFieldWithoutIdMapMybatis = dbdata.getColumnFieldWithoutIdMapMybatis(pojo) />
<#assign columnWOIdListMybatis = columnFieldWithoutIdMapMybatis.keyList() />

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

<#assign paramSyntaxKey = "#" />
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${pojo.shortName}">
	
	<cache />
	
	<resultMap id="${pojoNameLower}Result" type="${pojo.shortName}">  
	  <#assign field = pojo.getIdentifierProperty()/>
	  <#if !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
	  <#assign pojoIdentifier = pojo.identifierProperty.getValue() >
	  <#foreach idfield in pojoIdentifier.getPropertyIterator()>        	 	
	  <result property="${pojo.getIdentifierProperty().getName()}.${idfield.name}" column="${dbdata.getColumn(idfield).getName()}" />
	  </#foreach> 
	  <#else>
	  <#list columnOnlyIdList as columnName>
      <result property="${columnFieldOnlyIdMap.get(columnName)}" column="${columnName}" />
	  </#list>		  
	  </#if>
	<#list columnWOIdList as columnName>
      <result property="${columnFieldWithoutIdMap.get(columnName)}" column="${columnName}" />
	</#list>	
	</resultMap>

	<insert id="insert${pojo.shortName}" parameterType="${pojo.shortName}">
		INSERT INTO ${tableName}
		(<#list columnListMybatis as columnName>${columnName}<#if columnName_has_next>, </#if></#list>) 
		VALUES (<#list fieldListMybatis as fieldName>${paramSyntaxKey}{${fieldName}}<#if fieldName_has_next>, </#if></#list>)
     </insert>

	<update id="update${pojo.shortName}" parameterType="${pojo.shortName}">
		UPDATE ${tableName} SET
		<#list columnWOIdListMybatis as columnName>${columnName} = ${paramSyntaxKey}{${columnFieldWithoutIdMapMybatis.get(columnName)}}<#if columnName_has_next>, </#if></#list><#if !c2j.isComponent(pojo.identifierProperty) && columnWOIdList?has_content && columnFieldManyToOneKeyList?has_content>,</#if>
		<#if !c2j.isComponent(pojo.identifierProperty)><#list columnFieldManyToOneKeyList as columnManyToOneName>${columnManyToOneName} = ${paramSyntaxKey}{${columnFieldManyToOneMap.get(columnManyToOneName)}}<#if columnManyToOneName_has_next>,</#if></#list></#if>WHERE <#list columnOnlyIdList as columnName>${columnName} = ${paramSyntaxKey}{${columnFieldOnlyIdMap.get(columnName)}}<#if columnName_has_next> AND </#if></#list> 
    </update>

	<delete id="delete${pojo.shortName}" parameterType="${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}">
		DELETE FROM ${tableName} WHERE <#list columnOnlyIdWONestedList as columnName>${columnName} = ${paramSyntaxKey}{${columnFieldOnlyIdWONestedMap.get(columnName)}}<#if columnName_has_next> AND </#if></#list>
    </delete>		
	
	<select id="get${pojo.shortName}" parameterType="${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)}" resultMap="${pojoNameLower}Result">
		SELECT <#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list>
		FROM ${tableName}
		WHERE <#list columnOnlyIdWONestedList as columnName>${columnName} = ${paramSyntaxKey}{${columnFieldOnlyIdWONestedMap.get(columnName)}}<#if columnName_has_next> AND </#if></#list>
    </select>
    
	<select id="get${pojo.shortName}List" parameterType="${pojo.shortName}" resultMap="${pojoNameLower}Result">
		SELECT  <#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list> 
		FROM ${tableName}
	</select>    
    
	<select id="get${pojo.shortName}ListCnt" parameterType="${pojo.shortName}" resultType="int">
		SELECT count(*)
		FROM ${tableName}
	</select>
	
</mapper>	