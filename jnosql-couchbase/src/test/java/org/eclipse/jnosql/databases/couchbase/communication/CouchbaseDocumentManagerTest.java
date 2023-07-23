/*
 *  Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.jnosql.databases.couchbase.communication;

import com.couchbase.client.core.error.DocumentNotFoundException;
import com.couchbase.client.java.json.JsonObject;
import org.assertj.core.api.Assertions;
import org.eclipse.jnosql.communication.Settings;
import org.eclipse.jnosql.communication.TypeReference;
import org.eclipse.jnosql.communication.document.Document;
import org.eclipse.jnosql.communication.document.DocumentDeleteQuery;
import org.eclipse.jnosql.communication.document.DocumentEntity;
import org.eclipse.jnosql.communication.document.DocumentQuery;
import org.eclipse.jnosql.communication.document.Documents;
import org.eclipse.jnosql.communication.keyvalue.BucketManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.eclipse.jnosql.communication.document.DocumentDeleteQuery.delete;
import static org.eclipse.jnosql.communication.document.DocumentQuery.select;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.MATCHES;
import static org.eclipse.jnosql.communication.driver.IntegrationTest.NAMED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@EnabledIfSystemProperty(named = NAMED, matches = MATCHES)
public class CouchbaseDocumentManagerTest {

    public static final String COLLECTION_PERSON_NAME = "person";
    public static final String COLLECTION_APP_NAME = "AppointmentBook";
    private static Settings settings;
    private static CouchbaseDocumentManager entityManager;
    private static BucketManager keyValueEntityManagerForPerson;
    private static BucketManager keyValueEntityManagerForAppointmentBook;

    static {
        settings = Database.INSTANCE.getSettings();
        CouchbaseDocumentConfiguration configuration = Database.INSTANCE.getDocumentConfiguration();
        CouchbaseDocumentManagerFactory managerFactory = configuration.apply(settings);
        entityManager = managerFactory.apply(CouchbaseUtil.BUCKET_NAME);

        CouchbaseKeyValueConfiguration keyValueConfiguration = Database.INSTANCE.getKeyValueConfiguration();
        CouchbaseBucketManagerFactory keyValueEntityManagerFactory = keyValueConfiguration.apply(settings);
        keyValueEntityManagerForPerson = keyValueEntityManagerFactory
                .getBucketManager(CouchbaseUtil.BUCKET_NAME, COLLECTION_PERSON_NAME);
        keyValueEntityManagerForAppointmentBook = keyValueEntityManagerFactory
                .getBucketManager(CouchbaseUtil.BUCKET_NAME, COLLECTION_APP_NAME);
    }

    @BeforeEach
    @AfterEach
    public void cleanUpData() throws InterruptedException {
        try {
            keyValueEntityManagerForPerson.delete("id");
        } catch (DocumentNotFoundException exp) {
            //IGNORE
        }
        try {
            keyValueEntityManagerForAppointmentBook.delete("ids");
        } catch (DocumentNotFoundException exp) {
            //IGNORE
        }
        Thread.sleep(1_000L);
    }

    @Test
    public void shouldInsert() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        assertEquals(entity, documentEntity);
    }

    @Test
    public void shouldInsertWithKey() {
        DocumentEntity entity = getEntity();
        entity.add("_key", "anyvalue");
        DocumentEntity documentEntity = entityManager.insert(entity);
        assertEquals(entity, documentEntity);
    }


    @Test
    public void shouldUpdate() {
        DocumentEntity entity = getEntity();
        DocumentEntity documentEntity = entityManager.insert(entity);
        Document newField = Documents.of("newField", "10");
        entity.add(newField);
        DocumentEntity updated = entityManager.update(entity);
        assertEquals(newField, updated.find("newField").get());
    }

    @Test
    public void shouldRemoveEntityByName() {
        DocumentEntity documentEntity = entityManager.insert(getEntity());

        Document name = documentEntity.find("name").get();
        DocumentQuery query = select().from(COLLECTION_PERSON_NAME).where(name.name()).eq(name.get()).build();
        DocumentDeleteQuery deleteQuery = delete().from(COLLECTION_PERSON_NAME)
                .where(name.name()).eq(name.get()).build();
        entityManager.delete(deleteQuery);
        assertTrue(entityManager.select(query).collect(Collectors.toList()).isEmpty());
    }

    @Test
    public void shouldSaveSubDocument() {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", Document.of("mobile", "1231231")));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Document id = entitySaved.find("_id").get();
        DocumentQuery query = select().from(COLLECTION_PERSON_NAME).where(id.name()).eq(id.get()).build();
        DocumentEntity entityFound = entityManager.select(query).collect(Collectors.toList()).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<>() {
        });
        assertThat(documents).contains(Document.of("mobile", "1231231"));
    }

    @Test
    public void shouldSaveSubDocument2() throws InterruptedException {
        DocumentEntity entity = getEntity();
        entity.add(Document.of("phones", asList(Document.of("mobile", "1231231"), Document.of("mobile2", "1231231"))));
        DocumentEntity entitySaved = entityManager.insert(entity);
        Thread.sleep(1_00L);
        Document id = entitySaved.find("_id").get();
        DocumentQuery query = select().from(COLLECTION_PERSON_NAME).where(id.name()).eq(id.get()).build();
        DocumentEntity entityFound = entityManager.select(query).collect(Collectors.toList()).get(0);
        Document subDocument = entityFound.find("phones").get();
        List<Document> documents = subDocument.get(new TypeReference<>() {
        });
        assertThat(documents).contains(Document.of("mobile", "1231231"),
                Document.of("mobile2", "1231231"));
    }

    @Test
    public void shouldSaveSetDocument() throws InterruptedException {
        Set<String> set = new HashSet<>();
        set.add("Acarajé");
        set.add("Munguzá");
        DocumentEntity entity = DocumentEntity.of(COLLECTION_PERSON_NAME);
        entity.add(Document.of("_id", "id"));
        entity.add(Document.of("foods", set));
        entityManager.insert(entity);
        Document id = entity.find("_id").get();
        Thread.sleep(1_000L);
        DocumentQuery query = select().from(COLLECTION_PERSON_NAME).where(id.name()).eq(id.get()).build();
        DocumentEntity entityFound = entityManager.singleResult(query).get();
        Optional<Document> foods = entityFound.find("foods");
        Set<String> setFoods = foods.get().get(new TypeReference<>() {
        });
        assertEquals(set, setFoods);
    }

    @Test
    public void shouldConvertFromListDocumentList() {
        DocumentEntity entity = createSubdocumentList();
        entityManager.insert(entity);

    }

    @Test
    public void shouldRetrieveListDocumentList() {
        DocumentEntity entity = entityManager.insert(createSubdocumentList());
        Document key = entity.find("_id").get();
        DocumentQuery query = select().from(COLLECTION_APP_NAME).where(key.name()).eq(key.get()).build();

        DocumentEntity documentEntity = entityManager.singleResult(query).get();
        assertNotNull(documentEntity);

        List<List<Document>> contacts = (List<List<Document>>) documentEntity.find("contacts").get().get();

        assertEquals(3, contacts.size());
        assertTrue(contacts.stream().allMatch(d -> d.size() == 3));
    }

    private DocumentEntity createSubdocumentList() {
        DocumentEntity entity = DocumentEntity.of(COLLECTION_APP_NAME);
        entity.add(Document.of("_id", "ids"));
        List<List<Document>> documents = new ArrayList<>();

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.EMAIL),
                Document.of("information", "ada@lovelace.com")));

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.MOBILE),
                Document.of("information", "11 1231231 123")));

        documents.add(asList(Document.of("name", "Ada"), Document.of("type", ContactType.PHONE),
                Document.of("information", "phone")));

        entity.add(Document.of("contacts", documents));
        return entity;
    }


    @Test
    public void shouldRunN1Ql() {
        DocumentEntity entity = getEntity();
        entityManager.insert(entity);
        await().until(() ->
                !entityManager
                        .n1qlQuery("select * from `jnosql`._default.person")
                        .collect(Collectors.toList()).isEmpty()
        );
    }

    @Test
    public void shouldRunN1QlParameters() {
        DocumentEntity entity = getEntity();
        entityManager.insert(entity);

        JsonObject params = JsonObject.create().put("name", entity.find("name", String.class).orElse(null));

        await().until(() ->
                !entityManager
                        .n1qlQuery("select * from `jnosql`._default.person where name = $name", params)
                        .collect(Collectors.toList()).isEmpty()
        );
    }

    @Test
    public void shouldCreateLimitOrderQuery(){
        DocumentEntity entity = getEntity();
        entity.add("_id", "id2");
        entityManager.insert(entity);
        entityManager.insert(getEntity());

        DocumentQuery query = DocumentQuery.select()
                .from(COLLECTION_PERSON_NAME).where("name")
                .eq("Poliana").and("city").eq("Salvador").limit(1).skip(1).build();
        List<DocumentEntity> select = entityManager.select(query).toList();
        Assertions.assertThat(select).flatMap(d -> d.find("name").stream().toList())
                .containsOnly(Document.of("name", "Poliana"));
    }

    private DocumentEntity getEntity() {
        DocumentEntity entity = DocumentEntity.of(COLLECTION_PERSON_NAME);
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Poliana");
        map.put("city", "Salvador");
        map.put("_id", "id");

        List<Document> documents = Documents.of(map);
        documents.forEach(entity::add);
        return entity;
    }

}