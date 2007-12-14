package org.openl.rules.workspace.production.client;

import org.openl.rules.repository.ProductionRepositoryFactoryProxy;
import org.openl.rules.repository.RFile;
import org.openl.rules.repository.RFolder;
import org.openl.rules.repository.RProductionDeployment;
import org.openl.rules.repository.RProject;
import org.openl.rules.repository.exceptions.RRepositoryException;
import org.openl.rules.workspace.deploy.DeployID;
import org.openl.rules.workspace.lw.impl.FolderHelper;
import org.openl.rules.workspace.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * This class can extract rules projects deployed into production JCR based environment to a specified location on
 * file system. Also it is a good place for higher level utility methods on top of production repository API. 
 */
public class JcrRulesClient {
    /**
     * Unpacks deployed project with given deploy id to <code>destFolder</code>.
     * The methods uses <code>RRepository</code> instance provided by <code>ProductionRepositoryFactoryProxy</code>
     * factory as production repository. 
     *
     * @param deployID identifier of deployed project
     * @param destFolder the folder to unpack the project to.
     *
     * @throws Exception if an error occurres
     */
    public void fetchDeployment(DeployID deployID, File destFolder) throws Exception {
        destFolder.mkdirs();
        FolderHelper.clearFolder(destFolder);

        RProductionDeployment rDeployment = ProductionRepositoryFactoryProxy.getRepositoryInstance().getDeployment(deployID.getName());

        Collection<RProject> projects = rDeployment.getProjects();
        for (RProject project : projects) {
            File projectFolder = new File(destFolder, project.getName());
            projectFolder.mkdirs();
            
            download(project.getRootFolder(), projectFolder);
        }
    }

    private void download(RFolder folder, File location) throws RRepositoryException, IOException {
        location.mkdirs();
        for (RFile rFile : folder.getFiles()) {
            IOUtil.copy(rFile.getContent(), new FileOutputStream(new File(location, rFile.getName())));
        }

        for (RFolder rFolder : folder.getFolders()) {
            download(rFolder, new File(location, rFolder.getName()));
        }
    }

    /**
     * Returns names of all existing deployments in production repository.
     *
     * @return collection of names
     * @throws RRepositoryException on repository error
     */
    public Collection<String> getDeploymentNames() throws RRepositoryException {
        return ProductionRepositoryFactoryProxy.getRepositoryInstance().getDeploymentNames();
    }

    

}
