<#if packageName!=''>package ${packageName};

</#if>
import org.springframework.stereotype.Repository;
<#if superClassName!=''>import ${superClassName};</#if>

<#if classDescription!=''>
/**
 * ${classDescription}
 */
 </#if>
@Repository("${repository}")
public class ${className} <#if superClassName!=''>extends ${superClassShortName}</#if>{	
}