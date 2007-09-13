<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
			pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://rules.openl.org/jsf" prefix="openl" %>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Insert title here</title></head>
<body>

<style type="text/css">
	.menuholderdiv {
		background: beige;
		padding: 4px;
		color: blue;
		border: 1px solid black;
	}

	.Minsk {
		font-style: italic;
	}
</style>

<f:view>
	<h:dataTable cellpadding="0" cellspacing="10px" width="700" border="0" var="record" value="#{report.records}">
		<f:facet name="header">
			<h:column>
				<h:outputText value="City"/>
			</h:column>
			<h:column>
				<h:outputText value="Country"/>
			</h:column>
		</f:facet>

		<h:column>
			<openl:popupMenu imageUrl="images/show_menu.gif" menuStyleClass="#{record.name}">
				<h:outputText value="#{record.name}"/>
				<f:facet name="menu">
					<h:outputText value="#{record.country}"/>
				</f:facet>
			</openl:popupMenu>
		</h:column>
		<h:column>
			<openl:popupMenu delay="700" tooltip="true">
				<h:outputText value="#{record.country}"/>
				<f:facet name="menu">
					<h:outputText value="#{record.name}"/>
				</f:facet>
			</openl:popupMenu>
		</h:column>

	</h:dataTable>
</f:view>
</body>
</html>