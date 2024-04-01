package org.eclipse.jnosql.databases.memorydocument.communication;

import java.util.function.Supplier;

/**
 * An enumeration to show the available options for the in-memory database.
 * It implements {@link Supplier}, where its it returns the property name that might be
 * overwritten by the system environment using Eclipse Microprofile or Jakarta Config API.
 *
 * @see org.eclipse.jnosql.communication.Settings
 */
public enum MemoryDocumentConfigurations implements Supplier<String> {
    /**
     * Whether to enable the in-memory database
     */
    ENABLED("jnosql.memory.document.enabled");

    private final String configuration;

    MemoryDocumentConfigurations(String configuration){
        this.configuration = configuration;
    }

    @Override
    public String get() {
        return configuration;
    }

}
