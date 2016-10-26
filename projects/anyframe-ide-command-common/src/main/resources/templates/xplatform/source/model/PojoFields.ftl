		private static final long serialVersionUID = 1L;
<#foreach field in pojo.getAllPropertiesIterator()><#rt/>
	<#if pojo.getMetaAttribAsBool(field, "gen-property", true)><#rt/>
	  <#if c2j.isComponent(field)>
	    <#if "Date" == pojo.getJavaTypeName(field, jdk5)>
  			@DateTimeFormat(iso = ISO.DATE)
  		</#if>
  		${pojo.getFieldModifiers(field)} ${pojo.getJavaTypeName(field, jdk5)} ${field.name}<#if pojo.hasFieldInitializor(field, jdk5)> = ${pojo.getFieldInitialization(field, jdk5)}<#elseif c2j.isComponent(field)> = new ${pojo.getJavaTypeName(field, jdk5)}();</#if>;
	  <#else>
  		<#assign columnJavaType = dbdata.getColumnJavaType(field)>
  		<#if "time" != columnJavaType><#rt/>
  			<#if "Date" == pojo.getJavaTypeName(field, jdk5)>
  			@DateTimeFormat(iso = ISO.DATE)
  			</#if>
  			${pojo.getFieldModifiers(field)} ${pojo.getJavaTypeName(field, jdk5)} ${field.name}<#if pojo.hasFieldInitializor(field, jdk5)> = ${pojo.getFieldInitialization(field, jdk5)}<#elseif c2j.isComponent(field)> = new ${pojo.getJavaTypeName(field, jdk5)}();</#if>;
  		<#else>	
      		${pojo.getFieldModifiers(field)} String ${field.name};
  		</#if>
  	</#if>	    
	</#if><#rt/>
</#foreach><#rt/>