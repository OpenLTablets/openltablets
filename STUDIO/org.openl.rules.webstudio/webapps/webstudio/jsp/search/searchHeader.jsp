<!--
<%@include file="header.jsp"%>
-->


<%
  String searchQuery = request.getParameter("searchQuery");
  if (searchQuery == null) searchQuery="";
%>


<body>
<form action ="search.jsp">

<!--
<table cellspacing=20 cellpadding=2>
-->
<table>
<tr>
<td rowspan=2>
<img src="../../images/openl-search.gif">
</td>

<td valign=bottom><a href="allIndexFrame.jsp"><font size=-1>&nbsp;Index</font></a></td>
<td valign=bottom align=center><a href="advSearch.jsp"><font size=-1>Advanced Search</font></a></td>
<td valign=bottom align=right><a href="../../html/ws-intro.html#search"><font size=-1>Help&nbsp;</font></a></td>
</tr>

<tr>
<td colspan=3 valign=top><input size=50 name="searchQuery"  value="<%=org.openl.util.StringTool.encodeHTMLBody(searchQuery)%>"/></td>
<td valign=top><input type="submit" name="search_button" value="Search"/></td>
</tr>
</table>
</form>
