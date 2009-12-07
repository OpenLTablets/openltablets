package org.openl.rules.tableeditor.renderkit;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

public class TableViewerRenderer extends BaseRenderer {

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        TableEditor tableEditor = new TableEditor(context, component);
        writer.write(new HTMLRenderer().render(tableEditor, false, null, null));
    }

    @Override
    public void encodeChildren(FacesContext arg0, UIComponent arg1) throws IOException {
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
