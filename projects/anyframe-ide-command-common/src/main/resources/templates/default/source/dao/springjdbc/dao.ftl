<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign valueType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>	
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
<#assign getIdMethodName = pojo.getGetterSignature(pojo.identifierProperty)>
<#assign setIdMethodName = 'set' + pojo.getPropertyName(pojo.identifierProperty)>
<#assign identifierType = pojo.getJavaTypeName(pojo.identifierProperty, jdk5)>
package ${package}.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.anyframe.pagination.Page;
import org.anyframe.datatype.SearchVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
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

<#foreach field in pojo.getAllPropertiesIterator()>
	<#if field.equals(pojo.identifierProperty)>
		<#if !c2j.isComponent(field) >
    		<#assign isComponentKey = false/>
		<#else>
			<#assign isComponentKey = true/>
			<#assign pojoIdentifier = pojo.identifierProperty.getValue() >		
			<#foreach idfield in pojoIdentifier.getPropertyIterator()>
				<#assign firstIdfield = "${idfield.name}" />	
				<#break/>			
			</#foreach>	
		</#if> 
	</#if>		 
</#foreach>

/**
 * ${pojo.shortName}Dao implementation class with Spring JDBC Template
 *
 */
@Repository("${pojoNameLower}Dao")
public class ${pojo.shortName}Dao extends JdbcDaoSupport {
  
  	@Value("${r"#{contextProperties['pageSize'] ?: 10}"}")
	int pageSize;

	@Value("${r"#{contextProperties['pageUnit'] ?: 10}"}")
	int pageUnit;
		
	@Inject
	public void setJdbcDaoDataSource(DataSource dataSource) throws Exception {
		super.setDataSource(dataSource);
	}
	
	public void create(${pojo.shortName} ${pojoNameLower}) throws Exception {
		String sql = "INSERT INTO ${tableName} (<#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list>) "
			         + "VALUES (<#list fieldList as fieldName>?<#if fieldName_has_next>, </#if></#list>)";
				
		this.getJdbcTemplate().update(
				sql,
				new Object[] {<#list fieldList as fieldName><#assign getterMethodName = dbdata.getGetterMethodName(fieldName) />${pojoNameLower}.${getterMethodName}<#if fieldName_has_next>, </#if></#list>});		
	}
	
	public void remove(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {
		String sql = "DELETE FROM ${tableName} WHERE <#list columnOnlyIdList as columnName>${columnName} = ?<#if columnName_has_next> AND </#if></#list>";
		this.getJdbcTemplate().update(sql, new Object[]  { 
		<#if isComponentKey>
				 	<#list columnOnlyIdList as columnName><#assign getterMethodName = dbdata.getIdGetterMethodName(columnFieldOnlyIdMap.get(columnName)) />
					${pojo.identifierProperty.name}.${getterMethodName}<#if columnName_has_next>, </#if></#list> });					 	
		<#else>	
					${pojo.identifierProperty.name}});					 	
		</#if>
	}
	
	public void update(${pojo.shortName} ${pojoNameLower}) throws Exception {
		String sql = "UPDATE ${tableName} SET <#list columnWOIdList as columnName>${columnName} = ?<#if columnName_has_next>, </#if></#list><#if !c2j.isComponent(pojo.identifierProperty) && columnWOIdList?has_content && columnFieldManyToOneKeyList?has_content>,</#if><#if !c2j.isComponent(pojo.identifierProperty)><#list columnFieldManyToOneKeyList as columnManyToOneName>${columnManyToOneName} = ?<#if columnManyToOneName_has_next>,</#if></#list></#if>" +" WHERE <#list columnOnlyIdList as columnName>${columnName} = ?<#if columnName_has_next> AND </#if></#list>";
		this.getJdbcTemplate().update(
				sql,
				new Object[] { 
				<#list columnWOIdList as columnName><#assign getterMethodName = dbdata.getGetterMethodName(columnFieldWithoutIdMap.get(columnName)) />${pojoNameLower}.${getterMethodName}<#if columnName_has_next>, 
				<#lt/>				</#if></#list><#if !c2j.isComponent(pojo.identifierProperty) && columnWOIdList?has_content && columnFieldManyToOneKeyList?has_content>,</#if>
				<#if !c2j.isComponent(pojo.identifierProperty)><#list columnFieldManyToOneKeyList as columnManyToOneName><#assign getterMethodName = dbdata.getGetterMethodName(columnFieldManyToOneMap.get(columnManyToOneName)) />${pojoNameLower}.${getterMethodName}<#if columnManyToOneName_has_next>, 
				<#lt/>				</#if></#list></#if>,
				<#list columnOnlyIdList as columnName><#assign getterMethodName = dbdata.getGetterMethodName(columnFieldOnlyIdMap.get(columnName)) />${pojoNameLower}.${getterMethodName}<#if columnName_has_next>, </#if></#list> 
				});
    }

	public ${pojo.shortName} get(${pojo.getJavaTypeName(pojo.identifierProperty, jdk5)} ${pojo.identifierProperty.name}) throws Exception {	
		String sql = "SELECT <#list columnList as columnName>${columnName}<#if columnName_has_next>, </#if></#list> "
		              + "FROM ${tableName} WHERE <#list columnOnlyIdList as columnName>${columnName} = ?<#if columnName_has_next> AND </#if></#list>";
		
		return this.getJdbcTemplate().queryForObject(sql,
				new BeanPropertyRowMapper<${pojo.shortName}>(${pojo.shortName}.class) {
					public ${pojo.shortName} mapRow(ResultSet rs, int i)
							throws SQLException {
                        ${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();     
<#foreach field in pojo.getAllPropertiesIterator()>    
    <#foreach column in field.getColumnIterator()>
        <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>        
                        ${pojoNameLower}.${setIdMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));                                                                    
        </#if>                                
    </#foreach>    
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
                        ${identifierType} ${pojo.identifierProperty.name} = new ${identifierType}();
        		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
        		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
        		<#assign setMethodName = 'set' + pojo.getPropertyName(idfield)>
        		<#assign column = dbdata.getColumn(idfield)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(idfield, jdk5) />
					    <#if pojo.getJavaTypeName(idfield, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>          		
                        ${pojo.identifierProperty.name}.${setMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(idfield)}"));            	
          		</#foreach>     		
                        ${pojoNameLower}.${setIdMethodName}(${pojo.identifierProperty.name});
    </#if>          	
</#foreach>  
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>   
        <#if !field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>          
                        ${pojoNameLower}.set${pojo.getPropertyName(field)}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));     
        <#elseif !field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
		  <#assign fieldType = pojo.getJavaTypeName(field, jdk5)>
          <#assign fieldTypeLower = fieldType.substring(0,1).toLowerCase()+fieldType.substring(1)>
                        ${fieldType} ${fieldTypeLower} = new ${fieldType}();        	    	
          <#if columnFieldManyToOneKeyList?has_content>          
            <#list fieldManyToOneKeyList as propertyName>
              <#assign setMethodName = 'set' + propertyName.substring(0,1).toUpperCase()+propertyName.substring(1)>
              <#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>                
                        ${fieldTypeLower}.${setMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));    
            </#list>          
          </#if>    	        	 		
                        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${fieldTypeLower});
        </#if>
    </#foreach>
</#foreach>
						return ${pojoNameLower};
					}
				}, new Object[] { 
		<#if isComponentKey>
				 	<#list columnOnlyIdList as columnName><#assign getterMethodName = dbdata.getIdGetterMethodName(columnFieldOnlyIdMap.get(columnName)) />
					${pojo.identifierProperty.name}.${getterMethodName}<#if columnName_has_next>, </#if></#list> });					 	
		<#else>	
					${pojo.identifierProperty.name}});					 	
		</#if>					
	}

	/**
	 * [WARNING] Don't use below sample codes in real world because of
	 * performance issue. This is a simple example about how to use spring jdbc
	 * pagination.
	 */
	public Page getPagingList(SearchVO searchVO) throws Exception {
        int pageIndex = searchVO.getPageIndex();
		String columnsSql = " <#list columnList as columnName>${pojoNameLower}.${columnName}<#if columnName_has_next>, </#if></#list>";
		String fromSql = " FROM ${tableName} ${pojoNameLower}";
		String orderbySql = " ";	
		orderbySql += "order by ";						
		<#foreach field in pojo.getAllPropertiesIterator()>
		<#if field.equals(pojo.identifierProperty)>
			<#if !c2j.isComponent(field) >
						orderbySql += "${pojoNameLower}.${dbdata.getColumnName(field)}";
			<#else>		        	    
				<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
				<#assign keyList = dbdata.getKeyList(pojo)/>
				<#foreach idfield in keyList.keyList().iterator()>          		 	
					<#assign columnName = dbdata.getColumnName(idfield)>
					<#if keyList.get(idfield) != "FK" >
		    	 <#lt/>				orderbySql += "${pojoNameLower}.${columnName}";
		    	 	<#break/>
		  			</#if>
		  		</#foreach>     			
			</#if>
		</#if>
		</#foreach>					
		

		Page result = fetchPage(
				this.getJdbcTemplate(),
				"SELECT count(*)" + fromSql,
				"SELECT " + columnsSql + fromSql + orderbySql, 
				new Object[] { }, pageIndex,
				new ParameterizedRowMapper<${pojo.shortName}>() {
					public ${pojo.shortName} mapRow(ResultSet rs, int i)
							throws SQLException {
                        ${pojo.shortName} ${pojoNameLower} = new ${pojo.shortName}();     
<#foreach field in pojo.getAllPropertiesIterator()>    
    <#foreach column in field.getColumnIterator()>
        <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>   
                        ${pojoNameLower}.${setIdMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));                                                                    
        </#if>                                
    </#foreach>    
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
                        ${identifierType} ${pojo.identifierProperty.name} = new ${identifierType}();
        		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
        		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
        		<#assign setMethodName = 'set' + pojo.getPropertyName(idfield)>
        		<#assign column = dbdata.getColumn(idfield)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(idfield, jdk5) />
					    <#if pojo.getJavaTypeName(idfield, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>         		
                        ${pojo.identifierProperty.name}.${setMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(idfield)}"));            	
          		</#foreach>     		
                        ${pojoNameLower}.${setIdMethodName}(${pojo.identifierProperty.name});
    </#if>          	
</#foreach>  
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>   
        <#if !field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>         
                        ${pojoNameLower}.set${pojo.getPropertyName(field)}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));     
        <#elseif !field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
		  <#assign fieldType = pojo.getJavaTypeName(field, jdk5)>
          <#assign fieldTypeLower = fieldType.substring(0,1).toLowerCase()+fieldType.substring(1)>
                        ${fieldType} ${fieldTypeLower} = new ${fieldType}();        	    	
          <#if columnFieldManyToOneKeyList?has_content>          
            <#list fieldManyToOneKeyList as propertyName>
              <#assign setMethodName = 'set' + propertyName.substring(0,1).toUpperCase()+propertyName.substring(1)>
              <#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
					    <#assign rsGetterMethodName = 'get' + pojo.getJavaTypeName(field, jdk5) />
					    <#if pojo.getJavaTypeName(field, jdk5) == "Integer">
					  		<#assign rsGetterMethodName = 'getInt' />
					    </#if>               
                        ${fieldTypeLower}.${setMethodName}(rs.${rsGetterMethodName}("${dbdata.getColumnName(field)}"));    
            </#list>          
          </#if>    	        	 		
                        ${pojoNameLower}.set${pojo.getPropertyName(field)}(${fieldTypeLower});
        </#if>
    </#foreach>
</#foreach>
						return ${pojoNameLower};
					}
				});

		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Page fetchPage(final JdbcTemplate jt, final String sqlCountRows,
			final String sqlFetchRows, final Object args[], final int pageNo,
			final ParameterizedRowMapper<${pojo.shortName}> rowMapper) {

		// determine how many rows are available
		final int rowCount = jt.queryForInt(sqlCountRows, args);

		// create the page object
		final Page page = new Page(new ArrayList<${pojo.shortName}>(), pageNo, rowCount,
				pageUnit, pageSize);

		// fetch a single page of results
		final int startRow = (pageNo - 1) * pageSize;
		jt.query(sqlFetchRows, args, new ResultSetExtractor() {
			public Object extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				final List<${pojo.shortName}> pageItems = (List<${pojo.shortName}>) page.getList();
				int currentRow = 0;
				while (rs.next() && currentRow < startRow + pageSize) {
					if (currentRow >= startRow) {
						pageItems.add(rowMapper.mapRow(rs, currentRow));
					}
					currentRow++;
				}
				return page;
			}
		});
		return page;
	}	
}
