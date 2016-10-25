<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)>
<!--${pojo.shortName}-START-->
			<li><a href="${r"${ctx}"}/${pojoNameLower}.do?method=list">${pojo.shortName} List</a></li>
<!--${pojo.shortName}-END-->		
<!--Add new crud generation menu here-->
