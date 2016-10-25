<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)> 		
<#assign columnFieldMap = dbdata.getColumnFieldMap(pojo) />
<#assign columnList = columnFieldMap.keyList() />
<#assign fieldList = columnFieldMap.valueList() />
<#assign columnFieldWithoutIdMap = dbdata.getColumnFieldWithoutIdMap(pojo) />
<#assign columnWOIdList = columnFieldWithoutIdMap.keyList() />
<#assign columnFieldTypeWithoutIdMap = dbdata.getColumnDataTypeWithoutIdMap(pojo) />
<#assign keyList = dbdata.getKeyList(pojo)/>
<#assign foreignKeyList = dbdata.getForeignKeyList(pojo)/>
<?xml version="1.0" encoding="utf-8"?>
<Window>
	<Form BorderColor="#58595b" Height="650" Id="${pojoNameLower}_grid_list" Left="8" OnLoadCompleted="${pojoNameLower}_grid_list_OnLoadCompleted" PidAttrib="7" Title="${pojoNameLower}_grid_list" Top="8" Ver="1.0" Width="848" WorkArea="true">
		<Datasets>
			<Dataset DataSetType="Dataset" Id="dsSearch">
				<Contents>
				  <column id="pageIndex" type="INTEGER">1</column>
					<column id="pageSize" type="INTEGER">5</column>
					<column id="pageUnit" type="INTEGER">5</column>
					<colinfo id="SEARCH_CONDITION" size="100" summ="default" type="STRING"/>
					<colinfo id="SEARCH_KEYWORD" size="100" summ="default" type="STRING"/>
					<record>
						<SEARCH_CONDITION></SEARCH_CONDITION>
						<SEARCH_KEYWORD></SEARCH_KEYWORD>
					</record>
				</Contents>
			</Dataset>
			<Dataset DataSetType="Dataset" Id="dsGrid${pojo.shortName}" <#foreach field in pojo.getAllPropertiesIterator()><#if field.equals(pojo.identifierProperty)>OnRowPosChanged="dsGrid${pojo.shortName}_OnRowPosChanged"</#if></#foreach>>
				<Contents>
				  <column id="pageCount" type="INTEGER">0</column>
					<column id="pageIndex" type="INTEGER">0</column>
					<column id="pageSize" type="INTEGER">0</column>
					<column id="totalCount" type="INTEGER">0</column>
					<colinfo id="_chk" size="1" summ="default" type="CHAR"/>
					<#list columnList as columnName>
					<colinfo id="${columnName.toUpperCase()}" size="256" summ="default" type="STRING"/>
					</#list>
				</Contents>
			</Dataset>
			<#foreach field in pojo.getAllPropertiesIterator()>
		        <#if field.equals(pojo.identifierProperty)>
		        	<#if c2j.isComponent(field) >
		        		  <#foreach idfield in keyList.keyList().iterator()>          		 	
		        			<#assign columnName = dbdata.getColumnName(idfield)>
		        			  <#if keyList.get(idfield) == "FK" >
		        			  	<#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
		          				<#assign columnJavaType = foreignKeyList.get(fkfield)/>
			        			  <Dataset DataSetType="Dataset" Id="dsGrid${columnJavaType}">
							          <Contents>
							            <colinfo id="${columnName.toUpperCase()}" size="256" summ="default" type="STRING"/>
							          </Contents>
						          </Dataset> 
					          </#foreach>
		            		</#if>	
		          		</#foreach>  
		          </#if>	
		        </#if>
	      </#foreach>
			<Dataset DataSetType="Dataset" Id="dsCboType">
				<Contents>
					<colinfo id="CODE" size="256" summ="default" type="STRING"/>
					<colinfo id="DATA" size="256" summ="default" type="STRING"/>
					<record>
						<CODE>1</CODE>
						<DATA>공통 Controller+공통Service</DATA>
					</record>
					<record>
						<CODE>2</CODE>
						<DATA>공통Controller+사용자Service</DATA>
					</record>
					<record>
						<CODE>3</CODE>
						<DATA>사용자Controller+사용자Service</DATA>
					</record>
				</Contents>
			</Dataset>
			<Dataset DataSetType="Dataset" Id="dsService">
				<Contents>
					<colinfo id="SVC_ID" size="100" summ="default" type="STRING"/>
					<colinfo id="CONTROLLER" size="256" summ="default" type="STRING"/>
					<colinfo id="QUERY_LIST" size="256" summ="default" type="STRING"/>
					<colinfo id="SERVICE" size="100" summ="default" type="STRING"/>
					<colinfo id="IN_DATASET_LIST" size="256" summ="default" type="STRING"/>
					<colinfo id="OUT_DATASET_LIST" size="256" summ="default" type="STRING"/>
					<colinfo id="CALLBACK" size="100" summ="default" type="STRING"/>
					<colinfo id="SYNC_YN" size="1" summ="default" type="STRING"/>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER></CONTROLLER>
						<IN_DATASET_LIST>dsSearch=dsSearch</IN_DATASET_LIST>
						<OUT_DATASET_LIST>dsGrid${pojo.shortName}=dsResult</OUT_DATASET_LIST>
						<QUERY_LIST></QUERY_LIST>
						<SERVICE>${pojoNameLower}MiPService.getPagingList</SERVICE>
						<SVC_ID>getPagingList_C_U</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER>${pojoNameLower}MiP.do</CONTROLLER>
						<IN_DATASET_LIST>dsSearch=dsSearch</IN_DATASET_LIST>
						<OUT_DATASET_LIST>dsGrid${pojo.shortName}=dsResult</OUT_DATASET_LIST>
						<QUERY_LIST></QUERY_LIST>
						<SERVICE>${pojoNameLower}MiPService.getPagingList</SERVICE>
						<SVC_ID>getPagingList_U_U</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER></CONTROLLER>
						<IN_DATASET_LIST>querySet1=dsSearch</IN_DATASET_LIST>
						<OUT_DATASET_LIST>dsGrid${pojo.shortName}=querySet1</OUT_DATASET_LIST>
						<QUERY_LIST>querySet1=find${pojo.shortName}List</QUERY_LIST>
						<SERVICE></SERVICE>
						<SVC_ID>getPagingList_C_C</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER></CONTROLLER>
						<IN_DATASET_LIST>querySet1=dsGrid${pojo.shortName}:U</IN_DATASET_LIST>
						<OUT_DATASET_LIST></OUT_DATASET_LIST>
						<QUERY_LIST>querySet1=create${pojo.shortName},update${pojo.shortName},remove${pojo.shortName}</QUERY_LIST>
						<SERVICE></SERVICE>
						<SVC_ID>saveAll_C_C</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER></CONTROLLER>
						<IN_DATASET_LIST>dsSave=dsGrid${pojo.shortName}:U</IN_DATASET_LIST>
						<OUT_DATASET_LIST></OUT_DATASET_LIST>
						<QUERY_LIST></QUERY_LIST>
						<SERVICE>${pojoNameLower}MiPService.saveAll</SERVICE>
						<SVC_ID>saveAll_C_U</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER>${pojoNameLower}MiP.do</CONTROLLER>
						<IN_DATASET_LIST>dsSave=dsGrid${pojo.shortName}:U</IN_DATASET_LIST>
						<OUT_DATASET_LIST></OUT_DATASET_LIST>
						<QUERY_LIST></QUERY_LIST>
						<SERVICE>${pojoNameLower}MiPService.saveAll</SERVICE>
						<SVC_ID>saveAll_U_U</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>					
          <#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
          <#assign columnJavaType = foreignKeyList.get(fkfield)/>                      
					<record>
						<CALLBACK></CALLBACK>
						<CONTROLLER></CONTROLLER>
						<IN_DATASET_LIST>querySet1=dsSearch</IN_DATASET_LIST>
						<OUT_DATASET_LIST>dsGrid${columnJavaType}=querySet1</OUT_DATASET_LIST>
						<QUERY_LIST>querySet1=find${columnJavaType}List</QUERY_LIST>
						<SERVICE></SERVICE>
						<SVC_ID>getPagingList${columnJavaType}_C_C</SVC_ID>
						<SYNC_YN></SYNC_YN>
					</record>
      		</#foreach>                  							
				</Contents>
			</Dataset>
		</Datasets>
		<Grid AutoFit="TRUE" BindDataset="dsGrid${pojo.shortName}" ColSizing="TRUE" BkColor2="default" BoldHead="true" Bottom="336" Editable="TRUE" Enable="true" EndLineColor="default" Font="돋움,9" HeadBorder="Flat" HeadHeight="23" Height="118" Id="Grid0" InputPanel="FALSE" Left="8" LineColor="default" MinWidth="100" OnHeadClick="Grid0_OnHeadClick" Right="824" Style="stlGrd" TabOrder="2" TabStop="true" Top="128" UseDBuff="true" UsePopupMenu="true" UseSelColor="true" Visible="true" VLineColor="default" WheelScrollRow="1" Width="816">
			<contents>
				<format id="Default">
					<columns>
					  <col width="44"/>
						<#foreach field in pojo.getAllPropertiesIterator()>  												
              				<#if field.equals(pojo.identifierProperty)>
		              		  	<#foreach idfield in keyList.keyList().iterator()>          		 	              			
		                  			<col width="85"/>                                         
		                		</#foreach>
			            	</#if>          
			            </#foreach>
  						<#list columnWOIdList as columnWOId>
                 			<col width="85"/>           					
			  			</#list>  
					</columns>
					<head>
						<cell col="0" display="checkbox" edit="checkbox" font="돋움,9,Bold"/>  		     
						<#assign headercount = 0>
						<#foreach field in pojo.getAllPropertiesIterator()>  												
			            	<#if field.equals(pojo.identifierProperty)>
				            	<#if c2j.isComponent(field) >
		              		  		<#foreach idfield in keyList.keyList().iterator()>          		 	
				              			<#assign columnName = dbdata.getColumnName(idfield)>
				              			<#if keyList.get(idfield) == "FK" >
				                  			<#assign headercount = headercount + 1> 
				                  			<cell col="${headercount}" color="user11" display="text" text="${columnName.toUpperCase()}"/>                
				                        <#else>
				                        	<#assign headercount = headercount + 1> 
				                            <cell col="${headercount}" color="user11" display="text" text="${columnName.toUpperCase()}"/>                
				                  		</#if>	
		                			</#foreach>
				                <#elseif !c2j.isComponent(field) >
				                	<#foreach idfield in keyList.keyList().iterator()>          		 	
				              			<#assign columnName = dbdata.getColumnName(idfield)> 
				                    	<#assign headercount = headercount + 1>
				                      	<cell col="${headercount}" color="user11" display="text" text="${columnName.toUpperCase()}"/> 
				                    </#foreach>               		                  
				            	</#if>
			            	</#if>          
			            </#foreach>
  						<#list columnWOIdList as columnWOId>
                  			<#assign headercount = headercount + 1>
                 			<cell col="${headercount}" color="user11" display="text" text="${columnWOId.toUpperCase()}"/>                 					
			  			</#list> 
					</head>
					<body>
						<cell align="center" bkcolor="#f7f7f7" col="0" colid="_chk" color="#58595b" display="checkbox" edit="checkbox" font="expr:iif(currow&lt;2,&#32;&apos;bold&apos;)"/>
						<#assign datacount = 0>
						<#foreach field in pojo.getAllPropertiesIterator()>  												
		              		<#if field.equals(pojo.identifierProperty)>
		              			<#if c2j.isComponent(field) >
		              		  		<#foreach idfield in keyList.keyList().iterator()>          		 	
		              					<#assign columnName = dbdata.getColumnName(idfield)>
		              			  		<#if keyList.get(idfield) == "FK" >
		              			  			<#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
					          					<#assign columnJavaType = foreignKeyList.get(fkfield)/>
					                  			<#assign datacount = datacount + 1>    
		              			  				<cell bkcolor="#f7f7f7" col="${datacount}" colid="${columnName.toUpperCase()}" color="#58595b" display="text"/>
		              			  			</#foreach>	
		                      			<#else>
		                          			<#assign datacount = datacount + 1>    
		                         			<cell bkcolor="#f7f7f7" col="${datacount}" colid="${columnName.toUpperCase()}" color="#58595b" display="text"/>		       	  
		                  				</#if>	
		                			</#foreach> 
		                		<#elseif !c2j.isComponent(field) >
		                  			<#foreach idfield in keyList.keyList().iterator()>          		 	
		              					<#assign columnName = dbdata.getColumnName(idfield)>
		                  				<#assign datacount = datacount + 1>    
		                    			<cell bkcolor="#f7f7f7" col="${datacount}" colid="${columnName.toUpperCase()}" color="#58595b" display="text"/>		       	  
		                 			</#foreach> 
		                		</#if>
		              		</#if>          
		            	</#foreach>
		  				<#list columnWOIdList as columnWOId>
		                	<#assign datacount = datacount + 1>  
		                	<#assign columnType = columnFieldTypeWithoutIdMap.get(columnWOId)>
		                  	<cell bkcolor="#f7f7f7" col="${datacount}" colid="${columnWOId.toUpperCase()}" color="#58595b" <#if "${columnType}" == "boolean">display="checkbox"<#else>display="text"</#if> <#if "${columnType}" == "timestamp" ||"${columnType}" == "time">mask="####-##-##&#32;##:##:##"</#if>/>		
					  	</#list>                           
					</body>
				</format>
			</contents>
		</Grid>
		<Div Height="25" Id="divPage" Left="136" TabOrder="2" Text="Div0" Top="256" Url="includes::LISTPAGE.xml" Width="560">
			<Contents></Contents>
		</Div>
		<Combo BindDataset="dsCboType" Border="Flat" CodeColumn="CODE" DataColumn="DATA" DisplayRowCnt="3" Font="돋움,10" Height="20" Id="cboType" InnerDataset="dsCboType" Left="12" OnChanged="cmbType_OnChanged" ResetIndex="FIRST" Style="stlCbo" TabOrder="2" Top="45" Width="240"></Combo>
		<#assign totcount = 0>
		<#foreach field in pojo.getAllPropertiesIterator()>
			<#assign totcount = totcount + 1>  
		</#foreach>

		<#if (totcount*23+22 > 264) >
      <Div Border="Flat" Height="264" scroll="true" Id="divManage" Left="10" Style="stlDivManage" TabOrder="4" Text="Div0" Top="296" UserData="RESIZE=TWH" Width="816">
    	<#else>
	  <Div Border="Flat" Height="${totcount*23+22}" Id="divManage" Left="10" Style="stlDivManage" TabOrder="4" Text="Div0" Top="296" UserData="RESIZE=TWH" Width="816">
		</#if>  		
			<Contents>				
				<#assign bindcount = 0>				
				<#foreach field in pojo.getAllPropertiesIterator()>
					<#if field.equals(pojo.identifierProperty)>
				    <#assign idFieldName = field.name>
					    <#if field.value.identifierGeneratorStrategy == "assigned">
							<#if !c2j.isComponent(field) >
								<#foreach idfield in keyList.keyList().iterator()>          		 	
					        		<#assign columnName = dbdata.getColumnName(idfield)>
					        		<Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="img${bindcount+1}" ImageID="blet_02" Left="10" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImgManage" TabOrder="${bindcount+1}" TabStop="FALSE" Text="${columnName.toUpperCase()}" Top="${(bindcount*23+11)?c}" Width="140"></Image>
					              	<Edit BindDataset="dsGrid${pojo.shortName}" Border="Flat" Column="${columnName.toUpperCase()}" Height="20" Id="${columnName.toUpperCase()}" Left="150" Style="stlEdt" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="460"></Edit>		 
					        		<#assign bindcount = bindcount + 1>    						 		
					        	</#foreach>
							<#else>
								<#foreach idfield in keyList.keyList().iterator()>
								<#assign columnName = dbdata.getColumnName(idfield)>
				              		<#if keyList.get(idfield) == "FK" >
					              		<#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
							                <#assign columnJavaType = foreignKeyList.get(fkfield)/>
							                	<Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="img${bindcount+1}" ImageID="blet_02" Left="10" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImgManage" TabOrder="${bindcount+1}" TabStop="FALSE" Text="${columnName.toUpperCase()}" Top="${(bindcount*23+11)?c}" Width="140"></Image>
							                	<Combo BindDataset="dsGrid${pojo.shortName}" Border="Flat" CodeColumn="${columnName.toUpperCase()}" Column="${columnName.toUpperCase()}" DataColumn="${columnName.toUpperCase()}" Height="20" Id="${columnName.toUpperCase()}" ImeMode="none" InnerDataset="dsGrid${columnJavaType}" Left="150" Style="stlCbo" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="126"></Combo>
							                <#assign bindcount = bindcount + 1>  	                 
					              		</#foreach>   
				              		<#else>
				              			<Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="img${bindcount+1}" ImageID="blet_02" Left="10" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImgManage" TabOrder="${bindcount+1}" TabStop="FALSE" Text="${columnName.toUpperCase()}" Top="${(bindcount*23+11)?c}" Width="140"></Image>   						
				                		<Edit BindDataset="dsGrid${pojo.shortName}" Border="Flat" Column="${columnName.toUpperCase()}" Height="20" Id="${columnName.toUpperCase()}" Left="150" Style="stlEdt" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="460"></Edit>
				                		<#assign bindcount = bindcount + 1>             		
				          			</#if>
								</#foreach>
							</#if>
					    </#if>
					<#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
					    <#foreach column in field.getColumnIterator()>
					    		<#if !column.nullable >	        
						        <Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="img${bindcount+1}" ImageID="blet_02" Left="10" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImgManage" TabOrder="${bindcount+1}" TabStop="FALSE" Text="${column.name}" Top="${(bindcount*23+11)?c}" Width="140"></Image>
						        <#else>
						        <Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="img${bindcount+1}" ImageID="blet_01" Left="10" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImgManage" TabOrder="${bindcount+1}" TabStop="FALSE" Text="${column.name}" Top="${(bindcount*23+11)?c}" Width="140"></Image>
						        </#if>
						        <#if field.value.typeName == "java.util.Date" || field.value.typeName == "date" || field.value.typeName == "java.sql.Date">	        
						        <Calendar BindDataset="dsGrid${pojo.shortName}" Border="Flat" Column="${column.name}" Height="20" Id="${column.name}" Left="150" MonthPickerFormat="yyyy-MM-dd" SaturdayTextColor="blue" Style="stlCal" SundayTextColor="red" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="126"></Calendar>
						        <#elseif field.value.typeName == "timestamp" || field.value.typeName == "time">
						        <MaskEdit BindDataset="dsGrid${pojo.shortName}" Border="Flat" Column="${column.name}" Height="20" Id="${column.name}" Left="150" LeftMargin="2" Style="stlEdt" Mask="####-##-##&#32;##:##:##" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Type="STRING" Width="140"></MaskEdit>
						        <#elseif field.value.typeName == "boolean" || field.value.typeName == "java.lang.Boolean">
						        <CheckBox BindDataset="dsGrid${pojo.shortName}" Column="${column.name}" Height="20" Id="${column.name}" Left="150" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="13"></CheckBox>        
						        <#else>
						        <Edit BindDataset="dsGrid${pojo.shortName}" Border="Flat" Column="${column.name}" Height="20" Id="${column.name}" Left="150" Style="stlEdt" TabOrder="${bindcount+20}" Top="${(bindcount*23+11)?c}" Width="460"></Edit>
						        </#if>
						        <#assign bindcount = bindcount + 1>	        
					    </#foreach>
					</#if>
				</#foreach>
			</Contents>
		</Div>
		<Button ButtonStyle="TRUE" Cursor="HAND" Height="19" Id="btnAddRow" ImageID="btn_add" Left="654" OnClick="btnAddRow_OnClick" Style="stlBtn" TabOrder="5" Text="&#32;추가" Top="30" Width="56"></Button>
		<Button ButtonStyle="TRUE" Cursor="HAND" Height="19" Id="btnDelRow" ImageID="btn_delete" Left="713" OnClick="btnDelRow_OnClick" Style="stlBtn" TabOrder="6" Text="&#32;삭제" Top="30" Width="56"></Button>
		<Button ButtonStyle="TRUE" Cursor="HAND" Height="19" Id="btnSaveAll" ImageID="btn_save" Left="772" OnClick="btnSaveAll_OnClick" Style="stlBtn" TabOrder="7" Text="&#32;저장" Top="30" Width="56"></Button>
		<Div Border="Flat" Height="54" Id="divSearch" Left="10" Style="stlDivSearch" TabOrder="8" Text="Div0" Top="69" UserData="RESIZE=TWH" Width="814">
			<Contents>
				<Static Border="Flat" Font="돋움,9,Bold" Height="48" Id="lblGrpSearch" Left="2" Style="stlLblGrp" TabOrder="1" Top="2" Width="808"></Static>
				<Image Align="Left" Font="돋움체,9,Bold" Height="20" Id="imgSearch4" ImageID="blet_search" Left="12" LeftMargin="20" OnClick="Div0_Image0_OnClick" Style="stlImg"  Text="검색조건" TabOrder="2" TabStop="FALSE" Top="14" Width="80"></Image>
				<Button ButtonStyle="TRUE" Cursor="HAND" Height="22" Id="btnSearch" ImageID="icon_search" Left="319" OnClick="btnSearch_Onclick" Style="stlBtn" TabOrder="3" Top="13" UserData="TYPE=S" Width="22"></Button>
				<Edit BindDataset="dsSearch" Border="Flat" Column="SEARCH_KEYWORD" Height="20" Id="edtSearchCd" ImeMode="keep,native" Left="215" Style="stlEdt" TabOrder="4" Top="14" Width="100"></Edit>
				<Combo BindDataset="dsSearch" Border="Flat" Column="SEARCH_CONDITION" Editable="TRUE" Height="20" Id="cboTemp" ImeMode="keep,native" INDEX="0" Left="94" ResetIndex="FIRST" Search="FILTERED" Style="stlCbo" TabOrder="5" Top="13" Width="120">
					<Contents>
						<Record code="All" Data="All"/>
						<#foreach field in pojo.getAllPropertiesIterator()>    
					        <#foreach column in field.getColumnIterator()>
					        	<#if !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
					            	<Record code="${column.name.toUpperCase()}" Data="${column.name.toUpperCase()}"/>
					            </#if>
					        </#foreach>    	    
				            <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>            		
				        		<#foreach idfield in keyList.keyList().iterator()>               	
				            		<#assign idColumnName = dbdata.getColumnName(idfield)> 		        			 	
				            		<Record code="${idColumnName.toUpperCase()}" Data="${idColumnName.toUpperCase()}"/> 				            
				         		</#foreach>     		
				        	</#if>	   		                  	
					    </#foreach>
					</Contents>
				</Combo>
			</Contents>
		</Div>
		<Image Align="Left" Height="16" Id="imgTitle" ImageID="b_title_icon1" Left="14" LeftMargin="20" Style="stlTitle" TabOrder="9" Top="24" Text="${pojo.shortName}MiP List" VAlign="Top" Width="626"></Image>
	</Form>
	<Script><![CDATA[#include "javascript::common.js";

var searchKeyword = "";	
	
function ${pojoNameLower}_grid_list_OnLoadCompleted(obj) {	
  
  divPage.nCurrentPage = 1;
  divSearch.cboTemp.index = 0;
  dsSearch.setConstColumn("pageIndex", 1);
	gfnService("getPagingList_C_C");
	
	<#foreach field in pojo.getAllPropertiesIterator()>
		<#if field.equals(pojo.identifierProperty)>
			<#if c2j.isComponent(field)>
				<#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
					<#assign columnJavaType = foreignKeyList.get(fkfield)/>
	gfnService("getPagingList${columnJavaType}_C_C");
					</#foreach>
			</#if>
		</#if>
	</#foreach>
	
	gfnForm_OnLoadCompleted(obj);
}

var sTemplateType = "1";

function cmbType_OnChanged(obj,strCode,strText,nOldIndex,nNewIndex)
{
	sTemplateType = strCode;
}

//페이징 처리용 script (◀, ▶, 번호 클릭)
function fnGetPagingList(obj) {	
	//검색 버튼 눌렀을 때의 검색 조건 유지, 없을 시 ""
	dsSearch.setColumn(0, "SEARCH_KEYWORD", searchKeyword);
	
	switch(sTemplateType) {
		case "1" :
			gfnService("getPagingList_C_C");
			break;
		case "2" :
			gfnService("getPagingList_C_U");
			break;
		case "3" :
			gfnService("getPagingList_U_U");
			break;
		default :
			gfnService("getPagingList_C_C");
	}
}

function btnSearch_Onclick(obj,nX,nY)
{
  searchKeyword = divSearch.edtSearchCd.text;
  
  divPage.nCurrentPage = 1;
	dsSearch.setConstColumn("pageIndex", 1);
  
	switch(sTemplateType) {
		case "1" :
			gfnService("getPagingList_C_C");
			break;
		case "2" :
			gfnService("getPagingList_C_U");
			break;
		case "3" :
			gfnService("getPagingList_U_U");
			break;
		default :
			gfnService("getPagingList_C_C");
	}
}

function btnAddRow_OnClick(obj,nX,nY)
{
	dsGrid${pojo.shortName}.AddRow();
}

function btnDelRow_OnClick(obj,nX,nY)
{
	gfnRemoveCheckedRows(dsGrid${pojo.shortName},"_chk");
}

function btnSaveAll_OnClick(obj,nX,nY)
{
	switch(sTemplateType) {
		case "1" :
			gfnService("saveAll_C_C");
			break;
		case "2" :
			gfnService("saveAll_C_U");
			break;
		case "3" :
			gfnService("saveAll_U_U");
			break;
		default :
			gfnService("saveAll_C_C");
	}
}

<#foreach field in pojo.getAllPropertiesIterator()>  												
	<#if field.equals(pojo.identifierProperty)>	
			function dsGrid${pojo.shortName}_OnRowPosChanged(obj,nOldRow,nRow)
			{
				if(dsGrid${pojo.shortName}.GetRowType(nRow) == "insert"){				
					<#foreach idfield in keyList.keyList().iterator()>          		 	
					<#assign columnName = dbdata.getColumnName(idfield)>
				    divManage.${columnName.toUpperCase()}.Enable = true;	
	    			</#foreach>
	    		}else {
	    			<#foreach idfield in keyList.keyList().iterator()>          		 	
					<#assign columnName = dbdata.getColumnName(idfield)>
				    divManage.${columnName.toUpperCase()}.Enable = false;	
	    			</#foreach>
	    		}
	    	}
	</#if>          
</#foreach>

function Grid0_OnHeadClick(obj,nCell,nX,nY,nPivotIndex)
{
	gfnGrid_OnHeadClick(obj,nCell,nX,nY,nPivotIndex);
}

// callback 메소드 정의
function fnCallback(strServiceId, strErrorCode, strErrorMsg) {
	if (strErrorCode == -1) {
		gfnMsg(strErrorMsg, "ERR");
		
	} else {
		if(indexOf(strServiceId, "saveAll") >= 0) {
			gfnMsg("MSG_SAVE_SUCCESS");
			btnSearch_Onclick();
		}else if(indexOf(strServiceId,"getPagingList") >= 0) {
			divPage.objListDataset = dsGrid${pojo.shortName};
			divPage.objPageDataset = dsSearch;
			divPage.fnMakePage();
		}
	}
}

]]></Script>
</Window>