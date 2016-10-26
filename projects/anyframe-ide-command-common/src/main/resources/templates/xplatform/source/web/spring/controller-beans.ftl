<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<!--${pojo.shortName}-START-->
	<bean name ="/${pojoNameLower}XP.do" class="${package}.web.${pojo.shortName}XPController"/>
	<mvc:view-controller path="/${pojoNameLower}XPView.do" view-name="redirect:/xp-query/extends/XPlatformAX.jsp?xpid=${pojoNameLower}/list${pojo.shortName}.xfdl" />   
<!--${pojo.shortName}-END-->
<!--Add additional controller beans here-->