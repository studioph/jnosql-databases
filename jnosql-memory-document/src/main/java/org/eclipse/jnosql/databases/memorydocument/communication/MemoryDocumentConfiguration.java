package org.eclipse.jnosql.databases.memorydocument.communication;

import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.semistructured.DatabaseConfiguration;
import org.eclipse.jnosql.communication.semistructured.DatabaseManagerFactory;
import static java.util.Objects.requireNonNull;

/**
 * The in-memory implementation of {@link DatabaseConfiguration}
 * that returns  {@link}
 * @see MemoryDocumentConfigurations
 */
public class MemoryDocumentConfiguration implements DatabaseConfiguration{

    @Override
    public DatabaseManagerFactory apply(Settings settings) {
        requireNonNull(settings, "settings is required");

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'apply'");
    }

}
