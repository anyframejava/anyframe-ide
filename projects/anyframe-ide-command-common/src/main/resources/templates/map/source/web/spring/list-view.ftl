<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numFieldSet = keyAndRequiredFieldList.get("NUM") />
<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/taglibs.jsp"%>
<head>
    <%@ include file="/sample/common/meta.jsp" %>
    <title><spring:message code="${pojoNameLower}List.title"/></title>
	<meta name="heading" content="<spring:message code='${pojoNameLower}List.heading'/>"/>    
	<link rel="stylesheet" href="<c:url value='/sample/css/admin.css'/>" type="text/css">
    <script type="text/javascript" src="<c:url value='/sample/javascript/CommonScript.js'/>"></script>    
	<script type="text/javascript">
		function fncCreate${pojo.shortName}View() {
			document.location.href="<c:url value='/${pojoNameLower}.do?method=createView'/>";
		}	
		function fncSearch${pojo.shortName}() {
			<#if (numFieldSet?size > 0) > 		
			var conditions = [<#list numFieldSet as numField>"${numField.name}"<#if numField_has_next>,</#if></#list>]	
			if(!listFormValidation(conditions)) return;				
			</#if>
		   	document.searchForm.action="<c:url value='/${pojoNameLower}.do?method=list'/>";
		   	document.searchForm.submit();						
		}		
	</script>
</head>
<!--************************** begin of contents *****************************-->
<!--begin of title-->
<table width="100%" height="24" border="0" cellpadding="0" cellspacing="0">
	<tr>
		<td height="24">
		<table width="100%" height="24" border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td height="24" class="ct_ttl01" style="padding-left: 12px">Search List of ${pojo.shortName}</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
<!--end of title-->
   
<form:form modelAttribute="search" method="post" name="searchForm">
<!--begin of search-->
<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;vertical-align: center;">
	<tr>
		<td align="right">
			<form:select path="map['searchCondition']" id="searchCondition" cssClass="ct_input_g" cssStyle="width:114px;">
				<form:option value="All">ALL</form:option>
<#foreach field in pojo.getAllPropertiesIterator()>    
    <#foreach column in field.getColumnIterator()>
        <#if field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
            <#lt/>				<form:option value="${field.name}"><spring:message code="${pojoNameLower}.${field.name}"/></form:option>     
        </#if>
    </#foreach>    
    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
            <#lt/>       
        		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >       		
        		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
            <#lt/>				<form:option value="${field.name}.${idfield.name}"><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}"/></form:option>            	
          		</#foreach>     		
    </#if>          	
</#foreach> 
<#foreach field in pojo.getAllPropertiesIterator()>
    <#foreach column in field.getColumnIterator()>
        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
            <#lt/>				<form:option value="${field.name}"><spring:message code="${pojoNameLower}.${field.name}"/></form:option>
        </#if>
    </#foreach>
</#foreach>								
			</form:select>
			<form:input path="map['searchKeyword']" id="searchKeyword" cssClass="ct_input_g" cssErrorClass="text medium error" maxlength="255"/>
		</td>
		<td align="right" width="80">
			<table border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td width="17" height="23"><img src="<c:url value='/sample/images/ct_btnbg01.gif'/>" width="17" height="23"></td>
					<td background="<c:url value='/sample/images/ct_btnbg02.gif'/>" class="ct_btn01" style="padding-top:3px;">
						<a href="javascript:fncSearch${pojo.shortName}();">Search</a>
					</td>
					<td width="14" height="23"><img src="<c:url value='/sample/images/ct_btnbg03.gif'/>" width="14" height="23"></td>
				</tr>
			</table>
		</td>
	</tr>
</table>

<!--end of search-->
<table class="scrollTable" width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;">
	<thead>
		<tr>
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if !c2j.isComponent(field) >
    	<#lt/>			<th scope="col" style="border-right: 1px #CCCCCC solid"><spring:message code="${pojoNameLower}.${field.name}" /></th>
	<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
    	<#lt/>			<th scope="col" style="border-right: 1px #CCCCCC solid"><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}" /></th>
  			</#if>
  		</#foreach>     			
	</#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#if field.value.typeName == "java.util.Date" || field.value.typeName == "java.sql.Date">
        <#lt/>			<th scope="col" style="border-right: 1px #CCCCCC solid"><spring:message code="${pojoNameLower}.${field.name}" /></th>
    <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
        <#lt/>			<th scope="col" style="border-right: 1px #CCCCCC solid"><spring:message code="${pojoNameLower}.${field.name}" /></th>
    <#else>
        <#lt/>			<th scope="col" style="border-right: 1px #CCCCCC solid"><spring:message code="${pojoNameLower}.${field.name}" /></th>
    </#if>
</#if>
</#foreach>
		</tr>
	</thead>		
	<tbody>
		<c:forEach var="${pojoNameLower}" items="${r"$"}{${pojoNameLower}List}">
		<tr class="board" onMouseOver="this.style.backgroundColor='#e4eaff';return true;" onMouseOut="this.style.backgroundColor=''; return true;" >			
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if !c2j.isComponent(field) >
    	<#lt/>			<td class="underline">
				<a class="linkClass" href="${r"${ctx}"}/${pojoNameLower}.do?method=get&map[${field.name}]=${r"$"}{${pojoNameLower}.${field.name}}">${r"$"}{${pojoNameLower}.${field.name}}</a>
			</td>
	<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
    	<#lt/>			<td class="underline">
				<a class="linkClass" href="${r"${ctx}"}/${pojoNameLower}.do?method=get<#foreach idfield in pojoIdentifier.getPropertyIterator()>&map[${idfield.name}]=${'$'}{${pojoNameLower}.${idfield.name}}</#foreach>">${r"$"}{${pojoNameLower}.${idfield.name}}</a>
			</td>
  			</#if>
  		</#foreach>     			
	</#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#if field.value.typeName == "java.util.Date" || field.value.typeName == "java.sql.Date">
        <#lt/>			<td align="left"><fmt:formatDate value="${'$'}{${pojoNameLower}.${field.name}}" pattern="${'$'}{datePattern}"/></td>
    <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
        <#lt/>			<td align="left"><input type="checkbox" disabled="disabled" <c:if test="${'$'}{${pojoNameLower}.${field.name}}">checked="checked"</c:if>/></td>
    <#else>
        <#lt/>			<td align="left">${r"$"}{${pojoNameLower}.${field.name}}</td>
    </#if>
</#if>
</#foreach>
			</tr>
		</c:forEach>
	</tbody>
</table>

	<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top:10px;">
		<tr>
			<td class="page" height="50" align="center">
				<anyframe:pagenavigator linkUrl="javascript:fncSearch${pojo.shortName}();"
					pages="${r"${resultPage}"}" 
					firstImg="${r"${ctx}"}/sample/images/pagenav/page_before1.gif" 
					lastImg="${r"${ctx}"}/sample/images/pagenav/page_after1.gif" 
					prevImg="${r"${ctx}"}/sample/images/pagenav/page_before.gif" 
					nextImg="${r"${ctx}"}/sample/images/pagenav/page_after.gif"/>
			</td>
		</tr>
	</table>

	<table width="100%" border="0" cellspacing="0" cellpadding="0" style="margin-top: 10px;">
		<tr>
			<td align="right"><a href='<c:url value="javascript:fncCreate${pojo.shortName}View();"/>'><img
				src="<c:url value='/sample/images/btn_add.png'/>" width="64" height="18" border="0" /></a></td>
		</tr>
	</table>
</form:form>
