/*
 *  Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

record OracleQuery(String query, Map<String, Object> params, List<String> ids) {

    @Override
    public List<String> ids() {
        return Collections.unmodifiableList(ids);
    }

    public boolean hasParameter() {
        return this.params.isEmpty();
    }

    public boolean hasOnlyIds() {
        return hasIds() && hasParameter();
    }

    public boolean hasIds() {
        return !this.ids.isEmpty();
    }

    @Override
    public String toString() {
        return "OracleQuery{" +
                "query='" + query + '\'' +
                ", params=" + params +
                ", ids=" + ids +
                '}';
    }

    static OracleQuery of(StringBuilder query, Map<String, Object> params, List<String> ids) {
        return new OracleQuery(query.toString(), params, ids);
    }
}
