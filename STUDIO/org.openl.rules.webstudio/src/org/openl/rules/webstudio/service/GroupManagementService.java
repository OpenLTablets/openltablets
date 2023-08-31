package org.openl.rules.webstudio.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openl.rules.security.standalone.dao.GroupDao;
import org.openl.rules.security.standalone.persistence.Group;
import org.openl.security.acl.JdbcMutableAclService;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Andrei Astrouski
 */
public class GroupManagementService {

    private final GroupDao groupDao;
    private final JdbcMutableAclService aclService;

    public GroupManagementService(GroupDao groupDao, JdbcMutableAclService aclService) {
        this.groupDao = groupDao;
        this.aclService = aclService;
    }

    public List<org.openl.rules.security.Group> getGroups() {
        List<Group> groups = groupDao.getAllGroups();
        List<org.openl.rules.security.Group> resultGroups = new ArrayList<>();

        for (Group group : groups) {
            resultGroups.add(PrivilegesEvaluator.wrap(group));
        }

        return resultGroups;
    }

    public org.openl.rules.security.Group getGroupByName(String name) {
        Group group = groupDao.getGroupByName(name);
        if (group != null) {
            return PrivilegesEvaluator.wrap(group);
        }
        return null;
    }

    public boolean isGroupExist(String name) {
        return groupDao.getGroupByName(name) != null;
    }

    public void addGroup(String name, String description) {
        Group persistGroup = new Group();
        persistGroup.setName(name);
        persistGroup.setDescription(description);
        groupDao.save(persistGroup);
    }

    @Transactional
    public void updateGroup(String name, String newName, String description) {
        Group persistGroup = groupDao.getGroupByName(name);
        persistGroup.setName(newName);
        persistGroup.setDescription(description);
        groupDao.update(persistGroup);
        aclService.updateSid(new GrantedAuthoritySid(name), newName);
    }

    public void updateGroup(String name, Set<String> groups, Set<String> privileges) {
        Group persistGroup = groupDao.getGroupByName(name);

        Set<Group> includedGroups = new HashSet<>();
        if (groups != null) {
            for (String group : groups) {
                Group includedGroup = groupDao.getGroupByName(group);
                if (!persistGroup.equals(includedGroup)) {
                    // Persisting group should not include itself
                    includedGroups.add(includedGroup);
                }
            }
        }

        persistGroup.setIncludedGroups(!includedGroups.isEmpty() ? includedGroups : null);
        persistGroup.setPrivileges(privileges);

        groupDao.update(persistGroup);
    }

    public void deleteGroup(Long id) {
        groupDao.deleteGroupById(id);
    }
}
