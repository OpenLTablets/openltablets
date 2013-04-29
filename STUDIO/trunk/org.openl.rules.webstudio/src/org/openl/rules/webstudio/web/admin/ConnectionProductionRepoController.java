package org.openl.rules.webstudio.web.admin;

import java.io.File;
import java.io.IOException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.lang.StringUtils;
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
    private boolean connectionChecked = false;

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
    public void clearForm() {
       super.clearForm();
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
            RRepository repository = this.getProductionRepositoryFactoryProxy().getFactory(repoConfig.getProperties()).getRepositoryInstance();
            repository.release();
            return true;
        } catch (RRepositoryException e) {
            Throwable resultException = e;

            while (resultException.getCause() != null) {
                resultException = resultException.getCause();
            }

            if (resultException instanceof javax.jcr.LoginException) {
                if (!this.isSecure()) {
                    setErrorMessage("Connection is secure. Please, insert login and password");
                    return false;
                } else {
                    setErrorMessage("Invalid login or password. Please, check login and password");
                    return false;
                }
            }

            setErrorMessage(resultException.getMessage());
            return false;
        }
    }

    private boolean checkLocalRepo(RepositoryConfiguration repoConfig) {
        File repoDir = new File(repoConfig.getPath());
        if (repoDir.exists()) {
            File[] files = repoDir.listFiles();
            RepoDirChecker checker = new RepoDirChecker(repoConfig.getPath());

            for (int i = 0; i < files.length; i++) {
                try {
                    checker.check(files[i].getCanonicalPath());
                } catch (IOException e) {
                    setErrorMessage("There is no repository in this folder. Please, correct folder path");
                    return false;
                }
            }

            if(!checker.isRepoThere()) {
                setErrorMessage("There is no repository in this folder. Please, correct folder path");
                return false;
            }

            if (checker.isRepoThere() && !StringUtils.isEmpty(repoConfig.getLogin())) {
                try {
                    RRepository repository = this.getProductionRepositoryFactoryProxy().getFactory(repoConfig.getProperties()).getRepositoryInstance();
                    repository.release();
                } catch (RRepositoryException e) {
                    setErrorMessage("Invalid login or password. Please, check login and password");
                    return false;
                }
            }
        } else {
            setErrorMessage("There is no repository in this folder. Please, correct folder path");
            return false;
        }

        this.setChecked(true);
        return true;
    }

    public boolean isConnectionChecked() {
        return connectionChecked;
    }

    public void setConnectionChecked(boolean connectionChecked) {
        this.connectionChecked = connectionChecked;
    }

    public class RepoDirChecker{
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
