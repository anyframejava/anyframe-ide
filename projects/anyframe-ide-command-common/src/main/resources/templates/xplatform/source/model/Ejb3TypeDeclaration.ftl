<#if ejb3?if_exists><#rt/>
    <#if pojo.isComponent()><#rt/>
    <#lt>@${pojo.importType("javax.persistence.Embeddable")}
    <#else><#rt/>
    <#lt>@${pojo.importType("javax.persistence.Entity")}
    <#lt>@${pojo.importType("javax.persistence.Table")}(name="${clazz.table.name}"<#rt/>
    <#if dbschema><#rt/>
    		<#lt>,schema="${clazz.table.schema}"<#rt/>
    </#if>
    <#assign uniqueConstraint=pojo.generateAnnTableUniqueConstraint()><#rt/>
        <#if uniqueConstraint?has_content><#rt/>
            <#lt/>, uniqueConstraints = ${uniqueConstraint} <#-- bug alert: two unique column names will cause invalid output --><#rt/>
        </#if><#lt/>)
    </#if>
</#if> 