package org.openl.rules.security.standalone.dao;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import org.openl.rules.security.standalone.persistence.Tag;

public interface TagDao extends Dao<Tag> {
    @Transactional
    Tag getById(Long id);

    @Transactional
    Tag getByName(Long tagTypeId, String name);

    @Transactional
    List<Tag> getAll();

    @Transactional
    List<Tag> getByTagType(String tagType);

    @Transactional
    Tag getByTagTypeAndName(String tagType, String tagName);

    @Transactional
    boolean deleteById(Long id);
}
