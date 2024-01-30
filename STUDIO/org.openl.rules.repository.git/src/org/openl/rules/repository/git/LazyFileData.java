package org.openl.rules.repository.git;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openl.rules.repository.api.FileData;
import org.openl.rules.repository.api.UserInfo;
import org.openl.rules.repository.git.CommitMessageParser.CommitMessage;

class LazyFileData extends FileData {
    private final Logger log = LoggerFactory.getLogger(LazyFileData.class);

    private final String fullPath;
    private final GitRepository gitRepo;
    private ObjectId fromCommit;
    private RevCommit fileCommit;
    private ObjectId fileId;
    private final CommitMessageParser commitMessageParser;
    private final CommitMessageParser commitMessageParserOld;

    private boolean loaded = false;
    private boolean deleteStatusLoaded = false;

    LazyFileData(String branch,
            String fullPath,
            GitRepository gitRepo,
            ObjectId fromCommit,
            ObjectId fileId,
            CommitMessageParser commitMessageParser,
            CommitMessageParser commitMessageParserOld) {
        setBranch(branch);
        setName(fullPath);
        if (fileId != null) {
            setUniqueId(fileId.getName());
        }

        this.fullPath = fullPath;
        this.gitRepo = gitRepo;
        this.commitMessageParser = commitMessageParser;
        this.commitMessageParserOld = commitMessageParserOld;
        this.fromCommit = fromCommit;
        this.fileId = fileId;
    }

    LazyFileData(String branch,
            String fullPath,
            GitRepository gitRepo,
            RevCommit fileCommit,
            ObjectId fileId,
            CommitMessageParser commitMessageParser,
            CommitMessageParser commitMessageParserOld) {
        setBranch(branch);
        setName(fullPath);
        if (fileId != null) {
            setUniqueId(fileId.getName());
        }

        this.fullPath = fullPath;
        this.gitRepo = gitRepo;
        this.commitMessageParser = commitMessageParser;
        this.commitMessageParserOld = commitMessageParserOld;
        this.fileCommit = fileCommit;
        this.fileId = fileId;
    }

    @Override
    public long getSize() {
        if (fileId != null) {
            try (Git git = gitRepo.getClosableGit()) {
                ObjectLoader loader = git.getRepository().open(fileId);
                super.setSize(loader.getSize());
                fileId = null;
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                throw new IllegalStateException("Cannot get file size: " + e.getMessage(), e);
            }
        }

        return super.getSize();
    }

    @Override
    public void setSize(long size) {
        fileId = null;
        super.setSize(size);
    }

    @Override
    public UserInfo getAuthor() {
        verifyLoaded();
        return super.getAuthor();
    }

    @Override
    public void setAuthor(UserInfo author) {
        verifyLoaded();
        super.setAuthor(author);
    }

    @Override
    public String getComment() {
        verifyLoaded();
        return super.getComment();
    }

    @Override
    public void setComment(String comment) {
        verifyLoaded();
        super.setComment(comment);
    }

    @Override
    public Date getModifiedAt() {
        verifyLoaded();
        return super.getModifiedAt();
    }

    @Override
    public void setModifiedAt(Date modifiedAt) {
        verifyLoaded();
        super.setModifiedAt(modifiedAt);
    }

    @Override
    public String getVersion() {
        verifyLoaded();
        return super.getVersion();
    }

    @Override
    public void setVersion(String version) {
        verifyLoaded();
        super.setVersion(version);
    }

    @Override
    public boolean isDeleted() {
        verifyDeleteStatusLoaded();
        return super.isDeleted();
    }

    @Override
    public void setDeleted(boolean deleted) {
        super.setDeleted(deleted);
        deleteStatusLoaded = true;
    }

    private void verifyLoaded() {
        if (loaded) {
            return;
        }

        try (Git git = gitRepo.getClosableGit()) {
            if (fileCommit == null) {
                try {
                    fileCommit = GitRepository.findFirstCommit(git, fromCommit, fullPath);
                } catch (GitAPIException | MissingObjectException | IncorrectObjectTypeException e) {
                    log.error(e.getMessage(), e);
                }
                if (fileCommit == null) {
                    throw new IllegalStateException("Cannot find revision for the file " + fullPath);
                }

                fromCommit = null;
            }

            String fullMessage = fileCommit.getFullMessage();
            PersonIdent committerIdent = fileCommit.getCommitterIdent();

            CommitMessage commitMessage = parseCommitMessage(fullMessage);
            String message = fullMessage;
            String userDisplayName = committerIdent.getName();
            if (commitMessage != null) {
                CommitType commitType = commitMessage.getCommitType();
                if (commitType == CommitType.ARCHIVE || commitType == CommitType.ERASE) {
                    super.setDeleted(true);
                    deleteStatusLoaded = true;
                }
                if (commitMessage.getMessage() != null) {
                    message = commitMessage.getMessage();
                }
                if (commitMessage.getAuthor() != null) {
                    userDisplayName = commitMessage.getAuthor();
                }
            }

            super.setComment(message);
            super.setAuthor(new UserInfo(null, committerIdent.getEmailAddress(), userDisplayName));
            super.setModifiedAt(committerIdent.getWhen());

            String version;
            try {
                version = GitRepository.getVersionName(git.getRepository(), git.tagList().call(), fileCommit.getId());
            } catch (GitAPIException e) {
                throw new IllegalStateException("Cannot get tags list: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot get version name: " + e.getMessage(), e);
            }
            super.setVersion(version);

            loaded = true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private CommitMessage parseCommitMessage(String fullMessage) {
        CommitMessage commitMessage = null;
        if (commitMessageParserOld != null) {
            commitMessage = commitMessageParserOld.parse(fullMessage);
        }
        if (commitMessage == null && commitMessageParser != null) {
            commitMessage = commitMessageParser.parse(fullMessage);
        }
        return commitMessage;
    }

    /**
     * For non-flat folder structure this method shouldn't be invoked because in that case another algorithm is used.
     */
    private void verifyDeleteStatusLoaded() {
        verifyLoaded();
        if (deleteStatusLoaded) {
            return;
        }

        try {
            if (fileId == null && getSize() == UNDEFINED_SIZE) {
                // Deleted status for folder is got from main branch.
                if (!gitRepo.getBranch().equals(gitRepo.getBaseBranch())) {
                    FileData data = gitRepo.forBranch(gitRepo.getBaseBranch()).check(fullPath);
                    super.setDeleted(data.isDeleted());
                }
            }
            deleteStatusLoaded = true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
