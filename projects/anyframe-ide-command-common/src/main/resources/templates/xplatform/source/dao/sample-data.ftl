<!--${pojo.shortName}-START-->
	<#assign rows = 1 .. 4>
	<#foreach num in rows>
		<#assign sampleDataSet = dbdata.getSampleDataSet(pojo) />	
		
		<#foreach tablenameTmp in sampleDataSet.keyList().iterator()>					
			<#assign tablename = tablenameTmp?substring(tablenameTmp?index_of(":") + 1) />		
			<${tablename}
			<#assign columnSet = sampleDataSet.get(tablenameTmp) />		
			<#foreach column in columnSet.keyList().iterator()>
				${column}="${columnSet.get(column)}"
			</#foreach>
			/>	
	    </#foreach>
	</#foreach>	
	<!--${pojo.shortName}-END-->
</dataset>		
  

      