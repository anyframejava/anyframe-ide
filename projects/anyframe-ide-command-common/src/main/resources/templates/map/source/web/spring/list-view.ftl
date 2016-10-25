<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numFieldSet = keyAndRequiredFieldList.get("NUM") />
<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/top.jsp"%>
		<div class="location"><a href="<c:url value='/anyframe.jsp'/>">Home</a> &gt; <a href="<c:url value='/${pojoNameLower}.do?method=list'/>">${pojo.shortName} List</a></div>
    </div>
    <hr />    
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
  	<div id="container">
<!--************************** begin of contents *****************************-->
<form:form modelAttribute="search" method="post" name="searchForm">
    	<div class="cont_top">
        	<h2><spring:message code='${pojoNameLower}List.title'/></h2>
      		<div class="search_list">
                <fieldset>
                    <legend>Search</legend>
                    <label for="searchCondition" class="float_left margin_right5">
					<form:select path="map['searchCondition']" id="searchCondition" cssClass="w_search">
						<form:option value="All">ALL</form:option>
		<#foreach field in pojo.getAllPropertiesIterator()>    
		    <#foreach column in field.getColumnIterator()>
		        <#if field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		            <#lt/>              <form:option value="${field.name}"><spring:message code="${pojoNameLower}.${field.name}"/></form:option>     
		        </#if>
		    </#foreach>    
		    <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>
		            <#lt/>       
		        		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >       		
		        		<#foreach idfield in pojoIdentifier.getPropertyIterator()>               		 	
		            <#lt/>              <form:option value="${field.name}.${idfield.name}"><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}"/></form:option>            	
		          		</#foreach>     		
		    </#if>          	
		</#foreach> 
		<#foreach field in pojo.getAllPropertiesIterator()>
		    <#foreach column in field.getColumnIterator()>
		        <#if !field.equals(pojo.identifierProperty) && !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
		            <#lt/>              <form:option value="${field.name}"><spring:message code="${pojoNameLower}.${field.name}"/></form:option>
		        </#if>
		    </#foreach>
		</#foreach>								
					</form:select>
					</label>
                    <label for="searchKeyword" class="float_left margin_right5">
                    	<form:input path="map['searchKeyword']" id="searchKeyword" cssClass="w_search" cssErrorClass="text medium error" maxlength="255"/></label>
                    <label for="btnSearch" class="float_left">
                    	<input type="image" id="btnSearch" name="searchBtn" alt="Search" onclick="javascript:fncSearch${pojo.shortName}();" src="<c:url value='/sample/images/btn_search_i.gif'/>"/>
                    </label>
                </fieldset>
            </div>
      	</div>
        <div class="list">
      		<table summary="This is list of <spring:message code="${pojoNameLower}List.${pojoNameLower}"/>">
            	<caption><spring:message code="${pojoNameLower}List.title"/></caption>
                <colgroup>
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
    	<#lt/>			<col style="width:15%;" />
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
        <#lt/>			<col style="width:15%;" />
</#if>
</#foreach>                      
                </colgroup>
                <thead>
                    <tr>



<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if !c2j.isComponent(field) >
    	<#lt/>			<th><spring:message code="${pojoNameLower}.${field.name}" /></th>
	<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
    	<#lt/>			<th><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}" /></th>
  			</#if>
  		</#foreach>     			
	</#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
        <#lt/>			<th><spring:message code="${pojoNameLower}.${field.name}" /></th>
</#if>
</#foreach>
		</tr>
	</thead>		
	<tbody>
		<c:forEach var="${pojoNameLower}" items="${r"$"}{${pojoNameLower}List}">
		<tr>			
		
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if !c2j.isComponent(field) >
          <td><a href="${r"${ctx}"}/${pojoNameLower}.do?method=get&amp;map[${field.name}]=${r"$"}{${pojoNameLower}.${field.name}}">${r"$"}{${pojoNameLower}.${field.name}}</a></td>
	<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
          <td><a href="${r"${ctx}"}/${pojoNameLower}.do?method=get<#foreach idfield in pojoIdentifier.getPropertyIterator()>&amp;map[${idfield.name}]=${'$'}{${pojoNameLower}.${idfield.name}}</#foreach>">${r"$"}{${pojoNameLower}.${idfield.name}}</a></td>
  			</#if>
  		</#foreach>     			
	</#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#if field.value.typeName == "java.util.Date" || field.value.typeName == "java.sql.Date">
        <#lt/>			<td class="align_center"><fmt:formatDate value="${'$'}{${pojoNameLower}.${field.name}}" pattern="${'$'}{datePattern}"/></td>
    <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
        <#lt/>			<td class="align_center"><input type="checkbox" disabled="disabled" <c:if test="${'$'}{${pojoNameLower}.${field.name}}">checked="checked"</c:if>/></td>
    <#else>
        <#lt/>			<td>${r"$"}{${pojoNameLower}.${field.name}}</td>
    </#if>
</#if>
</#foreach>
			</tr>
		</c:forEach>
	</tbody>
  </table>
</div>
        
<!--************************** begin of paging navigation/buttons *****************************-->     
        <div class="listunder_container">           
            <div class="list_paging">
                <anyframe:pagenavigator linkUrl="javascript:fncSearch${pojo.shortName}();" pages="${r"${resultPage}"}"/>
            </div>
            <div class="list_underbtn_right">
                <a href="javascript:fncCreate${pojo.shortName}View();">
                <span class="button default icon">   
                    <span class="add">&nbsp;</span>
                    <span class="none_a txt_num3"><spring:message code="movie.button.add" /></span>
                </span>
                </a>                
            </div>            
        </div>
        </form:form>
	</div>
    <hr />
<%@ include file="/sample/common/bottom.jsp"%>
