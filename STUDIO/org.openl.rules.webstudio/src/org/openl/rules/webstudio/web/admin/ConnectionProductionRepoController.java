package org.openl.rules.webstudio.web.admin;

import java.io.File;
import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang3.StringUtils;
import org.openl.rules.repository.RRepository;
import org.openl.rules.repository.exceptions.RRepositoryException;

/**
 * @author Pavel Tarasevich
 * 
 */

@ManagedBean
@ViewScoped
public class ConnectionProductionRepoController extends AbstractProductionRepoController {
    private static final String PRODUCTION_REPOSITORY_CONNECTION_TYPE = "connection";

    @Override
    public void save() {
        RepositoryConfiguration repoConfig = createRepositoryConfiguration(PRODUCTION_REPOSITORY_CONNECTION_TYPE);

        if (!isInputParamValid(repoConfig)) {
            return;
        }

        if (!checkConnection(repoConfig)) {
            return;
        }

        //repoConfig.save();
        addProductionRepoToMainConfig(repoConfig);
        clearForm();
    }

    @Override
    public void setType(String type) {
        super.setType(type);
        if (isLocal()) {
            setSecure(false);
        }
    }

    private boolean checkConnection(RepositoryConfiguration repoConfig) {
        setErrorMessage("");

        if (this.getType().equals("local")) {
            return checkLocalRepo(repoConfig);
        } else {
            return checkRemoteConnection(repoConfig);
        }
    }

    private boolean checkRemoteConnection(RepositoryConfiguration repoConfig) {
        try {
            RepositoryValidators.validateConnection(repoConfig, getProductionRepositoryFactoryProxy());
            return true;
        } catch (RepositoryValidationException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
    }

    private boolean checkLocalRepo(RepositoryConfiguration repoConfig) {
        File repoDir = new File(repoConfig.getPath());
        String errorMessage = "There is no repository in this folder. Please, correct folder path";
        if (repoDir.exists()) {
            File[] files = repoDir.listFiles();
            RepoDirChecker checker = new RepoDirChecker(repoConfig.getPath());
            if (files == null) {
                setErrorMessage(errorMessage);
                return false;
            }

            for (File file : files) {
                try {
                    checker.check(file.getCanonicalPath());
                } catch (IOException e) {
                    setErrorMessage(errorMessage);
                    return false;
                }
            }

            if(!checker.isRepoThere()) {
                setErrorMessage(errorMessage);
                return false;
            }

            if (!StringUtils.isEmpty(repoConfig.getLogin())) {
                try {
                    RRepository repository = this.getProductionRepositoryFactoryProxy().getFactory(repoConfig.getProperties()).getRepositoryInstance();
                    repository.release();
                } catch (RRepositoryException e) {
                    setErrorMessage("Invalid login or password. Please, check login and password");
                    return false;
                }
            }
        } else {
            setErrorMessage(errorMessage);
            return false;
        }

        this.setChecked(true);
        return true;
    }

    public static class RepoDirChecker{
        private String root = "";
        private boolean hasRepoDir = false;
        private boolean hasVersionDir = false;
        private boolean hasWorkSpacesDir = false;

        public RepoDirChecker(String root) {
            this.root = root;
        }

        public void check(String str) {
            String subFolder = str.toLowerCase().replace(root.toLowerCase(), "");

            checkRepoDir(subFolder);
            checkVersionDir(subFolder);
            checkWorkSpacesDir(subFolder);
        }

        private void checkRepoDir(String dir) {
            if (dir.startsWith("repository") || dir.startsWith("\\repository")) {
                hasRepoDir = true;
            }
        }

        private void checkVersionDir(String dir) {
            if (dir.startsWith("version") || dir.startsWith("\\version")) {
                hasVersionDir = true;
            }
        }

        private void checkWorkSpacesDir(String dir) {
            if (dir.startsWith("workspaces") || dir.startsWith("\\workspaces")) {
                hasWorkSpacesDir = true;
            }
        }

        public boolean isRepoThere() {
            return hasRepoDir && hasVersionDir && hasWorkSpacesDir;
        }
    }
}
