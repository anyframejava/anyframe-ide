﻿//XJS=comFrame.xjs
(function()
{
    return function(path)
    {
        var obj;
    
        // User Script
        this.registerScript(path, function() {
        /**
         * @class 프레임 내에 화면을 중앙정렬처리
         * @param obj:Form 메뉴아이디
         * @param divBase:Div Reload 여부
         * @return None
         */  
        this.gfn_frameOnSize = function(obj,divBase)
        {
        	var nWidth = parseInt(application.mainframe.width);
        	var WIDTH = application.gv_mainframe_width;

        	var nMargin = parseInt((nWidth - WIDTH)/2);
        	//trace(obj.getOwnerFrame().name +" > "+obj.vscrollbar.scrollbarsize);
        	
        	if(nMargin < 0) nMargin = 0;	
        	divBase.set_left(nMargin);
        	//스크롤이 있는 경우 스크롤사이즈만큼 right값이 
        	if(obj.vscrollbar.visible) divBase.set_right(nMargin - obj.vscrollbar.scrollbarsize);
        	else divBase.set_right(nMargin);
        		
        	divBase.resetScroll();
        }

        /**
         * @class 메뉴 아이디를 기준으로 신규 윈도우 화면을 생성하고 open 시킴
         * @param menuid: 메뉴아이디
         * @param bReload: Reload 여부
         * @param oArgs: 넘겨줄 Argument
         * @param bClose: 
         * @return None
         */  
        this.gfn_openMenu = function (sMenuId,oArgs)
        {
        	if (Eco.isEmpty(sMenuId)){
        		return false;
        	}

        	var nRow = -1;	
        	nRow = application.gds_menu.findRow("MENU_ID", sMenuId);	
        	if (nRow == -1){
        		this.alert("Menu가 존재하지 않습니다.");
        		return false;
        	}
        	
        	var sMenuNm = application.gds_menu.getColumn(nRow, "MENU_NM");
        	var sMenuUrl = application.gds_menu.getColumn(nRow, "MENU_URL");
        	if(Eco.isEmpty(sMenuUrl)) return false;
        		
        	var oWorkFrame = application.gv_workframe;
        // 	oWorkFrame.set_formurl("");
        // 	oWorkFrame.set_formurl("frame::workframe.xfdl");
        // 	oWorkFrame._menuid = sMenuId;
        // 	oWorkFrame._menunm = sMenuNm;
        // 	oWorkFrame._menuurl = sMenuUrl;
        	oWorkFrame.form.form_init(sMenuId, sMenuNm, sMenuUrl);
        	
        	if(system.navigatorname != "nexacro")
        	MyHistory.setLocationHash(sMenuId, sMenuUrl);
        	
        	return true;
        }

        /**
         * @class 프레임 화면을 변경한다.
         * @param sView (로그인:login, 메인:main, 업무:work)
         * @return None
         */  
        this.gfn_setVFrameChange = function(sView)
        {
        	var sSeparatesize = application.gv_vframeset.separatesize;
        	switch(sView){
        	    case "work": 
        			sSeparatesize = application.gv_topframe_height+","+application.gv_infoframe_height+",0,*,"+application.gv_bottomframe_height;
        			break;
        		case "home": 
        			sSeparatesize = application.gv_topframe_height+",0,*,0,"+application.gv_bottomframe_height;
        			break;
        		case "expand": 
        			sSeparatesize = "0,0,0,*,0";
        			break;
        		default :
        			break;
        	}

        	application.gv_vframeset.set_separatesize(sSeparatesize);	
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * @class menuid 를 기준으로 해당 메뉴의 컬럼값을 전달
         * @param menuid: 메뉴아이디
         * @param menuInfo: 메뉴컬럼
         * @return 메뉴컬럼값
         */  
        this.gfn_getMenuPath = function (upmenuid)
        {
         	var strMenuPathName = "";
        	var strUpMenuId = "";
        	var nRow = application.gds_menu.findRow("MENU_CD",upmenuid);
        	while(nRow != -1){
        		strMenuPathName = application.gds_menu.getColumn(nRow,"MENU_NM")+"."+strMenuPathName;
        		strUpMenuId = application.gds_menu.getColumn(nRow,"UP_MENU_CD");
        		nRow = application.gds_menu.findRow("MENU_CD",strUpMenuId);
        	}
        	
        // 	nRow = application.gds_comCode.findRowExpr("GROUPID.toString() == '0019' && GRUP_CD1 == '"+strUpMenuId+"'");
        // 	strMenuPathName = application.gds_comCode.getColumn(nRow,"CODENAME")+"."+strMenuPathName;
        	
        	return strMenuPathName;
        }

        /**
         * @class 해당화면을 닫는다.
         * @param sWinKey : 화면 key값
         * @return boolean
         */  
        this.gfn_close = function(sWinKey)
        {
        	var oChildFrame = application.gv_workFrame.frames[sWinKey];
        	
        	if(Eco.isEmpty(this.opener))
        	{
        		if(Eco.isEmpty(oChildFrame)) return true;
        		
        		try{			
        			if(oChildFrame.form.div_work.fn_close() == false)
        			{
        				if(application.confirm("변경된 내역이 존재합니다. 종료하시겠습니까?"))
        				{
        					oChildFrame.form.close();
        				}
        				else
        				{
        					return false;
        				}
        			}
        			else
        			{
        				oChildFrame.form.close();
        			}
        		}
        		catch(e)
        		{
        			oChildFrame.form.close();
        		}
        		//trace(application.gv_workFrame.frames.length);
        		if(application.gv_workFrame.frames.length == 0)
        		{
        			this.gfn_setHFrameChange("main");
        			this.gfn_setVFrameChange("main");
        		}		
        	}
        	else
        	{
        		if(this.div_work.fn_close() == false)
        		{
        			if(application.confirm("변경된 내역이 존재합니다. 종료하시겠습니까?"))
        			{
        				this.close();
        			}
        			else
        			{
        				return false;
        			}
        		}
        		else
        		{
        			this.close();
        		}
        	}
        	
        	return true;
        }
        /**
         * @class 전체화면 닫기
         * @param 
         * @return None
         */ 
        this.gfn_closeAll = function()
        {
        	var iFramesCnt = application.gv_workFrame.frames.length;
        	for (var i=iFramesCnt-1; i>=0; i--)
        	{
        		try{			
        			if(application.gv_workFrame.frames[i].form.div_work.fn_close() == false)
        			{
        				if(application.confirm("변경된 내역이 존재합니다. 종료하시겠습니까?"))
        				{
        					application.gv_workFrame.frames[i].form.close();
        				}
        				else
        				{
        					return false;
        				}
        			}
        			else
        			{
        				application.gv_workFrame.frames[i].form.close();
        			}
        		}
        		catch(e)
        		{
        			application.gv_workFrame.frames[i].form.close();
        		}
        	}
        	return true;
        }
        });


    
        this.loadIncludeScript(path);
        
        obj = null;
    };
}
)();
