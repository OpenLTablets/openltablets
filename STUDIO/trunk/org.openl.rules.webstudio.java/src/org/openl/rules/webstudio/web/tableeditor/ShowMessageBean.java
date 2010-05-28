package org.openl.rules.webstudio.web.tableeditor;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openl.message.OpenLMessage;
import org.openl.message.Severity;
import org.openl.rules.webtools.jsf.FacesUtils;

public class ShowMessageBean {

    public ShowMessageBean() {
    }

    public List<OpenLMessage> getMessage() {
        String type = FacesUtils.getRequestParameter("type");
        String summary = FacesUtils.getRequestParameter("summary");

        OpenLMessage message = new OpenLMessage(summary, StringUtils.EMPTY, Severity.valueOf(type));

        return Collections.singletonList(message);
    }

}
