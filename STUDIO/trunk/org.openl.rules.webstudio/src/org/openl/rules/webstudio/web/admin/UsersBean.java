package org.openl.rules.webstudio.web.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.openl.rules.security.DefaultPrivileges;
import org.openl.rules.security.Group;
import org.openl.rules.security.Privilege;
import org.openl.rules.security.SimpleUser;
import org.openl.rules.security.User;
import org.openl.rules.webstudio.service.GroupManagementService;
import org.openl.rules.webstudio.service.UserManagementService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Andrei Astrouski
 */
@ManagedBean
@RequestScoped
public class UsersBean {

    public static final String VALIDATION_EMPTY = "Can not be empty";
    public static final String VALIDATION_MAX = "Must be less than 25";
    public static final String VALIDATION_USERNAME = "Invalid characters (valid: latin letters, numbers, _ and -)";
    public static final String VALIDATION_GROUPS = "Please select at least one group";

    @Size(max=25, message=VALIDATION_MAX)
    private String firstName;

    @Size(max=25, message=VALIDATION_MAX)
    private String lastName;

    @NotBlank(message=VALIDATION_EMPTY)
    @Size(max=25, message=VALIDATION_MAX)
    @Pattern(regexp="([a-zA-Z0-9-_]*)?", message=VALIDATION_USERNAME)
    private String username;

    @NotBlank(message=VALIDATION_EMPTY)
    @Size(max=25, message=VALIDATION_MAX)
    private String password;

    @NotEmpty(message=VALIDATION_GROUPS)
    private List<String> groups;

    @ManagedProperty(value="#{userManagementService}")
    protected UserManagementService userManagementService;

    @ManagedProperty(value="#{groupManagementService}")
    protected GroupManagementService groupManagementService;

    /**
     * Validation for existed user
     */
    public void validateUsername(FacesContext context, UIComponent toValidate, Object value) {
        User user = null;
        try {
            user = userManagementService.loadUserByUsername((String) value);
        } catch (UsernameNotFoundException e) { }

        if (user != null) {
            throw new ValidatorException(
                    new FacesMessage("User with such name already exists"));
        }
    }

    public List<User> getUsers() {
        return userManagementService.getAllUsers();
    }

    public String[] getGroups(Object objUser) {
        List<String> groups = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Collection<Privilege> authorities = (Collection<Privilege>) ((User) objUser).getAuthorities();
        for (Privilege authority : authorities) {
            if (authority instanceof Group) {
                groups.add(authority.getName());
            }
        }
        return groups.toArray(new String[groups.size()]);
    }

    public String[] getOnlyAdminGroups(Object objUser) {
        if (!isOnlyAdmin(objUser)) {
            return new String[0];
        }

        String adminPrivilege = DefaultPrivileges.PRIVILEGE_ADMINISTRATE.name();
        String allPrivileges = DefaultPrivileges.PRIVILEGE_ALL.name();

        List<String> groups = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Collection<Privilege> authorities = (Collection<Privilege>) ((User) objUser).getAuthorities();
        for (Privilege authority : authorities) {
            if (authority instanceof Group) {
                Group group = (Group) authority;
                if (group.hasPrivilege(adminPrivilege) || group.hasPrivilege(allPrivileges)) {
                    groups.add(group.getAuthority());
                }
            }
        }

        return groups.toArray(new String[groups.size()]);
    }

    private List<Privilege> getSelectedGroups() {
        List<Privilege> resultGroups = new ArrayList<Privilege>();
        Map<String, Group> groups = new HashMap<String, Group>();

        if (this.groups != null) {
            for (String groupName : this.groups) {
                groups.put(groupName, groupManagementService.getGroupByName(groupName));
            }

            for (Group group : new ArrayList<Group>(groups.values())) {
                if (!groups.isEmpty()) {
                    removeIncludedGroups(group, groups);
                }
            }

            for (Group group : groups.values()) {
                resultGroups.add(group);
            }
        }

        return resultGroups;
    }

    public void addUser() {
        userManagementService.addUser(
                new SimpleUser(firstName, lastName, username, password, getSelectedGroups()));
    }

    public void editUser() {
        userManagementService.updateUser(
                new SimpleUser(firstName, lastName, username, null, getSelectedGroups()));
    }

    private void removeIncludedGroups(Group group, Map<String, Group> groups) {
        Set<String> groupNames = new HashSet<String>(groups.keySet());
        for (String checkGroupName : groupNames) {
            if (!group.getName().equals(checkGroupName) &&
                    (group.hasPrivilege(checkGroupName) ||
                            group.hasPrivilege(DefaultPrivileges.PRIVILEGE_ALL.name()))) {
                Group includedGroup = groups.get(checkGroupName);
                if (includedGroup != null) {
                    removeIncludedGroups(includedGroup, groups);
                    groups.remove(checkGroupName);
                }
            }
        }
    }

    public boolean isOnlyAdmin(Object objUser) {
        String adminPrivilege = DefaultPrivileges.PRIVILEGE_ADMINISTRATE.name();
        String allPrivileges = DefaultPrivileges.PRIVILEGE_ALL.name();
        return (((User) objUser).hasPrivilege(adminPrivilege) || ((User) objUser).hasPrivilege(allPrivileges))
                && userManagementService.getUsersByPrivilege(adminPrivilege).size() == 1;
    }

    public void deleteUser(String username) {
        userManagementService.deleteUser(username);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<SelectItem> getGroupItems() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        List<Group> groups = groupManagementService.getGroups();
        for (Group group : groups) {
            result.add(new SelectItem(group.getName(), group.getDisplayName()));
        }
        return result;
    }

    public void setUserManagementService(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    public void setGroupManagementService(
            GroupManagementService groupManagementService) {
        this.groupManagementService = groupManagementService;
    }

}
