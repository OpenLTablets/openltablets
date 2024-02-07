package org.openl.rules.rest.model;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Parameter;

public class ProjectViewModel {

    @Parameter(description = "Project Name", required = true)
    @JsonView(GenericView.Full.class)
    public final String name;

    @Parameter(description = "Author of latest update", required = true)
    @JsonView(GenericView.Full.class)
    public final String modifiedBy;

    @Parameter(description = "Date and time of latest update", required = true)
    @JsonView(GenericView.Full.class)
    public final ZonedDateTime modifiedAt;

    @Parameter(description = "Branch Name. Can be absent if current repository doesn't support branches")
    @JsonView({GenericView.CreateOrUpdate.class, GenericView.Full.class})
    public final String branch;

    @Parameter(description = "Revision ID", required = true)
    @JsonView({GenericView.CreateOrUpdate.class, GenericView.Full.class})
    public final String rev;

    @Parameter(description = "Project path in target repository. Can be absent if Design Repository is flat")
    @JsonView(GenericView.Full.class)
    public final String path;

    private ProjectViewModel(Builder from) {
        this.name = from.name;
        this.modifiedBy = from.modifiedBy;
        this.modifiedAt = from.modifiedAt;
        this.branch = from.branch;
        this.rev = from.rev;
        this.path = from.path;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String modifiedBy;
        private ZonedDateTime modifiedAt;
        private String branch;
        private String rev;
        private String path;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder modifiedBy(String modifiedBy) {
            this.modifiedBy = modifiedBy;
            return this;
        }

        public Builder modifiedAt(ZonedDateTime modifiedAt) {
            this.modifiedAt = modifiedAt;
            return this;
        }

        public Builder branch(String branch) {
            this.branch = branch;
            return this;
        }

        public Builder rev(String rev) {
            this.rev = rev;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public ProjectViewModel build() {
            return new ProjectViewModel(this);
        }
    }
}
