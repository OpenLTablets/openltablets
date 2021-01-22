package org.openl.rules.project.resolving;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.openl.rules.project.model.Module;
import org.openl.rules.project.model.ProjectDescriptor;

public class ResolvingStrategiesTest {

    @Test
    public void testDescriptor() throws Exception {
        ProjectDescriptorBasedResolvingStrategy strategy = new ProjectDescriptorBasedResolvingStrategy();
        File projectFolder = new File("test-resources/descriptor");
        assertTrue(strategy.isRulesProject(projectFolder.toPath()));

        ProjectDescriptor descriptor = strategy.resolveProject(projectFolder.toPath());
        assertNotNull(descriptor);

        assertEquals(1, descriptor.getModules().size());
        assertEquals(1, descriptor.getClasspath().size());

        File nonProjectFolder = new File("test-resources");
        assertFalse(strategy.isRulesProject(nonProjectFolder.toPath()));
    }

    @Test
    public void testSimple() throws Exception {
        ResolvingStrategy resolvingStrategy = new SimpleXlsResolvingStrategy();
        File projectFolder = new File("test-resources/excel/");
        assertTrue(resolvingStrategy.isRulesProject(projectFolder.toPath()));
        ProjectDescriptor descriptor = resolvingStrategy.resolveProject(projectFolder.toPath());
        assertEquals(projectFolder.getName(), descriptor.getName());
        assertEquals(projectFolder.getCanonicalPath(), descriptor.getProjectFolder().toRealPath().toString());
        assertEquals(2, descriptor.getModules().size());
        Module moduleFirst = descriptor.getModules().get(0);
        assertEquals("Rules", moduleFirst.getName());
        Module moduleSecond = descriptor.getModules().get(1);
        assertEquals("Rules2", moduleSecond.getName());
    }

}
