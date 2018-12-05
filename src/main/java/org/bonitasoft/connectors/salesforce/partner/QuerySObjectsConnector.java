/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.salesforce.partner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;

/**
 * @author Charles Souillard, Haris Subasic
 * 
 */
public class QuerySObjectsConnector extends SalesforceConnector {

    // input parameters
    private static final String QUERY = "query";
    // output parameters
    private static final String QUERY_RESULT = "queryResults";
    private static final String S_OBJECTS = "sObjects";

    public QuerySObjectsConnector() {
    }

    @Override
    protected List<String> validateExtraValues() {
        final List<String> errors = new ArrayList<String>(1);
        final String query = (String) getInputParameter(QUERY);
        if (query == null || query.length() == 0) {
            errors.add("Query cannot be empty or null");
        }
        return errors;
    }

    @Override
    protected void executeFunction(final PartnerConnection connection)
            throws Exception {
        final String query = String.valueOf(getInputParameter(QUERY));
        final QueryResult queryResult = connection.query(query);
        setOutputParameter(QUERY_RESULT, queryResult);
        final List<SObject> sObjects = Arrays.asList(queryResult.getRecords());
        setOutputParameter(S_OBJECTS, sObjects);
    }
}