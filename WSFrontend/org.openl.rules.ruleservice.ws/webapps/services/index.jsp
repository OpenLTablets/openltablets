<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>

    <c:set var="contextPath" value="${pageContext.request.contextPath}"/>

    <head>
        <meta charset="UTF-8" />

        <title>OpenL Tablets Web Services</title>

        <link href="${contextPath}/css/common.css" rel="stylesheet" />

        <style>
            .url,
            .date-time {
                padding-left: 3px;
            }

            .date-time {
                color: #9a9a9a;
            }

            .services,
            .services a,
            .service-name {
                margin: 0;
                font-family: verdana, helvetica, arial, sans-serif;
                font-size: 12px;
            }

            .services {
                color: #444;

                padding: 1px 0 1px 0;
                overflow: hidden;
                width: 100%;
                white-space: nowrap;
            }

            .services > td > div {
                padding-top: 2px;
                padding-bottom: 2px;
            }


            .service-description {
                font-size: 0; /* Collapse whitespaces */
                min-height: 16px;
            }

            .services > td > div.service-description {
                padding-top: 3px;
                padding-bottom: 3px;
            }

            .service-name {
                padding: 0 4px 0 3px;
                vertical-align: middle;
            }

            .expand-button, .collapse-button {
                vertical-align: middle;
                margin: 0;
                cursor: pointer;
                width: 16px;
                height: 16px;
                display: inline-block;
            }

            .expand-button {
                background: url(${contextPath}/images/plus.png) no-repeat center;
            }

            .collapse-button {
                background: url(${contextPath}/images/minus.png) no-repeat center;
            }

            .methods {
                margin-left: 29px;
                margin-top: 2px;
            }

            .methods > div {
                background: url(${contextPath}/images/bullet.png) no-repeat left center;
                min-height: 16px;
                padding-left: 18px;
                padding-top: 1px;
                padding-bottom: 1px;
                font-size: 11px;
                vertical-align: middle;
            }
        </style>

        <script>
            function pressButton(button, methods) {
                if (button.className == "expand-button") {
                    // Expand the node
                    button.className = "collapse-button";
                    methods.className = "methods";
                } else {
                    // Collapse the node
                    button.className = "expand-button";
                    methods.className = "methods hidden";
                }
            }
        </script>
    </head>

    <body>
        <div id="header">OpenL Tablets Web Services</div>
        <div id="main">
            <div>
                <form>
                    <div>
                        <c:if test="${requestScope.servicesGroup == null}">
                            <c:redirect url="/"/>
                        </c:if>
                        <c:if test="${!empty requestScope.servicesGroup}">
                            <c:forEach items="${requestScope.servicesGroup}" var="servicesGroup">

                                <h2>Available ${servicesGroup.groupName} services:</h2>

                                <table class="table">
                                    <tbody>
                                    <c:forEach items="${servicesGroup.availableServices}" var="service" varStatus="i">

                                            <tr class="services">
                                                <td>
                                                    <div class="date-time">
                                                        Started time:
                                                        <fmt:formatDate value="${service.startedTime}" pattern="MM/dd/yyyy hh:mm:ss a"/>
                                                    </div>

                                                    <div class="service-description">
                                                        <c:if test="${!empty service.methodNames}">
                                                            <span class="expand-button" onclick="pressButton(this, document.getElementById('methods${i.index}'));"></span>
                                                        </c:if>
                                                        <span class="service-name"><c:out value="${service.name}"/></span>

                                                        <div id="methods${i.index}" class="methods hidden">
                                                            <c:forEach items="${service.methodNames}" var="methodName">
                                                                <div><c:out value="${methodName}"/></div>
                                                            </c:forEach>
                                                        </div>
                                                    </div>

                                                    <c:if test="${!empty service.url}">
                                                        <div class="url" >
                                                            ${service.urlDescription}:
                                                            <a href="${contextPath}/${service.url}">${contextPath}/${service.url}</a>
                                                        </div>
                                                    </c:if>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>

                            </c:forEach>
                        </c:if>

                        <c:if test="${empty requestScope.servicesGroup}">
                            <h2>There are no available services</h2>
                        </c:if>
                    </div>

                </form>
            </div>
        </div>
        <div id="footer">
            &#169; 2014 <a style="text-decoration: none" href="http://openl-tablets.sf.net" target="_blank">OpenL Tablets</a>
        </div>
    </body>
</html>