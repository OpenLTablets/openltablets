<jsp:useBean id='studio' scope='session' class="org.openl.rules.ui.studio.WebStudio" />
<jsp:useBean id="tracer" scope="session" class="org.openl.rules.ui.TraceHelper"/>


<%@include file="checkTimeout.jsp"%>


<% 
	String s_id = request.getParameter("elementID"); 
	
//	System.out.println("Params:" + request.getParameterMap());
   	
   	String t_id = request.getParameter("toggle"); 
   	 	
   	int elementID = -100;	
   	if (s_id != null)
   	{
     	elementID = Integer.parseInt(s_id);
     	studio.setTableID(elementID);
	   	String url = studio.getModel().makeXlsUrl(elementID);
	   	String uri = studio.getModel().getUri(elementID);
	   	String text = org.openl.rules.webtools.indexer.FileIndexer.showElementHeader(uri);   
	   	String name = studio.getModel().getDisplayNameFull(elementID);
	   	tracer.setName(name);
	   	org.openl.vm.Tracer t =  studio.getModel().traceElement(elementID);
	   	tracer.setObjects(t.getTracerObjects());
    }
   
   
	if (t_id != null)
   	{
   		int toggleID = Integer.parseInt(t_id);
   		
   		tracer.toggle(toggleID);
   		
   	} 
	   
%>


<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1257">
<title>OpenL Tracing</title>
<link href="../css/style1.css" rel="stylesheet" type="text/css"/>

</head>

<body>
<h3> Results of tracing <i><%=tracer.getName()%></i> </h3>
<p> 


<table>
<%
	org.openl.vm.ITracerObject[] ito = tracer.getObjects();
	for(int i = 0; i < ito.length; ++i)
	{
		int id = tracer.getID(ito[i]);
%>
<tr>
<td>	
	<a href="traceMethod.jsp?toggle=<%=id%>"><%=i+1%>.&nbsp;  <%=ito[i].getDisplayMessage()%></a>

<%
 if (tracer.isDisplayed(id))
 {
 	org.openl.vm.ITracerObject[] rtt = ito[i].getTracerObjects(); 
 	for(int j=0; j < rtt.length; ++j)
 	{
%>

<p>&nbsp;&nbsp;<%="" + (i+1) + "." + (j+1) + ". " + rtt[j].getDisplayMessage()%>


<%
	}
  }		
%>

</td>		
<%
 if (tracer.isDisplayed(id))
 {
%>
	<td>
	<%=tracer.showTrace(id)%>
	</td>

<%
 	
 }

%>

</tr>
<%
	}
%>
<p>

</body>
