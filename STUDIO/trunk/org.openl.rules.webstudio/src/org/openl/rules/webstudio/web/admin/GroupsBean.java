package org.openl.rules.webstudio.web.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.Size;

import org.apache.commons.lang.ArrayUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.openl.commons.web.jsf.FacesUtils;
import org.openl.rules.security.DefaultPrivileges;
import org.openl.rules.security.Group;
import org.openl.rules.security.Privilege;
import org.openl.rules.security.SimpleGroup;
import org.openl.rules.webstudio.service.GroupManagementService;

// TODO Needs performance optimization
/**
 * @author Andrei Astrouski
 */
@ManagedBean
@RequestScoped
public class GroupsBean {
    
    public static final String VALIDATION_EMPTY = "Can not be empty";
    public static final String VALIDATION_MAX = "Size must be between 1 and 25";

    @NotBlank(message=VALIDATION_EMPTY)
    @Size(max=25, message=VALIDATION_MAX)
    private String name;

    /* Used for editing*/
    @NotBlank(message=VALIDATION_EMPTY)
    @Size(max=25, message=VALIDATION_MAX)
    private String newName;
    private String oldName;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getOldName() {
        return oldName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManagedProperty(value="#{groupManagementService}")
    private GroupManagementService groupManagementService;

    /**
     * Validation for existed group
     */
    public void validateGroupName(FacesContext context, UIComponent toValidate, Object value) {
        if (groupManagementService.isGroupExist((String) value)) {
            throw new ValidatorException(
                    new FacesMessage("Group with such name already exists"));
        }
    }

    public Privilege[] getDefaultPrivileges() {
        Privilege[] privileges = DefaultPrivileges.values();
        return (Privilege[]) ArrayUtils.removeElement(
                privileges, DefaultPrivileges.PRIVILEGE_ALL);
    }

    public List<String> getPrivileges(String groupName) {
        List<String> result = new ArrayList<String>();
        Group group = groupManagementService.getGroupByName(groupName);
        Collection<Privilege> privileges = group.getPrivileges();
        for (Privilege privilege : privileges) {
            if (privilege instanceof Group) {
                result.addAll(getPrivileges(privilege.getName()));
            } else {
                result.add(privilege.getName());
            }
        }
        return result;
    }

    public List<String> getIncludedGroups(String groupName) {
        List<String> result = new ArrayList<String>();
        Group group = groupManagementService.getGroupByName(groupName);
        Collection<Privilege> authorities = group.getPrivileges();
        for (Privilege authority : authorities) {
            if (authority instanceof Group) {
                String incGroupName = authority.getName();
                // Don't use Set
                List<String> incGroups = getIncludedGroups(incGroupName);
                for (String incGroup : incGroups) {
                    if (!result.contains(incGroup)) {
                        result.add(incGroup);
                    }
                }
                result.add(incGroupName);
            }
        }
        return result;
    }

    public List<String> getNonGroupPrivileges(String groupName) {
        List<String> result = new ArrayList<String>();
        Group group = groupManagementService.getGroupByName(groupName);
        Collection<Privilege> authorities = group.getPrivileges();
        for (Privilege authority : authorities) {
            if (!(authority instanceof Group)) {
                result.add(authority.getDisplayName());
            }
        }
        return result;
    }

    public List<Group> getGroups() {
        return groupManagementService.getGroups();
    }

    private Collection<Privilege> getSelectedAuthorities() {
        Collection<Privilege> authorities = new ArrayList<Privilege>();

        String[] privilegesParam = FacesUtils.getRequest().getParameterValues("privilege");
        List<String> privileges = new ArrayList<String>(Arrays.asList(
                privilegesParam == null ? new String[0] : privilegesParam));
        privileges.add(0, DefaultPrivileges.PRIVILEGE_VIEW_PROJECTS.name());

        // Admin
        if (privileges.size() == DefaultPrivileges.values().length - 1) {
            authorities.add(DefaultPrivileges.PRIVILEGE_ALL);

        } else {
            Map<String, Group> groups = new java.util.HashMap<String, Group>();
            String[] groupNames = FacesUtils.getRequest().getParameterValues("group");
            if (groupNames != null) {
                for (String groupName : groupNames) {
                    groups.put(groupName, groupManagementService.getGroupByName(groupName));
                }

                for (Group group : new ArrayList<Group>(groups.values())) {
                    if (!groups.isEmpty()) {
                        removeIncludedGroups(group, groups);
                    }
                }

                removeIncludedPrivileges(privileges, groups);

                for (Group group : groups.values()) {
                    authorities.add(group);
                }
            }

            for (String privilegeName : privileges) {
                authorities.add(DefaultPrivileges.valueOf(privilegeName));
            }
        }

        return authorities;
    }

    public void addGroup() {
        groupManagementService.addGroup(
                new SimpleGroup(name, description, getSelectedAuthorities()));
    }

    public void editGroup() {
        groupManagementService.updateGroup(oldName,
                new SimpleGroup(newName, description, getSelectedAuthorities()));
    }

    private void removeIncludedGroups(Group group, Map<String, Group> groups) {
        Set<String> groupNames = new HashSet<String>(groups.keySet());
        for (String checkGroupName : groupNames) {
            if (!group.getName().equals(checkGroupName) &&
                    group.hasPrivilege(checkGroupName)) {
                Group includedGroup = groups.get(checkGroupName);
                if (includedGroup != null) {
                    removeIncludedGroups(includedGroup, groups);
                    groups.remove(checkGroupName);
                }
            }
        }
    }

    private void removeIncludedPrivileges(List<String> privileges, Map<String, Group> groups) {
        for (String privilege : new ArrayList<String>(privileges)) {
            for (Group group : groups.values()) {
                if (group.hasPrivilege(privilege)) {
                    privileges.remove(privilege);
                }
            }
        }
    }

    public boolean isOnlyAdmin(Object objGroup) {
        String allPrivileges = DefaultPrivileges.PRIVILEGE_ALL.name();
        return ((Group) objGroup).hasPrivilege(allPrivileges)
                && groupManagementService.getGroupsByPrivilege(allPrivileges).size() == 1;
    }

    public void deleteGroup(String name) {
        groupManagementService.deleteGroup(name);
    }

    public void setGroupManagementService(GroupManagementService groupManagementService) {
        this.groupManagementService = groupManagementService;
    }

}
