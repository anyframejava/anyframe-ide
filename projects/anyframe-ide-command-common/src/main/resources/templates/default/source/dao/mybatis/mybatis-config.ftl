<!--${pojo.shortName}-START-->
    <typeAlias alias="${pojo.shortName}" type="${pojo.packageName}.${pojo.shortName}" />			
<#if c2j.isComponent(pojo.identifierProperty) >
    <typeAlias alias="${pojo.getJavaTypeName(pojo.identifierProperty,jdk5)}" type="${pojo.packageName}.${pojo.getJavaTypeName(pojo.identifierProperty,jdk5)}" />				
</#if>
<!--${pojo.shortName}-END-->
<!--Add new alias here-->
