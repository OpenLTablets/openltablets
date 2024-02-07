package org.openl.rules.repository.api;

import java.util.Iterator;
import java.util.function.Function;

public class ConflictResolveData implements AdditionalData<ConflictResolveData> {
    private final String commitToMerge;
    private final Iterable<FileItem> resolvedFiles;
    private final String mergeMessage;

    public ConflictResolveData(String commitToMerge, Iterable<FileItem> resolvedFiles, String mergeMessage) {
        this.commitToMerge = commitToMerge;
        this.resolvedFiles = resolvedFiles;
        this.mergeMessage = mergeMessage;
    }

    public String getCommitToMerge() {
        return commitToMerge;
    }

    public Iterable<FileItem> getResolvedFiles() {
        return resolvedFiles;
    }

    public String getMergeMessage() {
        return mergeMessage;
    }

    @Override
    public ConflictResolveData convertPaths(final Function<String, String> converter) {
        Iterable<FileItem> convertedFolders = () -> new Iterator<FileItem>() {
            private final Iterator<FileItem> delegate = resolvedFiles.iterator();

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public FileItem next() {
                FileItem oldPath = delegate.next();
                FileData data = oldPath.getData();
                data.setName(converter.apply(oldPath.getData().getName()));
                return new FileItem(data, oldPath.getStream());
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported");
            }
        };
        return new ConflictResolveData(commitToMerge, convertedFolders, mergeMessage);
    }
}
