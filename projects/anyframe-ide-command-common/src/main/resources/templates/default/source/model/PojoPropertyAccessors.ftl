<#-- // Property accessors -->
<#foreach property in pojo.getAllPropertiesIterator()>
<#if pojo.getMetaAttribAsBool(property, "gen-property", true)>
	<#if pojo.hasFieldJavaDoc(property)>    
    /**       
     * ${pojo.getFieldJavaDoc(property, 4)}
     */
	</#if>
	<#if c2j.isComponent(property)>
		<#include "GetPropertyAnnotation.ftl" encoding="UTF-8"/>
	${pojo.getPropertyGetModifiers(property)} ${pojo.getJavaTypeName(property, jdk5)} ${pojo.getGetterSignature(property)}() {
        return this.${property.name};
    }
    
    ${pojo.getPropertySetModifiers(property)} void set${pojo.getPropertyName(property)}(${pojo.getJavaTypeName(property, jdk5)} ${property.name}) {
        this.${property.name} = ${property.name};
    }
	<#else>
	  	<#assign columnJavaType = dbdata.getColumnJavaType(property)>
	  	<#include "GetPropertyAnnotation.ftl" encoding="UTF-8"/>
  		<#if "time" != columnJavaType><#rt/>
      ${pojo.getPropertyGetModifiers(property)} ${pojo.getJavaTypeName(property, jdk5)} ${pojo.getGetterSignature(property)}() {
          return this.${property.name};
      }
      
      ${pojo.getPropertySetModifiers(property)} void set${pojo.getPropertyName(property)}(${pojo.getJavaTypeName(property, jdk5)} ${property.name}) {
          this.${property.name} = ${property.name};
      }
      <#else>
      <#assign columnJavaTypeUpper = columnJavaType.substring(0,1).toUpperCase()+columnJavaType.substring(1)>
      ${pojo.getPropertyGetModifiers(property)} String ${pojo.getGetterSignature(property)}() {
          return this.${property.name};
      }
      
      ${pojo.getPropertySetModifiers(property)} void set${pojo.getPropertyName(property)}(String ${property.name}) {
          this.${property.name} = ${property.name};
      }
		</#if>
	</#if> 
</#if>	
</#foreach>
