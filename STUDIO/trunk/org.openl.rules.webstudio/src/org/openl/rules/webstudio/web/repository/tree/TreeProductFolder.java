package org.openl.rules.webstudio.web.repository.tree;

import org.openl.rules.project.abstraction.AProjectArtefact;
import org.openl.rules.webstudio.web.repository.UiConst;
import org.openl.util.filter.IFilter;

public class TreeProductFolder extends TreeFolder {
    private IFilter<AProjectArtefact> filter;

    public TreeProductFolder(String id, String name, IFilter<AProjectArtefact> filter) {
        super(id, name, filter);
        this.filter = filter;
    }

    @Override
    public void addChild(AProjectArtefact childArtefact){
        String name = childArtefact.getName();
        String id = String.valueOf(name.hashCode());
        if (childArtefact.isFolder()) {
            TreeProductFolder treeFolder = new TreeProductFolder(id, name, filter);
            treeFolder.setData(childArtefact);
            add(treeFolder);
        } else {
            TreeProductFile treeFile = new TreeProductFile(id, name);
            treeFile.setData(childArtefact);
            add(treeFile);
        }
    }
    
    public String getType() {
        return UiConst.TYPE_PRODUCTION_FOLDER;
    }

}
