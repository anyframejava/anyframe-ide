<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/top.jsp"%>
	</div>
    <hr />
  	<div id="container">
    	<div class="main_greeting">
        	<dl>
                <dt>Welcome to Anyframe 5.6.1-SNAPSHOT</dt>
                <dd>Congratulations! Anyframe application has been successfully installed. Anyframe is an open source project and application framework that provides basic architecture, common technical services, templates to help you develop web applications on the Java platform quickly and efficiently.</dd>
            </dl>
        </div>
        <hr />
        
        <div class="itemBox_1">
        	<table summary="This is a list of installed plugins">
            <caption>A list of sample application</caption>
            	<colgroup>
                	<col style="width:230px;" />
                    <col style="" />
                </colgroup>
            	<tr>
                	<th>Installed Examples</th>
                    <td>
                    	<ul>
                            <li><a href="<c:url value='/movieFinder.do'/>">Servlet 3.0 Annotation Example</a></li>
                            <li><a href="<c:url value='/fileUpload.do'/>">Servlet 3.0 File Upload Example</a></li>
                        </ul>
                        <ul>
                    </td>
                </tr>
            </table>
        </div>
        <hr />
    </div>
    <hr />
<%@ include file="/sample/common/bottom.jsp"%>