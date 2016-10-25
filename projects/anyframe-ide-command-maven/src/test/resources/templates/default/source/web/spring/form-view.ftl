<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign dateExists = false>
<#assign dataList = dbdata.getDataList(pojo)> 
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMapWithObject(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />
<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/taglibs.jsp"%>
<head>
    <%@ include file="/sample/common/meta.jsp" %>
    <title><spring:message code="${pojoNameLower}Detail.title"/></title>
    <meta name="heading" content="<spring:message code='${pojoNameLower}Detail.heading'/>"/>   
	<link rel="stylesheet" href="<c:url value='/sample/css/admin.css'/>" type="text/css">        
	<script type="text/javascript" src="<c:url value='/sample/javascript/CommonScript.js'/>"></script>
	<script type="text/javascript">
	function fncCreate${pojo.shortName}() {		    		    
        document.${pojoNameLower}Form.action="<c:url value='/${pojoNameLower}.do?method=create'/>";
        document.${pojoNameLower}Form.submit();
	}
	
	function fncUpdate${pojo.shortName}() {	    
        document.${pojoNameLower}Form.action="<c:url value='/${pojoNameLower}.do?method=update'/>";
        document.${pojoNameLower}Form.submit();
	}
	
	function fncRemove${pojo.shortName}(){	
		if(confirmDelete('${pojoNameLower}')) {
		    document.${pojoNameLower}Form.action="<c:url value='/${pojoNameLower}.do?method=remove'/>";
		    document.${pojoNameLower}Form.submit();
		}	    
	}
	</script>         
</head>

<!--************************** begin of contents *****************************-->

<!-- begin of title -->
<table width="100%" height="37" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td background="<c:url value='/sample/images/ct_ttl_img02.gif'/>" width="100%" style="padding-left: 10px;">
		<table width="100%" height="24" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td height="24" class="ct_ttl01" style="padding-left: 12px">		
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
    <#assign idFieldName = field.name>   
</#if>
</#foreach>							

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
				 <#if isComponentKey>
				 	<c:if test="${'$'}{empty ${pojoNameLower}.${idFieldName}.${firstIdfield}}">				 	
				 <#else>	
				 	<c:if test="${'$'}{empty ${pojoNameLower}.${idFieldName}}">
				 </#if>
				 	Add ${pojo.shortName} Information
				 	<c:set var="readonly" value="false"/>
					</c:if>
			
				 <#if isComponentKey>	
					<c:if test="${'$'}{not empty ${pojoNameLower}.${idFieldName}.${firstIdfield}}">					
				 <#else>
				    <c:if test="${'$'}{not empty ${pojoNameLower}.${idFieldName}}">	
				 </#if>	
					Update ${pojo.shortName} Information
					<c:set var="readonly" value="true"/>				 
					</c:if>					 				 
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>

<form:form modelAttribute="${pojoNameLower}" method="post" action="${pojoNameLower}.do" id="${pojoNameLower}Form" name="${pojoNameLower}Form">
<#rt/>
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
    <#assign idFieldName = field.name>
    <#if field.value.identifierGeneratorStrategy == "assigned">
        <#lt/>       
    <table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top: 13px;">

		<#if !c2j.isComponent(field) >
		<tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>
		<tr>		
			<td width="150" class="ct_td"><spring:message code="${pojoNameLower}.${field.name}"/>*</td><td bgcolor="D6D6D6" width="1"></td>
			<td class="ct_write01">
				<form:input path="${field.name}" cssClass="ct_input_g" cssErrorClass="text medium error" readonly="${'$'}{readonly}"/>
				<form:errors path="${field.name}" cssClass="errors" />
			</td>			
		</tr>
		<#else>
			<#assign pojoIdentifier = pojo.identifierProperty.getValue() >		
			<#assign keyList = dbdata.getKeyList(pojo)>
			<#foreach idfield in keyList.keyList().iterator()>
		<tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>
		<tr>		
			<td width="150" class="ct_td"><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}"/>*</td><td bgcolor="D6D6D6" width="1"></td>
			<td class="ct_write01">
				<form:input path="${field.name}.${idfield.name}" cssClass="ct_input_g" cssErrorClass="text medium error" readonly="${'$'}{readonly}" />
				<form:errors path="${field.name}.${idfield.name}" cssClass="errors" />		
			</td>			
		</tr>		
			</#foreach>
		</#if>
    <#else>
		<#if !c2j.isComponent(field) >    
        <#lt/><form:hidden path="${field.name}"/>
        <#lt/><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top: 13px;">
        <#else>
			<#assign pojoIdentifier = pojo.identifierProperty.getValue() >		
			<#assign keyList = dbdata.getKeyList(pojo)>
			<#foreach idfield in keyList.keyList().iterator()>
        <#lt/><form:hidden path="${field.name}.${idfield.name}"/>
			</#foreach>    
			<#lt/><table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top: 13px;">    
        </#if>
    </#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#foreach column in field.getColumnIterator()>
		<tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>
		<tr>
			<td width="150" class="ct_td">
			
			<spring:message code="${pojoNameLower}.${field.name}"/><#if !column.nullable >*</#if></td>
			<td bgcolor="D6D6D6" width="1"></td>
			<td class="ct_write01">									        
	        <#if field.value.typeName == "java.util.Date" || field.value.typeName == "date" || field.value.typeName == "java.sql.Date" ||field.value.typeName == "timestamp" || field.value.typeName == "time">	        
	        <#assign dateExists = true/>
	        	<form:input path="${field.name}" cssClass="ct_input_g" size="11"/>
	        	<form:errors path="${field.name}" cssClass="errors" />	        	
	        	<c:if test="${'$'}{empty ${pojoNameLower}.${field.name}}">
					<anyframe:validate id="${field.name}" type="Date" value="currentDate" <#if !column.nullable >required="true"</#if>/>
				</c:if>
				<c:if test="${'$'}{not empty ${pojoNameLower}.${field.name}}">
					<anyframe:validate id="${field.name}" type="Date" <#if !column.nullable >required="true"</#if>/>
				</c:if>
					        	
	        <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
	        	<form:checkbox path="${field.name}" cssClass="checkbox"/>    
	        	<form:errors path="${field.name}" cssClass="errors" />
			<#elseif field.value.typeName == "[B">
	        	<form:input path="${field.name}" cssClass="ct_input_g" cssErrorClass="text medium error"<#if (column.length > 0)></#if>/>
	        	<form:errors path="${field.name}" cssClass="errors" />
	        <#else>
	        	<form:input path="${field.name}" cssClass="ct_input_g" cssErrorClass="text medium error"<#if (column.length > 0)> maxlength="${column.length?c}"</#if>/>
	        	<form:errors path="${field.name}" cssClass="errors" />
	        </#if>	        
			</td>
		</tr>
    </#foreach>
<#elseif c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
    <#if columnFieldManyToOneKeyList?has_content>          
    	<#list fieldManyToOneKeyList as propertyName>
        	<#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
        	<#assign fieldType = pojo.getJavaTypeName(field, jdk5)>
          	<#assign fieldTypeLower = fieldType.substring(0,1).toLowerCase()+fieldType.substring(1)>
		<tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>
		<tr>
			<td width="150" class="ct_td">				
			<spring:message code="${pojoNameLower}.${propertyName}"/><#if !column.nullable >*</#if></td>
			<td bgcolor="D6D6D6" width="1"></td>
			<td class="ct_write01">									        
	        <#if field.value.typeName == "java.util.Date" || field.value.typeName == "date" || field.value.typeName == "java.sql.Date" ||field.value.typeName == "timestamp" || field.value.typeName == "time">	        
	        <#assign dateExists = true/>
	        	<form:input path="${fieldTypeLower}.${propertyName}" cssClass="ct_input_g" size="11"/>
	        	<form:errors path="${fieldTypeLower}.${propertyName}" cssClass="errors" />	        	
	        	<c:if test="${'$'}{empty ${pojoNameLower}.${field.name}}">
					<anyframe:validate id="${field.name}" type="Date" value="currentDate" <#if !column.nullable >required="true"</#if>/>
				</c:if>
				<c:if test="${'$'}{not empty ${pojoNameLower}.${field.name}}">
					<anyframe:validate id="${field.name}" type="Date" <#if !column.nullable >required="true"</#if>/>
				</c:if>
					        	
	        <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
	        	<form:checkbox path="${fieldTypeLower}.${propertyName}" cssClass="checkbox"/>    
	        	<form:errors path="${fieldTypeLower}.${propertyName}" cssClass="errors" />
			<#elseif field.value.typeName == "[B">
	        	<form:input path="${fieldTypeLower}.${propertyName}" cssClass="ct_input_g" cssErrorClass="text medium error"<#if (column.length > 0)></#if>/>
	        	<form:errors path="${fieldTypeLower}.${propertyName}" cssClass="errors" />
	        <#else>
	        	<form:input path="${fieldTypeLower}.${propertyName}" cssClass="ct_input_g" cssErrorClass="text medium error"<#if (column.length > 0)> maxlength="${column.length?c}"</#if>/>
	        	<form:errors path="${fieldTypeLower}.${propertyName}" cssClass="errors" />
	        </#if>	        
			</td>
		</tr>   
		</#list>          
	</#if>
	 </#if>
</#foreach>
	<tr><td height="1" colspan="3" bgcolor="D6D6D6"></td></tr>
	<input type="hidden" name="rootPath" value="<c:url value='/'/>"/>		
</table>

<!-- begin of button -->
<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;">
	<tr>
			<td height="24" colspan="2" align="center">	
					<#if isComponentKey>
					<c:if test="${'$'}{empty ${pojoNameLower}.${idFieldName}.${firstIdfield}}">
					<#else>
					<c:if test="${'$'}{empty ${pojoNameLower}.${idFieldName}}">	
					</#if>		
					  <a href="javascript:fncCreate${pojo.shortName}();"><img src="<c:url value='/sample/images/btn_add.png'/>" width="64" height="18" border="0" /></a>
        			</c:if>
        					
					<#if isComponentKey>
					<c:if test="${'$'}{not empty ${pojoNameLower}.${idFieldName}.${firstIdfield}}">
					<#else>
					<c:if test="${'$'}{not empty ${pojoNameLower}.${idFieldName}}">
					</#if>        												        		
					  <a href="javascript:fncUpdate${pojo.shortName}();"><img src="<c:url value='/sample/images/btn_update.png'/>" width="64" height="18" border="0" /></a>
					  <a href="javascript:fncRemove${pojo.shortName}();"><img src="<c:url value='/sample/images/btn_delete.png'/>" width="64" height="18" border="0" /></a>
        			</c:if>
		</td>
	</tr>
</table>

</form:form>