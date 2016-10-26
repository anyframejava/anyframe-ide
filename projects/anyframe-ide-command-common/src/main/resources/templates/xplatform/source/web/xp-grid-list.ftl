<#assign pojoNameLower = pojo.shortName.substring(0,1).toLowerCase()+pojo.shortName.substring(1)> 		
<#assign columnFieldMap = dbdata.getColumnFieldMap(pojo) />
<#assign columnList = columnFieldMap.keyList() />
<#assign fieldList = columnFieldMap.valueList() />
<#assign columnFieldWithoutIdMap = dbdata.getColumnFieldWithoutIdMap(pojo) />
<#assign columnWOIdList = columnFieldWithoutIdMap.keyList() />
<#assign columnFieldTypeWithoutIdMap = dbdata.getColumnDataTypeWithoutIdMap(pojo) />
<#assign keyList = dbdata.getKeyList(pojo)/>
<#assign foreignKeyList = dbdata.getForeignKeyList(pojo)/>
﻿<?xml version="1.0" encoding="utf-8"?>
<FDL version="1.4">
  <TypeDefinition url="..\default_typedef.xml"/>
  <Form id="${pojoNameLower}_grid_list" classname="SAMPLE002" inheritanceid="" position="absolute 0 0 765 540" titletext="GRDFRM" onload="SAMPLE_onload" onbeforeclose="SAMPLE002_onbeforeclose" style="background:#f2f2efff;">
	<Layouts>
	    <Layout>
	      <Div id="Div00" taborder="8" text="Div00" position="absolute 0 0 765 40">
	      	<Layouts>
		        <Layout>
		          <Static id="stTitle" text="${pojo.shortName}XP List" position="absolute 18 10 219 32" style="border:0px solid #b1b5b9b3 ; "/>
		          <Button id="btAddCategory" taborder="4" text="추가" onclick="btAdd_onclick" position="absolute 542 7 615 32"/>
		          <Button id="btDeleteCategory" taborder="5" text="삭제" onclick="btDelete_onclick" position="absolute 617 7 690 32"/>
		          <Button id="btSaveAll" taborder="6" text="저장" onclick="btSaveAll_onclick" position="absolute 692 7 765 32"/>
		        </Layout>
		    </Layouts>
	      </Div>
	      <Static id="Static00" position="absolute 2 10 16 32" style="background:URL('image::bullet_WF_Lev1.png') left middle; " text=""/>
	      <Combo id="cmbType" taborder="9" text="공통 Controller + 공통 Service" innerdataset="dsCmbType" codecolumn="CODE" datacolumn="DATA" position="absolute 0 36 250 58" index="0" value="0" onitemchanged="cmbType_onitemchanged"/>
	      <Div id="divSearch" taborder="3" text="search" position="absolute 0 67 765 107" style="background:#fff0f5ff;border:1 solid #906d3bff ;">
	      	<Layouts>
		        <Layout>
		          <Static id="stSearchCondition" text="검색조건" position="absolute 2 9 57 31"/>
		          <Combo id="cmbSearchCondition" taborder="4" innerdataset="@dsSearchComboBox" codecolumn="CODE" datacolumn="DATA" position="absolute 59 9 167 31" index="0" value="0" text="All"/>
		          <Edit id="edtSearchKeyword" taborder="5" position="absolute 169 9 328 31" onkeydown="divSearch_edtSearchKeyword_onkeydown"/>
		          <Button id="btDoSearch" taborder="6" position="absolute 330 9 351 31" onclick="divSearch_btDoSearch_onclick" style="background:URL('image::btn_TP_SearchDrop_N.png') left middle;cursor:hand;"/>
		        </Layout>
		    </Layouts>
	      </Div>
	      <Div id="divGrd" taborder="4" text="divGrid" position="absolute 0 137 765 339">
	      	<Layouts>
		        <Layout>
		          <Grid id="Grid0" taborder="0" binddataset="dsGrid${pojo.shortName}" useinputpanel="false" selecttype="multirow" nodatatext="조회된 데이터가 없습니다." position="absolute 0 0 765 202" formats="&lt;Formats&gt;&#10;  &lt;Format id=&quot;default&quot;&gt;&#10;    &lt;Columns&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;      &lt;Col size=&quot;80&quot;/&gt;&#10;    &lt;/Columns&gt;&#10;    &lt;Rows&gt;&#10;      &lt;Row band=&quot;head&quot; size=&quot;24&quot;/&gt;&#10;      &lt;Row band=&quot;body&quot; size=&quot;24&quot;/&gt;&#10;    &lt;/Rows&gt;&#10;    &lt;Band id=&quot;head&quot;&gt;&#10;      &lt;Cell col=&quot;0&quot; disptype=&quot;normal&quot; text=&quot;TITLE&quot;/&gt;&#10;      &lt;Cell col=&quot;1&quot; disptype=&quot;normal&quot; text=&quot;CONTENTS&quot;/&gt;&#10;      &lt;Cell col=&quot;2&quot; disptype=&quot;normal&quot; text=&quot;REG_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;3&quot; disptype=&quot;normal&quot; text=&quot;REG_DATE&quot;/&gt;&#10;      &lt;Cell col=&quot;4&quot; disptype=&quot;normal&quot; text=&quot;POST_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;5&quot; disptype=&quot;normal&quot; text=&quot;COMMUNITY_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;6&quot; disptype=&quot;normal&quot; text=&quot;pageCount&quot;/&gt;&#10;      &lt;Cell col=&quot;7&quot; disptype=&quot;normal&quot; text=&quot;pageIndex&quot;/&gt;&#10;      &lt;Cell col=&quot;8&quot; disptype=&quot;normal&quot; text=&quot;pageSize&quot;/&gt;&#10;      &lt;Cell col=&quot;9&quot; disptype=&quot;normal&quot; text=&quot;totalCount&quot;/&gt;&#10;    &lt;/Band&gt;&#10;    &lt;Band id=&quot;body&quot;&gt;&#10;      &lt;Cell col=&quot;0&quot; disptype=&quot;normal&quot; text=&quot;bind:TITLE&quot;/&gt;&#10;      &lt;Cell col=&quot;1&quot; disptype=&quot;normal&quot; text=&quot;bind:CONTENTS&quot;/&gt;&#10;      &lt;Cell col=&quot;2&quot; disptype=&quot;normal&quot; text=&quot;bind:REG_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;3&quot; disptype=&quot;normal&quot; text=&quot;bind:REG_DATE&quot;/&gt;&#10;      &lt;Cell col=&quot;4&quot; disptype=&quot;normal&quot; text=&quot;bind:POST_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;5&quot; disptype=&quot;normal&quot; text=&quot;bind:COMMUNITY_ID&quot;/&gt;&#10;      &lt;Cell col=&quot;6&quot; disptype=&quot;normal&quot; text=&quot;bind:pageCount&quot;/&gt;&#10;      &lt;Cell col=&quot;7&quot; disptype=&quot;normal&quot; text=&quot;bind:pageIndex&quot;/&gt;&#10;      &lt;Cell col=&quot;8&quot; disptype=&quot;normal&quot; text=&quot;bind:pageSize&quot;/&gt;&#10;      &lt;Cell col=&quot;9&quot; disptype=&quot;normal&quot; text=&quot;bind:totalCount&quot;/&gt;&#10;    &lt;/Band&gt;&#10;  &lt;/Format&gt;&#10;&lt;/Formats&gt;&#10;" autofittype="col" cellsizingtype="col" onrbuttonup="divGrd_grdBoard_onrbuttonup" onheadclick="divGrd_grdBoard_onheadclick">
		            <Formats>
		              <Format id="default">
		                <Columns>
		                  <#foreach field in pojo.getAllPropertiesIterator()>  												
		      				<#if field.equals(pojo.identifierProperty)>
		              		  	<#foreach idfield in keyList.keyList().iterator()>          		 	              			
		                  			<Column size="85"/>                                         
		                		</#foreach>
			            	</#if>          
				           </#foreach>
				           <#list columnWOIdList as columnWOId>
		             		    <Column size="85"/>           					
				  		   </#list>  	
		                </Columns>
		                <Rows>
		                  <Row size="24" band="head"/>
		                  <Row size="24"/>
		                </Rows>
		                <Band id="head">
			                <#assign headercount = -1>
							<#foreach field in pojo.getAllPropertiesIterator()>  												
				            	<#if field.equals(pojo.identifierProperty)>
					            	<#if c2j.isComponent(field) >
			              		  		<#foreach idfield in keyList.keyList().iterator()>          		 	
					              			<#assign columnName = dbdata.getColumnName(idfield)>
					              			<#if keyList.get(idfield) == "FK" >
					                  			<#assign headercount = headercount + 1> 
					                  			<cell col="${headercount}" text="${columnName.toUpperCase()}"/>                
					                        <#else>
					                        	<#assign headercount = headercount + 1> 
					                            <cell col="${headercount}" text="${columnName.toUpperCase()}"/>                
					                  		</#if>	
			                			</#foreach>
					                <#elseif !c2j.isComponent(field) >
					                	<#foreach idfield in keyList.keyList().iterator()>          		 	
					              			<#assign columnName = dbdata.getColumnName(idfield)> 
					                    	<#assign headercount = headercount + 1>
					                      	<cell col="${headercount}" text="${columnName.toUpperCase()}"/> 
					                    </#foreach>               		                  
					            	</#if>
				            	</#if>          
				            </#foreach>
				            <#list columnWOIdList as columnWOId>
		              			<#assign headercount = headercount + 1>
		              			<cell col="${headercount}" text="${columnWOId.toUpperCase()}"/>
					  		</#list> 
		                </Band>
		                <Band id="body">
			                <#assign headercount = -1>
							<#foreach field in pojo.getAllPropertiesIterator()>  												
				            	<#if field.equals(pojo.identifierProperty)>
					            	<#if c2j.isComponent(field) >
			              		  		<#foreach idfield in keyList.keyList().iterator()>          		 	
					              			<#assign columnName = dbdata.getColumnName(idfield)>
					              			<#if keyList.get(idfield) == "FK" >
					                  			<#assign headercount = headercount + 1> 
					                  			<cell col="${headercount}" text="bind:${columnName.toUpperCase()}"/>                
					                        <#else>
					                        	<#assign headercount = headercount + 1> 
					                            <cell col="${headercount}" text="bind:${columnName.toUpperCase()}"/>                
					                  		</#if>	
			                			</#foreach>
					                <#elseif !c2j.isComponent(field) >
					                	<#foreach idfield in keyList.keyList().iterator()>          		 	
					              			<#assign columnName = dbdata.getColumnName(idfield)> 
					                    	<#assign headercount = headercount + 1>
					                      	<cell col="${headercount}" text="bind:${columnName.toUpperCase()}"/> 
					                    </#foreach>               		                  
					            	</#if>
				            	</#if>          
				            </#foreach>
				            <#list columnWOIdList as columnWOId>
		              			<#assign headercount = headercount + 1>
		              			<cell col="${headercount}" text="bind:${columnWOId.toUpperCase()}"/>
					  		</#list> 
		                </Band>
		              </Format>
		            </Formats>
		          </Grid>
		        </Layout>
		    </Layouts> 
	      </Div>
	      <#assign totcount = 0>
		  <#foreach field in pojo.getAllPropertiesIterator()>
			<#assign totcount = totcount + 1>  
		  </#foreach>
		  <#assign divbottom = 379 + totcount * 26 + 1>
	      <Div id="divBoardForm" taborder="6" text="divBoardForm" position="absolute 0 379 765 ${divbottom}">
	      	<Layouts>
		        <Layout>
		          <#assign bindcount = 0>				
					<#foreach field in pojo.getAllPropertiesIterator()>
						<#if field.equals(pojo.identifierProperty)>
					    <#assign idFieldName = field.name>
						    <#if field.value.identifierGeneratorStrategy == "assigned">
								<#if !c2j.isComponent(field) >
									<#foreach idfield in keyList.keyList().iterator()>
									    <#assign top = bindcount*26 + 1>
									    <#assign bottom = top + 26>
						        		<#assign columnName = dbdata.getColumnName(idfield)>
						        		<Static id="st${columnName}${bindcount}" text="${columnName.toUpperCase()}" position="absolute 0 ${top} 85 ${bottom}" style="background:#edeee6ff;border:1 solid #c6c6c5ff ;align:center middle;"/>
						        		<Static id="Static${bindcount}" position="absolute 84 ${top} 765 ${bottom}" style="background:#ffffffff; border:1 solid #c6c6c5ff ; align:center middle; " text=""/>
						        		<Edit id="${columnName.toUpperCase()}" taborder="6" enable="false" position="absolute 86 ${top} 334 ${bottom}"/>
						        		<#assign bindcount = bindcount + 1>
						        	</#foreach>
						        <#else>
						        	<#foreach idfield in keyList.keyList().iterator()>
									<#assign columnName = dbdata.getColumnName(idfield)>
										<#if keyList.get(idfield) == "FK" >
						              		<#foreach fkfield in foreignKeyList.keyList().iterator()>
						              			<#assign top = bindcount*26 + 1>
									    		<#assign bottom = top + 26>          		 	
								                <#assign columnJavaType = foreignKeyList.get(fkfield)/>
								                <Static id="st${columnName}" text="${columnName.toUpperCase()}" position="absolute 0 ${top} 85 ${bottom}" style="background:#edeee6ff;border:1 solid #c6c6c5ff ;align:center middle;"/>
						        				<Static id="Static${bindcount}" position="absolute 84 ${top} 765 ${bottom}" style="background:#ffffffff; border:1 solid #c6c6c5ff ; align:center middle; " text=""/>
						        				<Combo id="${columnName.toUpperCase()}" taborder="11" innerdataset="@dsGrid${pojo.shortName}" codecolumn="${columnName.toUpperCase()}" datacolumn="${columnName.toUpperCase()}" position="absolute 86 ${top} 334 ${bottom}"/>
						        				<#assign bindcount = bindcount + 1>
						              		</#foreach>   
					              		<#else>
									    	<#assign top = bindcount*26 + 1>
									    	<#assign bottom = top + 26>     
					                		<Static id="st${columnName}" text="${columnName.toUpperCase()}" position="absolute 0 ${top} 85 ${bottom}" style="background:#edeee6ff;border:1 solid #c6c6c5ff ;align:center middle;"/>
						        			<Static id="Static${bindcount}" position="absolute 84 ${top} 765 ${bottom}" style="background:#ffffffff; border:1 solid #c6c6c5ff ; align:center middle; " text=""/>
						        			<Edit id="${columnName.toUpperCase()}" taborder="6" enable="true" position="absolute 86 ${top} 334 ${bottom}"/>
						        			<#assign bindcount = bindcount + 1>             		
					          			</#if>
									</#foreach>
								</#if>
						    </#if>
						  <#elseif !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
							    <#foreach column in field.getColumnIterator()>
							    		
									    <#assign top = bindcount*26 + 1>
									    <#assign bottom = top + 26>
									    <Static id="st${column.name}" text="${column.name}" position="absolute 0 ${top} 85 ${bottom}" style="background:#edeee6ff;border:1 solid #c6c6c5ff ;align:center middle;"/>
						        		<Static id="Static${bindcount}" position="absolute 84 ${top} 765 ${bottom}" style="background:#ffffffff; border:1 solid #c6c6c5ff ; align:center middle; " text=""/>
						        		<Edit id="${column.name}" taborder="6" enable="true" position="absolute 86 ${top} 334 ${bottom}"/>
						        		<#assign bindcount = bindcount + 1>
							    </#foreach>  
						  </#if>
					</#foreach>
		        </Layout>
		    </Layouts>
	      </Div>
	      <Div id="divPage" taborder="5" url="includes::LISTPAGE.xfdl" text="divPage" position="absolute 0 339 765 375"/>
	      <Div id="divGrdStatus" taborder="7" url="includes::GRDSTATUS.xfdl" text="divPage" position="absolute 0 107 765 137"/>
	      <PopupMenu id="popupMenuGrd" position="absolute 28 211 176 268"/>
	    </Layout>
    </Layouts>
    <Objects>
      <Dataset id="dsService" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <Column id="SVC_ID" type="STRING" size="100"/>
          <Column id="QUERY_LIST" type="STRING" size="256"/>
          <Column id="SERVICE" type="STRING" size="100"/>
          <Column id="IN_DATASET_LIST" type="STRING" size="256"/>
          <Column id="OUT_DATASET_LIST" type="STRING" size="256"/>
          <Column id="CONTROLLER" type="STRING" size="512"/>
          <Column id="CALLBACK" type="STRING" size="100"/>
          <Column id="SYNC_YN" type="STRING" size="1"/>
        </ColumnInfo>
        <Rows>
          <Row>
            <Col id="SVC_ID">getPagingList_C_U</Col>
            <Col id="SERVICE">${pojoNameLower}XPService.getPagingList</Col>
            <Col id="IN_DATASET_LIST">dsSearch=dsSearch</Col>
            <Col id="OUT_DATASET_LIST">dsGrid${pojo.shortName}=dsResult</Col>
          </Row>
          <Row>
            <Col id="SVC_ID">getPagingList_U_U</Col>
            <Col id="CONTROLLER">${pojoNameLower}XP.do</Col>
            <Col id="SERVICE">${pojoNameLower}XPService.getPagingList</Col>
            <Col id="IN_DATASET_LIST">dsSearch=dsSearch</Col>
            <Col id="OUT_DATASET_LIST">dsGrid${pojo.shortName}=dsResult</Col>
          </Row>
          <Row>
            <Col id="SVC_ID">getPagingList_C_C</Col>
            <Col id="QUERY_LIST">querySet1=findXP${pojo.shortName}List</Col>
            <Col id="IN_DATASET_LIST">querySet1=dsSearch</Col>
            <Col id="OUT_DATASET_LIST">dsGrid${pojo.shortName}=querySet1</Col>
          </Row>
          <Row>
            <Col id="SVC_ID">saveAll_C_C</Col>
            <Col id="QUERY_LIST">querySet1=createXP${pojo.shortName},updateXP${pojo.shortName},removeXP${pojo.shortName}</Col>
            <Col id="IN_DATASET_LIST">querySet1=dsGrid${pojo.shortName}:U</Col>
          </Row>
          <Row>
            <Col id="SVC_ID">saveAll_C_U</Col>
            <Col id="SERVICE">${pojoNameLower}XPService.saveAll</Col>
            <Col id="IN_DATASET_LIST">dsSave=dsGrid${pojo.shortName}:U</Col>
          </Row>
          <Row>
            <Col id="SVC_ID">saveAll_U_U</Col>
            <Col id="CONTROLLER">${pojoNameLower}XP.do</Col>
            <Col id="SERVICE">${pojoNameLower}MiPService.saveAll</Col>
            <Col id="IN_DATASET_LIST">dsSave=dsGrid${pojo.shortName}:U</Col>
          </Row>
          <#foreach fkfield in foreignKeyList.keyList().iterator()>          		 	
          <#assign columnJavaType = foreignKeyList.get(fkfield)/>
          	<Row>
	            <Col id="SVC_ID">getPagingList${columnJavaType}_C_C</Col>
	            <Col id="IN_DATASET_LIST">querySet1=dsSearch</Col>
	            <Col id="OUT_DATASET_LIST">dsGrid${columnJavaType}=querySet1</Col>
	            <Col id="QUERY_LIST">querySet1=find${columnJavaType}List</Col>
	        </Row>
          </#foreach>       
        </Rows>
      </Dataset>
      <Dataset id="dsSearch" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <ConstColumn id="pageIndex" type="INT" size="30" value="1"/>
          <ConstColumn id="pageSize" type="INT" size="30" value="5"/>
          <ConstColumn id="pageUnit" type="INT" size="30" value="5"/>
          <Column id="SEARCH_CONDITION" type="STRING" size="100"/>
          <Column id="SEARCH_KEYWORD" type="STRING" size="100"/>
        </ColumnInfo>
        <Rows>
          <Row>
            <Col id="SEARCH_CONDITION"/>
            <Col id="SEARCH_KEYWORD"/>
          </Row>
        </Rows>
      </Dataset>
      <Dataset id="dsGrid${pojo.shortName}" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <ConstColumn id="pageCount" type="INT" size="30" value="0"/>
          <ConstColumn id="pageIndex" type="INT" size="30" value="0"/>
          <ConstColumn id="pageSize" type="INT" size="30" value="0"/>
          <ConstColumn id="totalCount" type="INT" size="30" value="0"/>
          <#list columnList as columnName>
            <Column id="${columnName.toUpperCase()}" type="STRING" size="256"/>
		  </#list>
        </ColumnInfo>
      </Dataset>
      <Dataset id="dsCboCommunity" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <Column id="COMMUNITY_ID" type="STRING" size="256"/>
          <Column id="COMMUNITY_NAME" type="STRING" size="256"/>
          <Column id="COMMUNITY_DESC" type="STRING" size="256"/>
          <Column id="CATEGORY_ID" type="STRING" size="256"/>
          <Column id="REG_ID" type="STRING" size="256"/>
          <Column id="RED_DATE" type="STRING" size="256"/>
        </ColumnInfo>
      </Dataset>
      <Dataset id="dsSearchComboBox" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <Column id="CODE" type="STRING" size="256"/>
          <Column id="DATA" type="STRING" size="256"/>
        </ColumnInfo>
        <Rows>
          <Row>
            <Col id="CODE">0</Col>
            <Col id="DATA"/>
          </Row>
          <#assign searchCombocount = 0>
          
          <#foreach field in pojo.getAllPropertiesIterator()>    
          	<#foreach column in field.getColumnIterator()>
        		<#if !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
        	    	<#assign searchCombocount = searchCombocount + 1> 
    		<Row>
	            <Col id="CODE">${searchCombocount}</Col>
	            <Col id="DATA"/>
	         </Row>
            	</#if>
        	</#foreach>    	    
        	<#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>            		
    		<#foreach idfield in keyList.keyList().iterator()>
    		    <#assign searchCombocount = searchCombocount + 1>
        	<Row>
	            <Col id="CODE">${searchCombocount}</Col>
	        	<Col id="DATA"/>
	         </Row> 		        			 	
      				            
     		</#foreach>     		
    	</#if>	   		                  	
    </#foreach>
        </Rows>
      </Dataset>
      <Dataset id="dsCmbType" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <Column id="CODE" type="STRING" size="256"/>
          <Column id="DATA" type="STRING" size="256"/>
        </ColumnInfo>
        <Rows>
          <Row>
            <Col id="CODE">0</Col>
            <Col id="DATA"/>
          </Row>
          <Row>
            <Col id="CODE">1</Col>
            <Col id="DATA"/>
          </Row>
          <Row>
            <Col id="CODE">2</Col>
            <Col id="DATA"/>
          </Row>
        </Rows>
      </Dataset>
      <Dataset id="dsGrdBoardValid" firefirstcount="0" firenextcount="0" useclientlayout="false" updatecontrol="true" enableevent="true" loadkeymode="keep" loadfiltermode="keep" reversesubsum="false">
        <ColumnInfo>
          <Column id="TITLE" type="STRING" size="256"/>
          <Column id="CONTENTS" type="STRING" size="256"/>
          <Column id="REG_ID" type="STRING" size="256"/>
          <Column id="REG_DATE" type="DATE" size="8"/>
          <Column id="POST_ID" type="STRING" size="256"/>
          <Column id="COMMUNITY_ID" type="STRING" size="256"/>
        </ColumnInfo>
        <Rows>
          <Row>
            <Col id="TITLE">title:title,required:true,maxlength:150</Col>
            <Col id="CONTENTS">title:contents,maxlength:360</Col>
          </Row>
        </Rows>
      </Dataset>
    </Objects>
    <Bind>
      <BindItem id="item100" compid="divSearch.edtSearchKeyword" propid="value" datasetid="dsSearch" columnid="SEARCH_KEYWORD"/>
      <BindItem id="item101" compid="divSearch.cmbSearchCondition" propid="value" datasetid="dsSearch" columnid="SEARCH_CONDITION"/>
      
      <#assign itemcount = 0>
      <#list columnList as columnName>
            <BindItem id="item${itemcount}" compid="divBoardForm.${columnName.toUpperCase()}" propid="value" datasetid="dsGrid${pojo.shortName}" columnid="${columnName.toUpperCase()}"/>
            <#assign itemcount = itemcount + 1>
	  </#list>
    </Bind>
    <Script type="xscript4.0"><![CDATA[include "lib::commonScript.xjs";

var searchKeyword = "";
var searchCondition = 0;
var nCurrentPage = 1;
var objFocusedGrd;
var sTemplateType = "0";

// Form onload 이벤트 처리
function SAMPLE_onload(obj:Form, e:LoadEventInfo)
{
	divGrdStatus.fnSetUserData("GRD_STATUS=Grid0");
	
	// 조회 방식 조건 처리
	dsCmbType.setColumn(0, "DATA", "공통 Controller + 공통 Service");
	dsCmbType.setColumn(1, "DATA", "공통 Controller + 사용자 Service");
	dsCmbType.setColumn(2, "DATA", "사용자 Controller + 사용자 Service");
	
	cmbType.index = 0;
	
	// 검색 조건 Combo Box Message 처리
	dsSearchComboBox.setColumn(0, "DATA", "All");
	<#assign count = 0>
	<#foreach field in pojo.getAllPropertiesIterator()>    
        <#foreach column in field.getColumnIterator()>
        	<#if !column.nullable && !c2h.isCollection(field) && !c2h.isManyToOne(field) && !c2j.isComponent(field)>
        	    <#assign count = count + 1> 
    dsSearchComboBox.setColumn(${count}, "DATA", "${column.name.toUpperCase()}");
            </#if>
        </#foreach>    	    
        <#if field.equals(pojo.identifierProperty) && !c2h.isCollection(field) && !c2h.isManyToOne(field) && c2j.isComponent(field)>            		
    		<#foreach idfield in keyList.keyList().iterator()>
    		    <#assign count = count + 1>
        		<#assign idColumnName = dbdata.getColumnName(idfield)> 		        			 	
     dsSearchComboBox.setColumn(${count}, "DATA", "${column.name.toUpperCase()}"); 				            
     		</#foreach>     		
    	</#if>	   		                  	
    </#foreach>
    
	divSearch.cmbSearchCondition.index = 0;
	dsSearch.setConstColumn("pageIndex", nCurrentPage);
	dsSearch.setColumn(0, "SEARCH_KEYWORD", "");
	
	// Grid, Combo Box Data 수신
	gfnService("getPagingList_C_C");
	
	// pk 조건 disable 처리
	<#foreach field in pojo.getAllPropertiesIterator()>
		<#if field.equals(pojo.identifierProperty)>
		    <#if field.value.identifierGeneratorStrategy == "assigned">
				<#if !c2j.isComponent(field) >
					<#foreach idfield in keyList.keyList().iterator()>
					    <#assign columnName = dbdata.getColumnName(idfield)>
	divBoardForm.${columnName.toUpperCase()}.enable="false";
		        	</#foreach>
		        </#if>
		    </#if>
		</#if>
	</#foreach>
}

// 삭제 버튼 클릭 이벤트 처리
function btDelete_onclick(obj:Button,  e:ClickEventInfo)
{
	var nRow = dsGrid${pojo.shortName}.rowposition;
	
	if(!dsGrid${pojo.shortName}.getSelect(nRow)){
		gfnAlertMsg("err.grid.noselect");
	} else{
		var rtVal = gfnConfirm("msg.before.delete", "YN");

		if(rtVal == "Y"){
			dsGrid${pojo.shortName}.deleteSelectedRows();
		} else {
			return;
		}
	}
}

// 저장 버튼 클릭 이벤트 처리
function btSaveAll_onclick(obj:Button,  e:ClickEventInfo)
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

// 추가 버튼 클릭 이벤트 처리
function btAdd_onclick(obj:Button,  e:ClickEventInfo)
{
	dsGrid${pojo.shortName}.addRow();
	
	// pk 조건 disable 처리
	<#foreach field in pojo.getAllPropertiesIterator()>
		<#if field.equals(pojo.identifierProperty)>
		    <#if field.value.identifierGeneratorStrategy == "assigned">
				<#if !c2j.isComponent(field) >
					<#foreach idfield in keyList.keyList().iterator()>
						<#assign columnName = dbdata.getColumnName(idfield)>
	divBoardForm.${columnName.toUpperCase()}.enable="true";
		        	</#foreach>
		        </#if>
		    </#if>
		</#if>
	</#foreach>
}

// 페이징 처리
function fnGetPagingList(obj) {
	dsSearch.setColumn(0, "SEARCH_KEYWORD", searchKeyword);
	dsSearch.setColumn(0, "SEARCH_CONDITION", searchCondition);
	nCurrentPage = dsSearch.getConstColumn("pageIndex");
	
	switch(sTemplateType) {
		case "0" :
			gfnService("getPagingList_C_C");
			break;
		case "1" :
			gfnService("getPagingList_C_U");
			break;
		case "2" :
			gfnService("getPagingList_U_U");
			break;
		default :
			gfnService("getPagingList_C_C");
	}
}

// 검색 버튼 클릭 이벤트 처리
function divSearch_btDoSearch_onclick(obj:Button,  e:ClickEventInfo)
{
	searchKeyword = divSearch.edtSearchKeyword.text;
	searchCondition = divSearch.cmbSearchCondition.index;
	dsSearch.setColumn(0, "SEARCH_KEYWORD", searchKeyword);
	dsSearch.setColumn(0, "SEARCH_CONDITION", divSearch.cmbSearchCondition.text);
	divPage.nCurrentPage = 1;
	nCurrentPage = 1;
	dsSearch.setConstColumn("pageIndex", 1);
			
	switch(sTemplateType) {
		case "0" :
			gfnService("getPagingList_C_C");
			break;
		case "1" :
			gfnService("getPagingList_C_U");
			break;
		case "2" :
			gfnService("getPagingList_U_U");
			break;
		default :
			gfnService("getPagingList_C_C");
	}
	divSearch.cmbSearchCondition.index = searchCondition;	
}

// 검색어 입력 후 엔터키를 입력한 경우
function divSearch_edtSearchKeyword_onkeydown(obj:Edit, e:KeyEventInfo)
{
	if(e.keycode == 13){
		dsSearch.setColumn(0, "SEARCH_KEYWORD", divSearch.edtSearchKeyword.text);
		divSearch_btDoSearch_onclick();
	}
}

// Grid Head 클릭시 정렬
function divGrd_grdBoard_onheadclick(obj:Grid, e:GridClickEventInfo)
{
	gfnGridSort(obj, e);
}

// gfnService 처리 후 callback 처리
function fnCallback(strServiceId, strErrorCode, strErrorMsg){
	if(strErrorCode == -1){
		gfnAlertMsg(strErrorMsg, "ERR");
	} else {
		if(gfnIndexOf(strServiceId, "saveAll") >= 0) {
			gfnAlertMsg("msg.save.success");
			SAMPLE_onload();
		}else if(gfnIndexOf(strServiceId,"getPagingList") >= 0) {
			divPage.objListDataset = dsGrid${pojo.shortName};
			divPage.objPageDataset = dsSearch;
			divPage.fnMakePage();
		}
	}
}

// combobox event 처리
function cmbType_onitemchanged(obj:Combo, e:ItemChangeEventInfo)
{
	sTemplateType = e.postvalue;
}

]]></Script>
  </Form>
</FDL>
