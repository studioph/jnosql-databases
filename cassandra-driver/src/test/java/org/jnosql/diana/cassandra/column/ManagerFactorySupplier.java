/*
 *  Copyright (c) 2019 Otávio Santana and others
 *   All rights reserved. This program and the accompanying materials
 *   are made available under the terms of the Eclipse Public License v1.0
 *   and Apache License v2.0 which accompanies this distribution.
 *   The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *   and the Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php.
 *
 *   You may elect to redistribute this code under either of these licenses.
 *
 *   Contributors:
 *
 *   Otavio Santana
 */

package org.jnosql.diana.cassandra.column;

import org.jnosql.diana.api.Settings;
import org.jnosql.diana.driver.ConfigurationReader;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.jnosql.diana.cassandra.column.CassandraConfiguration.CASSANDRA_FILE_CONFIGURATION;

public enum ManagerFactorySupplier implements Supplier<CassandraColumnFamilyManagerFactory> {

    INSTANCE;

    private final GenericContainer cassandra =
            new GenericContainer("cassandra:latest")
                    .withExposedPorts(9042)
                    .waitingFor(Wait.defaultWaitStrategy());

    {
        cassandra.start();
    }

    @Override
    public CassandraColumnFamilyManagerFactory get() {
        Settings settings = getSettings();
        CassandraConfiguration cassandraConfiguration = new CassandraConfiguration();
        return cassandraConfiguration.get(settings);
    }

    Settings getSettings() {
        Map<String, Object> configuration = new HashMap<>(ConfigurationReader.from(CASSANDRA_FILE_CONFIGURATION));
        configuration.put("cassandra.host-1", cassandra.getContainerIpAddress());
        configuration.put("cassandra.port", cassandra.getFirstMappedPort());
        return Settings.of(configuration);
    }
}
