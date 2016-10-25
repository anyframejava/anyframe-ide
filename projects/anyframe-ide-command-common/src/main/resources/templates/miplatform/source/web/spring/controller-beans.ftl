<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<!--${pojo.shortName}-START-->
	<bean name ="/${pojoNameLower}MiP.do" class="${package}.web.${pojo.shortName}MiPController"/>
	<mvc:view-controller path="/${pojoNameLower}MiPView.do" view-name="redirect:/mip-query/extends/mipQuery.jsp?mipid=${pojoNameLower}/list${pojo.shortName}.xml" />   
<!--${pojo.shortName}-END-->
<!--Add additional controller beans here-->