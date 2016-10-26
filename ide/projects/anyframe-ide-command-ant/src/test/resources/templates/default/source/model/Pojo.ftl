${pojo.getPackageDeclaration()}

<#assign classbody>
<#include "PojoTypeDeclaration.ftl" encoding="UTF-8"/> {
<#if !pojo.isInterface()>
<#include "PojoFields.ftl" encoding="UTF-8"/>
<#--include "PojoConstructors.ftl" encoding="UTF-8"/-->

<#include "PojoPropertyAccessors.ftl" encoding="UTF-8"/>

<#--include "PojoEqualsHashcode.ftl" encoding="UTF-8"/-->

<#include "PojoToString.ftl" encoding="UTF-8"/>

<#else>
<#include "PojoInterfacePropertyAccessors.ftl" encoding="UTF-8"/>

</#if>
<#include "PojoExtraClassCode.ftl" encoding="UTF-8"/>

}
</#assign>

${pojo.generateImports()}

import java.io.Serializable;
<#foreach field in pojo.getAllPropertiesIterator()><#rt/>
	<#if pojo.getMetaAttribAsBool(field, "gen-property", true)><#rt/>
		<#if "Date" == pojo.getJavaTypeName(field, jdk5)>
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
		<#break/>
		</#if>
	</#if><#rt/>
</#foreach><#rt/> 

${classbody}