<#assign pojoNameLowerForError = pojo.shortName.toLowerCase()>
# -- ${pojo.shortName}-START
<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
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
</#if>
</#foreach>

# -- success messages -- 
${pojoNameLower}.added=${pojo.shortName} has been added successfully.
${pojoNameLower}.updated=${pojo.shortName} has been updated successfully.
${pojoNameLower}.deleted=${pojo.shortName} has been deleted successfully.

# -- error messages --
error.${pojoNameLowerForError}xpserviceimpl.saveall=${pojo.shortName} data not saved
error.${pojoNameLowerForError}xpserviceimpl.saveall.solution=Enter correct data for mandatory field or enter data according to formats means date format as yyyy-mm-dd
error.${pojoNameLowerForError}xpserviceimpl.saveall.reason=Entered incorrect data for ${pojo.shortName}

error.${pojoNameLowerForError}xpserviceimpl.get=${pojo.shortName} details of  specified ${pojoNameLower} identifier not displayed
error.${pojoNameLowerForError}xpserviceimpl.get.solution=Enter correct ${pojoNameLower} identifier
error.${pojoNameLowerForError}xpserviceimpl.get.reason=Entered incorrect ${pojoNameLower} identifier

error.${pojoNameLowerForError}xpserviceimpl.getlist=${pojo.shortName} List not displayed
error.${pojoNameLowerForError}xpserviceimpl.getlist.solution=Enter correct value
error.${pojoNameLowerForError}xpserviceimpl.getlist.reason=Entered incorrect value

error.${pojoNameLowerForError}xpserviceimpl.remove=${pojo.shortName} information not removed
error.${pojoNameLowerForError}xpserviceimpl.remove.solution=Remove only unused removable data
error.${pojoNameLowerForError}xpserviceimpl.remove.reason=Your trying to remove, not removable data or you entered data in incorrect format

# -- ${pojoNameLower} list page --
${pojoNameLower}List.title=${pojo.shortName} List
${pojoNameLower}List.heading=${util.getPluralForWord(pojo.shortName)}
${pojoNameLower}List.${pojoNameLower}=${pojoNameLower}
${pojoNameLower}List.${util.getPluralForWord(pojoNameLower)}=${util.getPluralForWord(pojoNameLower)}

# -- ${pojoNameLower} detail page --
${pojoNameLower}Detail.title=${pojo.shortName} Detail
${pojoNameLower}Detail.heading=${pojo.shortName} Information
# -- ${pojo.shortName}-END