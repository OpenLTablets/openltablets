package org.openl.rules.workspace.mock;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.openl.rules.common.ProjectException;
import org.openl.rules.repository.api.ResourceAPI;

public class MockResource extends MockArtefact implements ResourceAPI {
    public static final InputStream NULL_STREAM = new ByteArrayInputStream(new byte[0]);

    private String resourceType = "unknown";
    private byte[] content;

    public MockResource(String name) {
        super(name);
    }

    public String getResourceType() {
        return resourceType;
    }

    public InputStream getContent() throws ProjectException {
        return new ByteArrayInputStream(content);
    }

    public void setContent(InputStream inputStream) throws ProjectException {
        try {
            content = new byte[inputStream.available()];
            inputStream.read(content);
        } catch (IOException e) {
            setContent(NULL_STREAM);
        }
    }
}
