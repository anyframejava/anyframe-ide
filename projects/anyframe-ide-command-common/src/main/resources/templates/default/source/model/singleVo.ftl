<#if packageName!=''>package ${packageName};

</#if>
<#if superClassName!=''>
import ${superClassName};

</#if>
<#if classDescription!=''>
/**
 * ${classDescription} 
 */
</#if>
public class ${className} <#if superClassName!=''>extends ${superClassShortName}</#if>{	
}