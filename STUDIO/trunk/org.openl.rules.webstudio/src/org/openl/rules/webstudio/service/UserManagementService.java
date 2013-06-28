package org.openl.rules.webstudio.service;

import org.openl.rules.security.DefaultPrivileges;
import org.openl.rules.security.SimpleUser;
import org.openl.rules.security.standalone.dao.GroupDao;
import org.openl.rules.security.standalone.persistence.Group;
import org.openl.rules.security.standalone.persistence.User;
import org.openl.rules.security.standalone.service.UserInfoUserDetailsServiceImpl;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrei Astrouski
 */
public class UserManagementService extends UserInfoUserDetailsServiceImpl {

    private GroupDao groupDao;

    public List<org.openl.rules.security.User> getAllUsers() {
        List<User> users = userDao.getAll();
        List<org.openl.rules.security.User> resultUsers = new ArrayList<org.openl.rules.security.User>();
        for (User user : users) {
            org.openl.rules.security.User resultUser = new SimpleUser(user.getFirstName(), user.getSurname(),
                    user.getLoginName(), user.getPasswordHash(), createPrivileges(user));
            resultUsers.add(resultUser);
        }
        return resultUsers;
    }

    public List<org.openl.rules.security.User> getUsersByPrivilege(String privilege) {
        List<User> users = userDao.getAll();
        List<org.openl.rules.security.User> resultUsers = new ArrayList<org.openl.rules.security.User>();
        for (User user : users) {
            org.openl.rules.security.User resultUser = new SimpleUser(user.getFirstName(), user.getSurname(),
                    user.getLoginName(), user.getPasswordHash(), createPrivileges(user));
            if (resultUser.hasPrivilege(DefaultPrivileges.PRIVILEGE_ALL.name())
                    || resultUser.hasPrivilege(privilege)) {
                resultUsers.add(resultUser);
            }
        }
        return resultUsers;
    }

    public void addUser(org.openl.rules.security.User user) {
        User persistUser = new User();
        persistUser.setLoginName(user.getUsername());
        persistUser.setPasswordHash(
                new Md5PasswordEncoder().encodePassword(user.getPassword(), null));
        persistUser.setFirstName(user.getFirstName());
        persistUser.setSurname(user.getLastName());

        Set<Group> groups = new HashSet<Group>();
        for (GrantedAuthority auth : user.getAuthorities()) {
            groups.add(groupDao.getGroupByName(auth.getAuthority()));
        }
        persistUser.setGroups(groups);

        userDao.save(persistUser);
    }

    public void updateUser(org.openl.rules.security.User user) {
        User persistUser = userDao.getUserByName(user.getUsername());

        persistUser.setFirstName(user.getFirstName());
        persistUser.setSurname(user.getLastName());

        Set<Group> groups = new HashSet<Group>();
        for (GrantedAuthority auth : user.getAuthorities()) {
            groups.add(groupDao.getGroupByName(auth.getAuthority()));
        }
        persistUser.setGroups(groups);
        if (user.getPassword() != null) {
            persistUser.setPasswordHash(new Md5PasswordEncoder().encodePassword(user.getPassword(), null));
        }

        userDao.update(persistUser);
    }

    public void deleteUser(String username) {
        userDao.delete(userDao.getUserByName(username));
    }

    public void setGroupDao(GroupDao groupDao) {
        this.groupDao = groupDao;
    }

    public User getUserByName(String name) {
        return userDao.getUserByName(name);
    }
}
