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
package org.jnosql.diana.couchbase.document;


import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.queries.MatchQuery;
import org.jnosql.diana.api.Sort;
import org.jnosql.diana.api.document.Document;
import org.jnosql.diana.api.document.DocumentCondition;
import org.jnosql.diana.api.document.DocumentEntity;
import org.jnosql.diana.api.document.DocumentQuery;
import org.jnosql.diana.api.key.BucketManager;
import org.jnosql.diana.api.key.BucketManagerFactory;
import org.jnosql.diana.couchbase.key.CouchbaseKeyValueConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class SearchQueryTest {

    public static final String COLLECTION_NAME = "person";
    private CouchbaseDocumentCollectionManager entityManager;

    {
        CouchbaseDocumentConfiguration configuration = new CouchbaseDocumentConfiguration();
        CouhbaseDocumentCollectionManagerFactory managerFactory = configuration.get();
        entityManager = managerFactory.get("default");
    }

//    @AfterClass
    public static void afterClass() {
        CouchbaseKeyValueConfiguration configuration = new CouchbaseKeyValueConfiguration();
        BucketManagerFactory keyValueEntityManagerFactory = configuration.get();
        BucketManager keyValueEntityManager = keyValueEntityManagerFactory.getBucketManager("default");
        keyValueEntityManager.remove("city:salvador");
        keyValueEntityManager.remove("city:sao_paulo");
        keyValueEntityManager.remove("city:rio_janeiro");
        keyValueEntityManager.remove("city:manaus");
    }

    @BeforeClass
    public static void beforeClass() throws InterruptedException {
        CouchbaseDocumentConfiguration configuration = new CouchbaseDocumentConfiguration();
        CouhbaseDocumentCollectionManagerFactory managerFactory = configuration.get();
        CouchbaseDocumentCollectionManager entityManager = managerFactory.get("default");

        DocumentEntity salvador = DocumentEntity.of("city", asList(Document.of("_id", "salvador")
                , Document.of("name", "Salvador"),
                Document.of("description", "Founded by the Portuguese in 1549 as the first capital" +
                        " of Brazil, Salvador is" +
                        " one of the oldest colonial cities in the Americas.")));
        DocumentEntity saoPaulo = DocumentEntity.of("city", asList(Document.of("_id", "sao_paulo")
                , Document.of("name", "São Paulo"), Document.of("description", "São Paulo, Brazil’s vibrant " +
                        "financial center, is among the world's most populous cities, with numerous cultural institutions" +
                        " and a rich architectural tradition. ")));
        DocumentEntity rioJaneiro = DocumentEntity.of("city", asList(Document.of("_id", "rio_janeiro")
                , Document.of("name", "Rio de Janeiro"), Document.of("description", "Rio de Janeiro " +
                        "is a huge seaside city in Brazil, famed for its Copacabana" +
                        " and Ipanema beaches, 38m Christ the Redeemer statue atop Mount" +
                        " Corcovado and for Sugarloaf Mountain, a granite peak with cable" +
                        " cars to its summit. ")));
        DocumentEntity manaus = DocumentEntity.of("city", asList(Document.of("_id", "manaus")
                , Document.of("name", "Manaus"), Document.of("description", "Manaus, on the banks " +
                        "of the Negro River in northwestern Brazil, is the capital of the vast state of Amazonas.")));

        entityManager.insert(Arrays.asList(salvador, saoPaulo, rioJaneiro, manaus));
        Thread.sleep(2_000L);

    }


    @Test
//    @Ignore
    public void shouldSearchElement() {
        MatchQuery fts = SearchQuery.match("Financial");
        SearchQuery query = new SearchQuery("index-diana", fts);
        List<DocumentEntity> entities = entityManager.search(query);
        System.out.println(entities);
    }

}
