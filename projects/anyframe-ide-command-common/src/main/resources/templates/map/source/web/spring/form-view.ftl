<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign dateExists = false>
<#assign dataList = dbdata.getDataList(pojo)> 
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMapWithObject(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />
<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/top.jsp"%>
		<div class="location"><a href="<c:url value='/anyframe.jsp'/>">Home</a> &gt; <a href="<c:url value='/${pojoNameLower}.do?method=list'/>">${pojo.shortName} List</a></div>
    </div>
    <hr />
<script type="text/javascript" src="<c:url value='/sample/javascript/InputCalendar.js'/>"></script>
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
<!--************************** begin of contents *****************************-->
    <div id="container">
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
    	<div class="cont_top">
        	<h2>
				 <#if isComponentKey>
				 	<c:if test="${'$'}{empty ${pojoNameLower}.map.${firstIdfield}}">				 	
				 <#else>	
				 	<c:if test="${'$'}{empty ${pojoNameLower}.map.${idFieldName}}">
				 </#if>
				 	Add ${pojo.shortName} Information
				 	<c:set var="readonly" value="false"/>
					</c:if>
			
				 <#if isComponentKey>	
					<c:if test="${'$'}{not empty ${pojoNameLower}.map.${firstIdfield}}">					
				 <#else>
				    <c:if test="${'$'}{not empty ${pojoNameLower}.map.${idFieldName}}">	
				 </#if>	
					Update ${pojo.shortName} Information
					<c:set var="readonly" value="true"/>				 
					</c:if>					 				 
			</h2>
        </div>
        <div class="view">							
		<form:form modelAttribute="${pojoNameLower}" method="post" action="${pojoNameLower}.do" id="${pojoNameLower}Form" name="${pojoNameLower}Form">
<#rt/>
	 	<table summary="This table shows detail information about the ${pojoNameLower}">
    	<caption>Detail information</caption>
        <colgroup>
        	<col style="width:20%;" />
            <col style="width:80%;" />
        </colgroup>
        <tbody>
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
    <#assign idFieldName = field.name>
    <#if field.value.identifierGeneratorStrategy == "assigned">
        <#lt/>       
	<#if !c2j.isComponent(field) >
   		<tr>
        	<th><label for="${field.name}"><spring:message code="${pojoNameLower}.${field.name}" />&nbsp;*</label></th>
            <td><form:input path="map[${field.name}]" id="${field.name}" cssClass="w_normal" readonly="${'$'}{readonly}"/><form:errors path="map[${field.name}]" cssClass="errors" /></td>
        </tr>
		<#else>
			<#assign pojoIdentifier = pojo.identifierProperty.getValue() >		
			<#assign keyList = dbdata.getKeyList(pojo)>
			<#foreach idfield in keyList.keyList().iterator()>
    	<tr>
        	<th><label for="${idfield.name}"><spring:message code="${pojoNameLower}.${field.name}.${idfield.name}" />&nbsp;*</label></th>
            <td><form:input path="map[${idfield.name}]" id="${idfield.name}" cssClass="w_normal" readonly="${'$'}{readonly}" /><form:errors path="map[${idfield.name}]" cssClass="errors" /></td>
        </tr>					
			</#foreach>
		</#if>
    <#else>
		<#if !c2j.isComponent(field) >    
	        <#lt/><form:hidden path="map[${field.name}]"/>
	        <#lt/>
        <#else>
			<#assign pojoIdentifier = pojo.identifierProperty.getValue() >		
			<#assign keyList = dbdata.getKeyList(pojo)>
			<#foreach idfield in keyList.keyList().iterator()>
        <#lt/><form:hidden path="map[${idfield.name}]"/>
			</#foreach>    
			<#lt/>   
        </#if>
    </#if>
<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#foreach column in field.getColumnIterator()>
    	<tr>
        	<th><label for="${field.name}"><spring:message code="${pojoNameLower}.${field.name}" /><#if !column.nullable >&nbsp;*</#if></label></th>
            <td>
	        <#if field.value.typeName == "java.util.Date" || field.value.typeName == "date" || field.value.typeName == "java.sql.Date" || field.value.typeName == "time">	        
	        <#assign dateExists = true/>
	        	<form:input path="map[${field.name}]" id="${field.name}" cssClass="w_date" maxlength="10"/>	   
	        	<a class="underline_none" href="javascript:popUpCalendar(document.${pojoNameLower}Form.${field.name}, 'yyyy-mm-dd');"><img src="<c:url value='/sample/images/btn_calendar_i.gif'/>" alt="Calendar"/></a>
	        	<form:errors path="map[${field.name}]" cssClass="errors" />	   	        	
	        <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
		        <form:checkbox path="map[${field.name}]" id="${field.name}" cssClass="checkbox" value="false"/>        
		        <form:errors path="map[${field.name}]" cssClass="errors" />
		        <anyframe:validate id="${field.name}" type="CheckBox" checked="${'$'}{${pojoNameLower}.map.${field.name} !=null ? ${pojoNameLower}.map.${field.name} : false}"/>
			<#elseif field.value.typeName == "[B">
		        <form:input path="map[${field.name}]" id="${field.name}" cssClass="w_normal" <#if (column.length > 0)></#if>/>
		        <form:errors path="map[${field.name}]" cssClass="errors" />
	        <#else>
		        <form:input path="map[${field.name}]" id="${field.name}" cssClass="w_normal" <#if (column.length > 0)> maxlength="${column.length?c}"</#if>/>
		        <form:errors path="map[${field.name}]" cssClass="errors" />
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
    	<tr>
        	<th><label for="${propertyName}"><spring:message code="${pojoNameLower}.${propertyName}" /><#if !column.nullable >&nbsp;*</#if></label></th>
			<td>
			<#if field.value.typeName == "java.util.Date" || field.value.typeName == "date" || field.value.typeName == "java.sql.Date"  || field.value.typeName == "time">	        
	        <#assign dateExists = true/>
	        <form:input path="map[${propertyName}]" id="${propertyName}" cssClass="w_date" maxlength="10"/>	        
	        <a class="underline_none" href="javascript:popUpCalendar(document.${pojoNameLower}Form.${propertyName}, 'yyyy-mm-dd');"><img src="<c:url value='/sample/images/btn_calendar_i.gif'/>" alt="Calendar"/></a>
	        <form:errors path="map[${propertyName}]" cssClass="errors" />	        	
	        <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
	        <form:checkbox path="map[${propertyName}]" id="${field.name}" cssClass="checkbox" value="false"/>        
	        <form:errors path="map[${propertyName}]" cssClass="errors" />
	        <anyframe:validate id="${field.name}" type="CheckBox" checked="${'$'}{${pojoNameLower}.map.${field.name} !=null ? ${pojoNameLower}.map.${field.name} : false}"/>
			<#elseif field.value.typeName == "[B">
	        <form:input path="map[${propertyName}]" id="${field.name}" cssClass="w_normal" <#if (column.length > 0)></#if>/>
	        <form:errors path="map[${propertyName}]" cssClass="errors" />
	        <#else>
	        <form:input path="map[${propertyName}]" id="${field.name}" cssClass="ct_input_g" <#if (column.length > 0)> maxlength="${column.length?c}"</#if>/>
	        <form:errors path="map[${propertyName}]" cssClass="errors" />
	        </#if>
			</td>
		</tr>   
		</#list>          
	</#if>
</#if>
</#foreach>	
	</tbody>
    </table>
	<input type="hidden" name="rootPath" value="<c:url value='/'/>"/>	
</form:form>
</div>

<!--************************** begin of buttons *****************************-->
        <div class="btncontainer_center">
	    <a href="<c:url value='/${pojoNameLower}.do?method=list'/>">
	    <span class="button default icon">
	        <span class="list">&nbsp;</span>
	        <span class="none_a txt_num4"><spring:message code="movie.button.list" /></span>
	    </span>
	    </a>         
		<#if isComponentKey>
		<c:if test="${'$'}{empty ${pojoNameLower}.map.${firstIdfield}}">
		<#else>
		<c:if test="${'$'}{empty ${pojoNameLower}.map.${idFieldName}}">	
		</#if>	
		    <a href="javascript:fncCreate${pojo.shortName}();">
		    <span class="button default icon">
		        <span class="add">&nbsp;</span>
		        <span class="none_a txt_num3"><spring:message code="movie.button.add" /></span>
		    </span>
		    </a>
		</c:if> 
		<#if isComponentKey>
		<c:if test="${'$'}{not empty ${pojoNameLower}.map.${firstIdfield}}">
		<#else>
		<c:if test="${'$'}{not empty ${pojoNameLower}.map.${idFieldName}}">
		</#if>   
		    <a href="javascript:fncUpdate${pojo.shortName}();">
		    <span class="button default icon">
		        <span class="update">&nbsp;</span>
		        <span class="none_a txt_num6"><spring:message code="movie.button.update" /></span>
		    </span>
		    </a> 
		    <a href="javascript:fncRemove${pojo.shortName}();">
		    <span class="button default icon">
		        <span class="delete">&nbsp;</span>
		        <span class="none_a txt_num6"><spring:message code="movie.button.remove" /></span>
		    </span>
		    </a> 
		</c:if>		
    	</div>
	</div>
    <hr />
<%@ include file="/sample/common/bottom.jsp"%>