<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<!--${pojo.shortName}-START-->
		<definition name="genList${pojo.shortName}" extends="standardLayoutGen">
        <put-attribute name="body" value="/WEB-INF/jsp/generation/${pojoNameLower}/list.jsp" />
    </definition>
    <definition name="genView${pojo.shortName}" extends="standardLayoutGen">
        <put-attribute name="body" value="/WEB-INF/jsp/generation/${pojoNameLower}/form.jsp" />
    </definition>
<!--${pojo.shortName}-END-->

  <!-- Add new tiles definition here -->