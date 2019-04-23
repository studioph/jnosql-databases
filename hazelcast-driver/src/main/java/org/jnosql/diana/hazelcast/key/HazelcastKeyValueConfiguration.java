/*
 *  Copyright (c) 2017 Otávio Santana and others
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
package org.jnosql.diana.hazelcast.key;


import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.jnosql.diana.api.Configurations;
import org.jnosql.diana.api.Settings;
import org.jnosql.diana.api.SettingsBuilder;
import org.jnosql.diana.api.key.KeyValueConfiguration;
import org.jnosql.diana.driver.ConfigurationReader;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * The hazelcast implementation of {@link KeyValueConfiguration} that returns
 * {@link HazelcastBucketManagerFactory}. It tries to read the diana-hazelcast.properties file
 * that has the properties:
 * <p>hazelcast.instanceName: the instance name</p>
 * <p>hazelcast.host: as prefix to n host where n is the number of host, eg: hazelcast-host-1: host </p>
 *
 */
public class HazelcastKeyValueConfiguration implements KeyValueConfiguration<HazelcastBucketManagerFactory> {

    private static final String HAZELCAST_FILE_CONFIGURATION = "diana-hazelcast.properties";
    private static final String DEFAULT_INSTANCE = "hazelcast-instanceName";


    /**
     * Creates a {@link HazelcastBucketManagerFactory} from configuration map
     * @param configurations the configuration map
     * @return the HazelCastBucketManagerFactory instance
     * @throws NullPointerException when configurations is null
     */
    public HazelcastBucketManagerFactory get(Map<String, String> configurations) throws NullPointerException {
        Objects.requireNonNull(configurations, "configurations is required");
        SettingsBuilder builder = Settings.builder();
        configurations.entrySet().forEach(e -> builder.put(e.getKey(), e.getValue()));
        return get(builder.build());
    }

    /**
     * Creates a {@link HazelcastBucketManagerFactory} from hazelcast config
     * @param config the {@link Config}
     * @return the HazelCastBucketManagerFactory instance
     * @throws NullPointerException when config is null
     */
    public HazelcastBucketManagerFactory get(Config config)throws NullPointerException {
        requireNonNull(config, "config is required");
        HazelcastInstance hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);
        return new DefaultHazelcastBucketManagerFactory(hazelcastInstance);
    }

    @Override
    public HazelcastBucketManagerFactory get() {
        Map<String, String> configuration = ConfigurationReader.from(HAZELCAST_FILE_CONFIGURATION);
        return get(configuration);
    }

    @Override
    public HazelcastBucketManagerFactory get(Settings settings) {
        requireNonNull(settings, "settings is required");

        List<String> servers = settings.prefix(Arrays.asList(OldHazelcastConfigurations.HOST.get(),
                HazelcastConfigurations.HOST.get(), Configurations.HOST.get()))
                .stream().map(Object::toString)
                .collect(Collectors.toList());
        String instance = settings.get(Arrays.asList(OldHazelcastConfigurations.INSTANCE.get(),
                HazelcastConfigurations.INSTANCE.get())).map(Object::toString)
                .orElse(DEFAULT_INSTANCE);
        Config config = new Config(instance);

        NetworkConfig network = config.getNetworkConfig();

        settings.get(HazelcastConfigurations.PORT.get())
                .map(Object::toString)
                .map(Integer::parseInt)
                .ifPresent(network::setPort);

        settings.get(HazelcastConfigurations.PORT_COUNT.get())
                .map(Object::toString)
                .map(Integer::parseInt)
                .ifPresent(network::setPortCount);

        settings.get(HazelcastConfigurations.PORT_AUTO_INCREMENT.get())
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .ifPresent(network::setPortAutoIncrement);

        JoinConfig join = network.getJoin();

        settings.get(HazelcastConfigurations.MULTICAST_ENABLE.get())
                .map(Object::toString)
                .map(Boolean::parseBoolean)
                .ifPresent(join.getMulticastConfig()::setEnabled);

        servers.forEach(join.getTcpIpConfig()::addMember);

        join.getTcpIpConfig()
                .addMember("machine1")
                .addMember("localhost");

        settings.get(HazelcastConfigurations.TCP_IP_JOIN.get())
                .map(Object::toString)
                .map(Boolean::valueOf)
                .ifPresent(join.getTcpIpConfig()::setEnabled);

        HazelcastInstance hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);
        return new DefaultHazelcastBucketManagerFactory(hazelcastInstance);
    }
}
