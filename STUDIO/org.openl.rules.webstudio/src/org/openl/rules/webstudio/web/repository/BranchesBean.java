package org.openl.rules.webstudio.web.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import org.openl.rules.project.abstraction.RulesProject;
import org.openl.rules.repository.api.BranchRepository;
import org.openl.rules.repository.api.MergeConflictException;
import org.openl.rules.repository.api.Repository;
import org.openl.rules.webstudio.web.repository.merge.ConflictUtils;
import org.openl.rules.webstudio.web.repository.merge.MergeConflictInfo;
import org.openl.rules.webstudio.web.servlet.RulesUserSession;
import org.openl.rules.webstudio.web.util.WebStudioUtils;
import org.openl.rules.workspace.WorkspaceException;
import org.openl.rules.workspace.uw.UserWorkspace;
import org.openl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@ViewScoped
public class BranchesBean {
    private final Logger log = LoggerFactory.getLogger(BranchesBean.class);

    private String currentProjectName;

    private List<String> branches;

    private String currentBranch;

    private String branchToMergeFrom;
    private String branchToMergeTo;

    public String getCurrentProjectName() {
        return currentProjectName;
    }

    public void setCurrentProjectName(String currentProjectName) {
        this.currentProjectName = currentProjectName;
    }

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }

    public List<String> getAvailableBranches() {
        RulesProject project = getProject(currentProjectName);
        if (project != null) {
            Repository repository = project.getDesignRepository();
            if (repository.supports().branches()) {
                try {
                    return new ArrayList<>(((BranchRepository) repository).getBranches(null));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return Collections.emptyList();
    }

    public List<SelectItem> getBranchesToMerge() {
        List<SelectItem> result = new ArrayList<>();

        RulesProject project = getProject(currentProjectName);
        if (project != null) {
            Repository repository = project.getDesignRepository();
            if (repository.supports().branches()) {
                try {
                    List<String> projectBranches = ((BranchRepository) repository).getBranches(currentProjectName);

                    List<SelectItem> projectBranchesList = new ArrayList<>();
                    for (String projectBranch : projectBranches) {
                        projectBranchesList.add(new SelectItem(projectBranch, projectBranch));
                    }
                    if (!projectBranchesList.isEmpty()) {
                        SelectItemGroup projectBranchesGroup = new SelectItemGroup("Project branches");
                        projectBranchesGroup.setSelectItems(projectBranchesList.toArray(new SelectItem[0]));
                        result.add(projectBranchesGroup);
                    }

                    List<SelectItem> otherBranchesList = new ArrayList<>();
                    for (String b : ((BranchRepository) repository).getBranches(null)) {
                        if (!projectBranches.contains(b)) {
                            otherBranchesList.add(new SelectItem(b, b));
                        }
                    }
                    if (!otherBranchesList.isEmpty()) {
                        SelectItemGroup otherBranchesGroup = new SelectItemGroup("Other branches");
                        otherBranchesGroup.setSelectItems(otherBranchesList.toArray(new SelectItem[0]));
                        result.add(otherBranchesGroup);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    return Collections.emptyList();
                }
            }
        }

        return result;
    }

    public void swapBranches() {
        String temp = branchToMergeFrom;
        branchToMergeFrom = branchToMergeTo;
        branchToMergeTo = temp;
    }

    public void merge() {
        try {
            if (branchToMergeFrom == null || branchToMergeTo == null) {
                WebStudioUtils.addErrorMessage("Choose the branches to merge.");
                return;
            }
            if (branchToMergeFrom.equals(branchToMergeTo)) {
                WebStudioUtils.addErrorMessage("Can't merge the branch '" + branchToMergeFrom + "' to itself.");
                return;
            }
            RulesProject project = getProject(currentProjectName);
            if (project != null) {
                Repository designRepository = project.getDesignRepository();
                boolean opened = project.isOpened();

                String userId = getUserWorkspace().getUser().getUserId();
                ((BranchRepository) designRepository).forBranch(branchToMergeTo).merge(branchToMergeFrom, userId, null);

                if (opened) {
                    if (project.isDeleted()) {
                        project.close();
                    } else {
                        // Update files
                        project.open();
                    }
                }
                setWasMerged(true);
            }
        } catch (MergeConflictException e) {
            MergeConflictInfo info = new MergeConflictInfo(e, getProject(currentProjectName), branchToMergeFrom, branchToMergeTo);
            ConflictUtils.saveMergeConflict(info);
            log.debug("Failed to save the project because of merge conflict.", e);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (StringUtils.isBlank(msg)) {
                msg = "Error during merge operation.";
            }
            log.error(msg, e);
            WebStudioUtils.addErrorMessage(msg);
        }
    }

    public void setWasMerged(boolean wasMerged) {
        if (wasMerged) {
            WebStudioUtils.addInfoMessage("Branches were merged successfully.");
        }
    }

    public void save() {
        if (branches == null || branches.isEmpty()) {
            WebStudioUtils.addErrorMessage("At least one branch must be selected.");
            return;
        }
        try {
            RulesProject project = getProject(currentProjectName);
            if (project != null) {
                BranchRepository repository = (BranchRepository) project.getDesignRepository();

                List<String> existingBranches = getBranches(project);
                for (String branch : branches) {
                    if (!existingBranches.contains(branch)) {
                        repository.createBranch(currentProjectName, branch);
                    }
                }

                for (String branch : existingBranches) {
                    if (!branches.contains(branch)) {
                        repository.deleteBranch(currentProjectName, branch);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            WebStudioUtils.addErrorMessage("Cannot copy the project: " + e.getMessage());
        }
    }

    public void setInitProject(String currentProjectName) {
        try {
            this.currentProjectName = currentProjectName;

            RulesProject project = getProject(currentProjectName);
            if (project != null) {
                branches = getBranches(project);
                currentBranch = project.getBranch();
                branchToMergeFrom = null;
                branchToMergeTo = currentBranch;
            } else {
                currentBranch = null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public String getBranchToMergeFrom() {
        return branchToMergeFrom;
    }

    public void setBranchToMergeFrom(String branchToMergeFrom) {
        this.branchToMergeFrom = branchToMergeFrom;
    }

    public String getBranchToMergeTo() {
        return branchToMergeTo;
    }

    public void setBranchToMergeTo(String branchToMergeTo) {
        this.branchToMergeTo = branchToMergeTo;
    }

    private List<String> getBranches(RulesProject project) {
        String projectName = project.getName();
        Repository repository = project.getDesignRepository();
        if (repository.supports().branches()) {
            try {
                return new ArrayList<>(((BranchRepository) repository).getBranches(projectName));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }

        return Collections.emptyList();
    }

    private RulesProject getProject(String projectName) {
        if (StringUtils.isBlank(projectName)) {
            return null;
        }

        try {
            UserWorkspace userWorkspace = getUserWorkspace();
            if (!userWorkspace.hasProject(projectName)) {
                return null;
            }

            return userWorkspace.getProject(projectName, false);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private UserWorkspace getUserWorkspace() throws WorkspaceException {
        RulesUserSession rulesUserSession = WebStudioUtils.getRulesUserSession(WebStudioUtils.getSession());
        return rulesUserSession.getUserWorkspace();
    }
}
