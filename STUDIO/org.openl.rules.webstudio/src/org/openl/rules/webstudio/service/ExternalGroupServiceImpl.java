package org.openl.rules.webstudio.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import org.openl.rules.security.Group;
import org.openl.rules.security.SimpleGroup;
import org.openl.rules.security.standalone.dao.ExternalGroupDao;
import org.openl.rules.security.standalone.persistence.ExternalGroup;

/**
 * External groups service implementation
 *
 * @author Vladyslav Pikus
 */
public class ExternalGroupServiceImpl implements ExternalGroupService {

    private final ExternalGroupDao externalGroupDao;

    public ExternalGroupServiceImpl(ExternalGroupDao externalGroupDao) {
        this.externalGroupDao = externalGroupDao;
    }

    @Override
    @Transactional
    public void deleteAll() {
        externalGroupDao.deleteAll();
    }

    @Override
    @Transactional
    public void mergeAllForUser(String loginName, Collection<? extends GrantedAuthority> externalGroups) {
        externalGroupDao.deleteAllForUser(loginName);
        externalGroupDao.save(new BatchCreateExternalGroupCursor(loginName, externalGroups));
    }

    @Override
    @Transactional
    public List<Group> findAllForUser(String loginName) {
        return externalGroupDao.findAllForUser(loginName)
                .stream()
                .map(ext -> new SimpleGroup(ext.getGroupName(), ext.getGroupName(), Collections.emptySet()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public long countAllForUser(String loginName) {
        return externalGroupDao.countAllForUser(loginName);
    }

    @Override
    @Transactional
    public List<Group> findMatchedForUser(String loginName) {
        return externalGroupDao.findMatchedForUser(loginName)
                .stream()
                .map(PrivilegesEvaluator::wrap)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public long countMatchedForUser(String loginName) {
        return externalGroupDao.countMatchedForUser(loginName);
    }

    @Override
    @Transactional
    public List<Group> findNotMatchedForUser(String loginName) {
        return externalGroupDao.findNotMatchedForUser(loginName)
                .stream()
                .map(ext -> new SimpleGroup(ext.getGroupName(), ext.getGroupName(), Collections.emptySet()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public long countNotMatchedForUser(String loginName) {
        return externalGroupDao.countNotMatchedForUser(loginName);
    }

    @Override
    @Transactional
    public List<Group> findAllByName(String groupName, int limit) {
        return externalGroupDao.findAllByName(groupName, limit)
                .stream()
                .map(ext -> new SimpleGroup(ext, ext, Collections.emptySet()))
                .collect(Collectors.toList());
    }

    private static class BatchCreateExternalGroupCursor implements Iterable<ExternalGroup> {

        private final String loginName;
        private final Collection<? extends GrantedAuthority> externalGroups;

        public BatchCreateExternalGroupCursor(String loginName, Collection<? extends GrantedAuthority> externalGroups) {
            this.externalGroups = externalGroups;
            this.loginName = loginName;
        }

        @Override
        public Iterator<ExternalGroup> iterator() {
            final Iterator<? extends GrantedAuthority> it = externalGroups.iterator();
            return new Iterator<ExternalGroup>() {

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public ExternalGroup next() {
                    GrantedAuthority group = it.next();
                    ExternalGroup externalGroup = new ExternalGroup();
                    externalGroup.setLoginName(loginName);
                    externalGroup.setGroupName(group.getAuthority());
                    return externalGroup;
                }
            };
        }

    }

}
