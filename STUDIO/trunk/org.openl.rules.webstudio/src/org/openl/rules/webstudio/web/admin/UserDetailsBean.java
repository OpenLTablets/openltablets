package org.openl.rules.webstudio.web.admin;

import java.util.ArrayList;
import java.util.Collection;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
import javax.validation.constraints.Size;

import org.apache.commons.lang.StringUtils;
import org.openl.rules.security.Privilege;
import org.openl.rules.security.SimpleUser;
import org.openl.rules.security.User;
import org.openl.rules.webstudio.security.CurrentUserInfo;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;

@ManagedBean
@RequestScoped
public class UserDetailsBean extends UsersBean {
    public static final String VALIDATION_MAX = "Must be less than 25";

    private User user;
    private String newPassword;
    private String confirmPassword;
    private CurrentUserInfo userInfo;
    private org.openl.rules.security.User simpleUser;
    private boolean isPasswordValid = false;
    private String currentPassword;
    private String userPassword;

    @Size(max=25, message=VALIDATION_MAX)
    private String userFirstName;

    @Size(max=25, message=VALIDATION_MAX)
    private String userLastName;

    public UserDetailsBean() {
        super();
    }

    /**
     * Returns the current logged in user
     * 
     * @return org.openl.rules.security.User
     */
    public User getUser() {
        userInfo = new CurrentUserInfo();
        setUsername(userInfo.getUser().getUsername());
        user = userManagementService.loadUserByUsername(userInfo.getUser().getUsername());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setCurrentPassword(user.getPassword());
        return user;
    }

    /**
     * Return the user's privileges
     * 
     * @return Collection of user's privileges
     */
    private Collection<Privilege> getPriveleges() {
        Collection<Privilege> privileges = new ArrayList<Privilege>();

        for (GrantedAuthority auth : user.getAuthorities()) {
            Privilege group = (Privilege) groupManagementService.getGroupByName(auth.getAuthority());
            privileges.add(group);
        }
        return privileges;
    }

    /**
     * Updates the user's firstName, lastName, passWord
     */
    @Override
    public void editUser() {

        if (isPasswordValid) {
            String encodedPassword = new Md5PasswordEncoder().encodePassword(newPassword, null);
            setCurrentPassword(encodedPassword);
        } else {
            setCurrentPassword(getUser().getPassword());
        }

        if (userFirstName == null) {
            userFirstName = getFirstName();
        }

        if (userLastName == null) {
            userLastName = getLastName();
        }

        simpleUser = new SimpleUser(getUserFirstName(), getUserLastName(), getUsername(), currentPassword, getPriveleges());
        userManagementService.updateUser(simpleUser);
    }

    /**
     * Validates newPassword and confirmPassword on identity. If newPassword
     * isEmty, then validation isn't needed
     * 
     * @param context
     * @param component
     * @param value
     */
    public void passwordsValidator(FacesContext context, UIComponent component, Object value) {
        newPassword = (String) value;

        if (!StringUtils.isEmpty(newPassword)) {
            UIInput uiInputConfirmPassword = (UIInput) component.getAttributes().get("confirmPassword");
            String confirmPasswordString = uiInputConfirmPassword.getSubmittedValue().toString();
            UIInput uiInputPassword = (UIInput) component.getAttributes().get("currentPassword");
            String passwordString = uiInputPassword.getValue().toString();
            String userPasswordHash = user.getPassword();
            String enteredPasswordHash = new Md5PasswordEncoder().encodePassword(passwordString, null);

            if (!StringUtils.equals(newPassword, confirmPasswordString)) {
                throw new ValidatorException(new FacesMessage("Password missmatch"));
            } else {
                isPasswordValid = true;
            }
            if (StringUtils.isEmpty(passwordString)) {
                throw new ValidatorException(new FacesMessage("Enter your password"));
            }
            if (!userPasswordHash.equals(enteredPasswordHash)) {
                throw new ValidatorException(new FacesMessage("Incorect password!"));
            }
        }
    }

    public void firstNameListener(ValueChangeEvent e) {
        UIInput uiInput = (UIInput) e.getComponent();
        setUserFirstName(uiInput.getValue().toString());
    }

    public void lastNameListener(ValueChangeEvent e) {
        UIInput uiInput = (UIInput) e.getComponent();
        setUserLastName(uiInput.getValue().toString());
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public org.openl.rules.security.User getSimpleUser() {
        return simpleUser;
    }

    public void setSimpleUser(org.openl.rules.security.User simpleUser) {
        this.simpleUser = simpleUser;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

}