package org.openl.rules.ruleservice.loader;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openl.rules.project.abstraction.Deployment;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocalTemporaryDeploymentsStorageTest {

    private static DataSource dataSource;

    private Deployment deployment;

    @BeforeClass
    public static void setDataSource() throws Exception {
        dataSource = new JcrDataSource();
    }

    @AfterClass
    public static void releaseDataSource() throws Exception {
        ((JcrDataSource) dataSource).destroy();
    }

    @Before
    public void getDeployment() {
        Collection<Deployment> deployments = dataSource.getDeployments();
        assertTrue(!deployments.isEmpty());
        deployment = deployments.iterator().next();
    }

    @Test
    public void testContainsDeloymentAndLoadDeployment() {
        LocalTemporaryDeploymentsStorage storage = new LocalTemporaryDeploymentsStorage();
        assertFalse(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        storage.loadDeployment(deployment);
        assertTrue(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
    }

    @Test
    public void testClearStorage() {
        LocalTemporaryDeploymentsStorage storage = new LocalTemporaryDeploymentsStorage();
        assertFalse(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        storage.loadDeployment(deployment);
        assertTrue(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        storage.clear();
        assertFalse(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
    }

    @Test
    public void testLoadDeployment() {
        LocalTemporaryDeploymentsStorage storage = new LocalTemporaryDeploymentsStorage();
        assertNull(storage.getDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        assertFalse(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        storage.loadDeployment(deployment);
        assertTrue(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
    }

    @Test
    public void testGetDeployment() {
        LocalTemporaryDeploymentsStorage storage = new LocalTemporaryDeploymentsStorage();
        assertNull(storage.getDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        assertFalse(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        storage.loadDeployment(deployment);
        assertTrue(storage.containsDeployment(deployment.getDeploymentName(), deployment.getCommonVersion()));
        Deployment deployment1 = storage.getDeployment(deployment.getDeploymentName(), deployment.getCommonVersion());
        assertNotNull(deployment1);
        Deployment deployment2 = storage.getDeployment(deployment.getDeploymentName(), deployment.getCommonVersion());
        assertTrue(deployment1 == deployment2);
    }

}
