<%@ page language="java" errorPage="/sample/common/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ include file="/sample/common/taglibs.jsp"%>
<div id="Topapplication" style="margin:0">
<table width="100%" height="79" border="0" cellpadding="0" cellspacing="0">
  <tr>  
    <td width="450" height="48" align="left" valign="middle" class="toptitle">
    	<a href="${ctx}" title="Go Home">Plugin Samples</a></td>
    <td align="right" valign="top">
    	<img src="<c:url value='/sample/images/top_bg_movie.jpg'/>" width="350" height="48" border="0"/></td>
  </tr>
  <tr>
    <td height="31" align="right" valign="middle" style="padding-right:20px"></td>
    <td align="right" valign="middle">
		<table border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td align="right" width="17" height="23" ><img src="<c:url value='/sample/images/ct_btnbg01.gif'/>" width="17" height="23"></td>
				<td align="right" background="<c:url value='/sample/images/ct_btnbg02.gif'/>" class="ct_btn01">
					<a href="${ctx}/index.do">Go app. samples page</a>
				</td>
				<td align="right" width="14" height="23"><img src="<c:url value='/sample/images/ct_btnbg03.gif'/>" width="14" height="23"></td>
			</tr>
		</table>
	</td>
  </tr>
</table>
</div>
