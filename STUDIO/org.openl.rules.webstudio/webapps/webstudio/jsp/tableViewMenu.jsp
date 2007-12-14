<%
  ColorFilterHolder fh  = studio.getModel().getFilterHolder();
  String ft = request.getParameter("filterType");
  if (ft != null)
  {
    int ftype = Integer.parseInt(ft);
    fh.setFilterType(ftype);
  }

  String transparency = request.getParameter("transparency");
  if ("up".equals(transparency))
  {
    fh.setTransparency(fh.getTransparency() + 10);
  }
  if ("down".equals(transparency))
  {
    fh.setTransparency(fh.getTransparency() - 10);
  }

  String[] menuParams = {"transparency", "filterType"};
  String pars = WebTool.listParamsExcept(menuParams, request);

  String[] menuParamsView = {"transparency", "filterType", "view"};
  String parsView = WebTool.listParamsExcept(menuParamsView, request);


  view = studio.getModel().getTableView(request.getParameter("view"));
%>


<div class="menudiv">
    <td width="120">
<table class="top">
 <tr bgcolor="#FF8080">
 <td onmouseover="showmenu('blends')" onmouseout="hidemenu('blends')" title="<%=fh.getFilterName()%>">
   <img src="../images/<%=fh.getImageName()%>"><br/>
   <table class="menu" id="blends">
   <tr>
<%

  for(int f = 0; f < ColorFilterHolder.imageNames.length; ++f)
  {
%>

     <td class="menu"><a href="?<%=pars%>&filterType=<%=f%>"><img src="../images/<%=ColorFilterHolder.imageNames[f]%>" title="<%=ColorFilterHolder.filterNames[f]%>"/></a></td>

 <%
   }
 %>
   </tr>
   <tr>
     <td class="menu"><a href="?<%=parsView%>&view=view.developer"><img src="../images/developer-view.gif" title="Developer (Full) View"/></a></td>
     <td class="menu"><a href="?<%=parsView%>&view=view.business"><img src="../images/business-view.gif" title="Business View"/></a></td>
     <td class="menu"><a href="?<%=pars%>&transparency=down"><img src="../images/brightness_down.png" title="Less Transparent (<%=fh.getTransparency()%>%)"/></a></td>
     <td class="menu"><a href="?<%=pars%>&transparency=up"><img src="../images/brightness_up.png" title="More Transparent (<%=fh.getTransparency()%>%)"/></a></td>

   </tr>
   </table>
 </td></tr></table></td></div>
