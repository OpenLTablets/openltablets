package org.openl.rules.rest.model.tables;

/**
 * Decision header abstract model
 *
 * @author Vladyslav Pikus
 */
public abstract class ARuleHeaderView {

    public static final String UNKNOWN_HEADER_NAME = "col";

    public final String title;

    protected ARuleHeaderView(Builder<?> builder) {
        this.title = builder.title;
    }

    public abstract static class Builder<T extends ARuleHeaderView.Builder<T>> {

        private String title;

        protected Builder() {
        }

        protected abstract T self();

        public T title(String title) {
            this.title = title;
            return self();
        }

        public abstract ARuleHeaderView build();
    }

}
