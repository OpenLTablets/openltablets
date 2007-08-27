<%
     org.openl.rules.ui.AllTestsRunResult atr = studio.getModel().getRunMethods(elementID); 
     if (atr != null)
     {
	     org.openl.rules.ui.AllTestsRunResult.Test[] tests = atr.getTests();
	     if (tests.length > 0)
	     {
	%>
			<h1>Select one of the available runs by clicking on it:</h1>
	<%    
		 
	     	for(int i = 0; i < tests.length; ++i)
	     	{
	     		for(int j = 0; j < tests[i].ntests(); ++j)
	     		{
	     		
	     			String tname = org.openl.rules.webtools.WebTool.encodeURL(tests[i].getTestName());
	     			String tdescrURL = org.openl.rules.webtools.WebTool.encodeURL(tests[i].getTestDescription(j));
	     			String tdescrBody = org.openl.rules.webtools.WebTool.encodeHTMLBody(tests[i].getTestDescription(j));
	     			

	     			
	%>
					<p>&nbsp;<a href="runMethod.jsp?elementID=<%=s_id%>&testName=<%=tname%>&testID=<%=j%>&testDescr=<%=tdescrURL%>"><%=tdescrBody%></a>
					<a href="runMethod.jsp?elementID=<%=s_id%>&testName=<%=tname%>&testID=<%=j%>&testDescr=<%=tdescrURL%>" title="Run"><img border=0 src="../images/test.gif" /></a>
					         
					&nbsp;<a href="benchmarkMethod.jsp?elementID=<%=s_id%>&testName=<%=tname%>&testID=<%=j%>&testDescr=<%=tdescrURL%>"><img src="../images/clock-icon.png" border="0" title="Benchmark"/></a>
					     
	<%     	
				}
	     	}
	 	  } 	
	}	   
   
%>
