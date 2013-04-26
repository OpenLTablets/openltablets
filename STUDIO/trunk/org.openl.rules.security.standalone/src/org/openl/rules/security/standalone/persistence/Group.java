package org.openl.rules.security.standalone.persistence;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;

/**
 * Group.
 *
 * @author Andrey Naumenko
 */
@Entity
@Table(name = "UserGroup")
public class Group extends PersistentObject {
    private static final long serialVersionUID = 1L;
    private String name;
    private String description;
    private String privileges;
    private Set<Group> includedGroups;
    private Set<AccessControlEntry> accessControlEntries;
    private Set<User> users;
    private Set<Group> parentGroups;

    /**
     * Description of group.
     *
     * @return description
     */
    @Column(length = 200, name = "Description")
    public String getDescription() {
        return description;
    }

    @Override
    @Id
    @GeneratedValue(
        strategy=GenerationType.AUTO)
    @Column(name = "GroupID")
    @Type(type = "java.lang.Long")
    public Long getId() {
        return super.getId();
    }

    /**
     * Included groups.
     *
     * @return
     */
    @ManyToMany(targetEntity = Group.class, fetch = FetchType.EAGER, cascade = javax.persistence.CascadeType.MERGE)
    @JoinTable(name = "Group2Group", joinColumns = { @JoinColumn(name = "GroupID") }, inverseJoinColumns = { @JoinColumn(name = "IncludedGroupID") })
    public Set<Group> getIncludedGroups() {
        return includedGroups;
    }

    /**
     * Parent groups.
     *
     * @return
     */
    @ManyToMany(targetEntity = Group.class, fetch = FetchType.LAZY, cascade = javax.persistence.CascadeType.MERGE)
    @JoinTable(name = "Group2Group", joinColumns = { @JoinColumn(name = "IncludedGroupID") }, inverseJoinColumns = { @JoinColumn(name = "GroupID") })
    public Set<Group> getParentGroups() {
        return parentGroups;
    }

    /**
     * Group name.
     *
     * @return
     */
    @Column(length = 40, name = "GroupName", unique = true, nullable = false)
    public String getName() {
        return name;
    }

    /**
     * Comma separated list of user's privileges.
     *
     * @return
     */
    @Column(name = "UserPrivileges", length = 500) // Privileges is reserved word for Oracle Data base
    public String getPrivileges() {
        return privileges;
    }

    /**
     * Group's access control entries.
     *
     * @return
     */
    @OneToMany(targetEntity = AccessControlEntry.class, mappedBy = "group", orphanRemoval = true)
    @Cascade(value = { CascadeType.ALL })
    public Set<AccessControlEntry> getAccessControlEntries() {
        return accessControlEntries;
    }

    /**
     * Users belonging to this group. Users count can be too big - we should use
     * lazy loading here
     * 
     * @return belonging to this group
     */
    @ManyToMany(targetEntity = User.class, fetch = FetchType.LAZY, cascade = javax.persistence.CascadeType.MERGE)
    @JoinTable(name = "User2Group", joinColumns = { @JoinColumn(name = "GroupID") }, inverseJoinColumns = { @JoinColumn(name = "UserID") })
    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void setAccessControlEntries(Set<AccessControlEntry> accessControlEntries) {
        this.accessControlEntries = accessControlEntries;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIncludedGroups(Set<Group> includedGroups) {
        this.includedGroups = includedGroups;
    }

    public void setParentGroups(Set<Group> parentGroups) {
        this.parentGroups = parentGroups;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrivileges(String privileges) {
        this.privileges = privileges;
    }
}
