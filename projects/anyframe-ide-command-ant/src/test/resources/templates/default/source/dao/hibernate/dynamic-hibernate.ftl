<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign keyAndRequiredFieldList = dbdata.getKeyAndRequiredFieldList(pojo) />
<#assign numFieldSet = keyAndRequiredFieldList.get("NUM") />
<#assign strFieldSet = keyAndRequiredFieldList.get("STR") />		
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE dynamic-hibernate PUBLIC "-//ANYFRAME//DTD DYNAMIC-HIBERNATE//EN"
"http://www.anyframejava.org/dtd/anyframe-dynamic-hibernate-mapping-4.0.dtd">
<dynamic-hibernate>
	<query name="find${pojo.shortName}List">
		<![CDATA[
		FROM ${pojo.shortName} ${pojoNameLower} 
		#if ($keywordNum != "")			
			WHERE 		
			#if ($condition == "All" || $condition == "")
				(
				 <#if (strFieldSet?size > 0) >
				 <#list strFieldSet as strField> ${pojoNameLower}.${dbdata.checkAndGetPropertyName(pojo,strField)} like :keywordStr<#if strField_has_next> or </#if></#list>
				 </#if>						
				 <#if (numFieldSet?size > 0) >			 			 
				 #if($isNumeric == "true")
				 	or <#list numFieldSet as numField>${pojoNameLower}.${dbdata.checkAndGetPropertyName(pojo,numField)} = {{keywordNum}} <#if numField_has_next> or </#if></#list>
				 #end
				 </#if>		
				)	
			<#if (numFieldSet?size > 0) >
			#elseif(<#list numFieldSet as numField>$condition == "${dbdata.checkAndGetPropertyName(pojo,numField)}"<#if numField_has_next> || </#if></#list>)
				${pojoNameLower}.{{condition}} = {{keywordNum}}			
			</#if>	
			<#if (strFieldSet?size > 0) >
			#elseif(<#list strFieldSet as strField>$condition == "${dbdata.checkAndGetPropertyName(pojo,strField)}"<#if strField_has_next> || </#if></#list>)
				${pojoNameLower}.{{condition}} like :keywordStr			
			</#if>						
			#end
		#end			
			order by							
<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if !c2j.isComponent(field) >
				${pojoNameLower}.${field.name}
	<#else>		        	    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() >
		<#assign keyList = dbdata.getKeyList(pojo)/>
		<#foreach idfield in keyList.keyList().iterator()>          		 	
			<#assign columnName = dbdata.getColumnName(idfield)>
			<#if keyList.get(idfield) != "FK" >
    	 <#lt/>				${pojoNameLower}.${field.name}.${idfield.name}
    	 	<#break/>
  			</#if>
  		</#foreach>     			
	</#if>
</#if>
</#foreach>						
		]]>
	</query>

	<query name="count${pojo.shortName}List">
		<![CDATA[
		SELECT count(*) 
		FROM ${pojo.shortName} ${pojoNameLower} 
		#if ($keywordNum != "")			
			WHERE 		
			#if ($condition == "All" || $condition == "")
				(
				 <#if (strFieldSet?size > 0) >
				 <#list strFieldSet as strField> ${pojoNameLower}.${dbdata.checkAndGetPropertyName(pojo,strField)} like :keywordStr<#if strField_has_next> or </#if></#list>
				 </#if>						
				 <#if (numFieldSet?size > 0) >			 			 
				 #if($isNumeric == "true")
				 	or <#list numFieldSet as numField>${pojoNameLower}.${dbdata.checkAndGetPropertyName(pojo,numField)} = {{keywordNum}} <#if numField_has_next> or </#if></#list>
				 #end
				 </#if>		
				)	
			<#if (numFieldSet?size > 0) >
			#elseif(<#list numFieldSet as numField>$condition == "${dbdata.checkAndGetPropertyName(pojo,numField)}"<#if numField_has_next> || </#if></#list>)
				${pojoNameLower}.{{condition}} = {{keywordNum}}			
			</#if>	
			<#if (strFieldSet?size > 0) >
			#elseif(<#list strFieldSet as strField>$condition == "${dbdata.checkAndGetPropertyName(pojo,strField)}"<#if strField_has_next> || </#if></#list>)
				${pojoNameLower}.{{condition}} like :keywordStr			
			</#if>						
			#end
		#end					
		]]>
	</query>	
</dynamic-hibernate>



