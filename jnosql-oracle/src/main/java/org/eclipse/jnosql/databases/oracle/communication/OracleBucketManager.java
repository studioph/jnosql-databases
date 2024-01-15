/*
 *  Copyright (c) 2023 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.databases.oracle.communication;

import jakarta.json.bind.Jsonb;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.ops.DeleteRequest;
import oracle.nosql.driver.ops.DeleteResult;
import oracle.nosql.driver.ops.GetRequest;
import oracle.nosql.driver.ops.GetResult;
import oracle.nosql.driver.ops.PutRequest;
import oracle.nosql.driver.values.JsonOptions;
import oracle.nosql.driver.values.MapValue;
import org.eclipse.jnosql.communication.Value;
import org.eclipse.jnosql.communication.driver.ValueJSON;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.eclipse.jnosql.communication.keyvalue.KeyValueEntity;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class OracleBucketManager implements BucketManager {
    private final String bucketName;
    private final NoSQLHandle serviceHandle;

    private final Jsonb jsonB;

    public OracleBucketManager(String bucketName, NoSQLHandle serviceHandle, Jsonb jsonB) {
        this.bucketName = bucketName;
        this.serviceHandle = serviceHandle;
        this.jsonB = jsonB;
    }

    @Override
    public String name() {
        return bucketName;
    }

    @Override
    public <K, V> void put(K key, V value) {
        Objects.requireNonNull(key, "key is required");
        Objects.requireNonNull(value, "value is required");
        putImplementation(key, value);
    }

    @Override
    public void put(KeyValueEntity keyValueEntity) {
        Objects.requireNonNull(keyValueEntity, "keyValueEntity is required");
        putImplementation(keyValueEntity.key(), keyValueEntity.value());
    }

    @Override
    public void put(KeyValueEntity keyValueEntity, Duration duration) {
        throw new UnsupportedOperationException("Oracle NoSQL does not support TTL");
    }

    @Override
    public void put(Iterable<KeyValueEntity> iterable) {
        Objects.requireNonNull(iterable, "iterable is required");
        iterable.forEach(this::put);
    }

    @Override
    public void put(Iterable<KeyValueEntity> iterable, Duration duration) {
        throw new UnsupportedOperationException("Oracle NoSQL does not support TTL");
    }

    @Override
    public <K> Optional<Value> get(K key) {
        Objects.requireNonNull(key, "key is required");
        GetRequest getRequest = new GetRequest();
        getRequest.setKey(new MapValue().put("id", key.toString()));
        getRequest.setTableName(name());
        GetResult getResult = serviceHandle.get(getRequest);
        if (getResult != null) {
            String json = getResult.getValue().toJson(new JsonOptions());
            return Optional.of(ValueJSON.of(json));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public <K> Iterable<Value> get(Iterable<K> keys) {
        Objects.requireNonNull(keys, "keys is required");
        return StreamSupport.stream(keys.spliterator(), false)
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public <K> void delete(K key) {
        Objects.requireNonNull(key, "key is required");
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey(new MapValue().put("id", key.toString()));
        deleteRequest.setTableName(name());
        serviceHandle.delete(deleteRequest);
    }

    @Override
    public <K> void delete(Iterable<K> keys) {
        Objects.requireNonNull(keys, "keys is required");
        keys.forEach(this::delete);
    }

    @Override
    public void close() {

    }

    private <K, V> void putImplementation(K key, V value) {
        MapValue mapValue = new MapValue().put("id", key.toString());
        MapValue contentVal = mapValue.putFromJson("content", jsonB.toJson(value),
                new JsonOptions());
        PutRequest putRequest = new PutRequest()
                .setValue(contentVal)
                .setTableName(name());
        serviceHandle.put(putRequest);
    }
}
