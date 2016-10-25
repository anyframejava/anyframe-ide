
# -- ${pojo.shortName}-START
<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<#assign pojoNameLowerForError = pojo.shortName.toLowerCase()>
<#assign columnFieldManyToOneMap = dbdata.getColumnFieldManyToOneMapWithObject(pojo) />
<#assign columnFieldManyToOneKeyList = columnFieldManyToOneMap.keyList() />
<#assign fieldManyToOneKeyList = columnFieldManyToOneMap.valueList() />

<#foreach field in pojo.getAllPropertiesIterator()>
<#if field.equals(pojo.identifierProperty)>
	<#if c2j.isComponent(field) >    
		<#assign pojoIdentifier = pojo.identifierProperty.getValue() />		
		<#foreach idfield in pojoIdentifier.getPropertyIterator()>
${pojoNameLower}.${field.name}.${idfield.name}=${data.getFieldDescription(idfield.name)}		
		</#foreach>	
	</#if>	
</#if>
<#if !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
    <#lt/>${pojoNameLower}.${field.name}=${data.getFieldDescription(field.name)}
<#elseif c2h.isManyToOne(field) && !c2j.isComponent(pojo.identifierProperty)>
    <#if columnFieldManyToOneKeyList?has_content>          
    	<#list fieldManyToOneKeyList as propertyName>
        	<#assign column = columnFieldManyToOneKeyList.get(propertyName_index)>
    <#lt/>${pojoNameLower}.${propertyName}=${propertyName.substring(0,1).toUpperCase()+propertyName.substring(1)}  
    	</#list>
    </#if>	     	        
</#if>
</#foreach>

# -- success messages -- 
success.${pojoNameLowerForError}.create=${pojo.shortName} has been added successfully.
success.${pojoNameLowerForError}.update=${pojo.shortName} has been updated successfully.
success.${pojoNameLowerForError}.delete=${pojo.shortName} has been deleted successfully.

# -- error messages --
error.${pojoNameLowerForError}serviceimpl.create=${pojo.shortName} data not created
error.${pojoNameLowerForError}serviceimpl.create.solution=Enter correct data for mandatory field or enter data according to formats means date format as yyyy-mm-dd
error.${pojoNameLowerForError}serviceimpl.create.reason=Entered incorrect data for ${pojo.shortName}

error.${pojoNameLowerForError}serviceimpl.exists=${pojo.shortName} don't exist
error.${pojoNameLowerForError}serviceimpl.exists.solution=Check ${pojo.shortName} data
error.${pojoNameLowerForError}serviceimpl.exists.reason=Entered non-existent data for ${pojo.shortName}

error.${pojoNameLowerForError}serviceimpl.get=${pojo.shortName} details of  specified ${pojoNameLower} identifier not displayed
error.${pojoNameLowerForError}serviceimpl.get.solution=Enter correct ${pojoNameLower} identifier
error.${pojoNameLowerForError}serviceimpl.get.reason=Entered incorrect ${pojoNameLower} identifier

error.${pojoNameLowerForError}serviceimpl.getpaginglist=${pojo.shortName} List not displayed
error.${pojoNameLowerForError}serviceimpl.getpaginglist.solution=Enter correct value
error.${pojoNameLowerForError}serviceimpl.getpaginglist.reason=Entered incorrect value

error.${pojoNameLowerForError}serviceimpl.remove=${pojo.shortName} information not removed
error.${pojoNameLowerForError}serviceimpl.remove.solution=Remove only unused removable data
error.${pojoNameLowerForError}serviceimpl.remove.reason=Your trying to remove, not removable data or you entered data in incorrect format

error.${pojoNameLowerForError}serviceimpl.update=${pojo.shortName} data not updated
error.${pojoNameLowerForError}serviceimpl.update.solution=Enter correct data for mandatory field or enter data according to formats means date format as yyyy-mm-dd
error.${pojoNameLowerForError}serviceimpl.update.reason=Entered incorrect data for ${pojo.shortName}


# -- ${pojoNameLower} list page --
${pojoNameLower}List.title=${pojo.shortName} List
${pojoNameLower}List.heading=${util.getPluralForWord(pojo.shortName)}
${pojoNameLower}List.${pojoNameLower}=${pojoNameLower}
${pojoNameLower}List.${util.getPluralForWord(pojoNameLower)}=${util.getPluralForWord(pojoNameLower)}

# -- ${pojoNameLower} detail page --
${pojoNameLower}Detail.title=${pojo.shortName} Detail
${pojoNameLower}Detail.heading=${pojo.shortName} Information
# -- ${pojo.shortName}-END