/*
 * Copyright (C) 2009 - 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.bonitasoft.connectors.salesforce.partner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

public class DeleteSObjectsConnector extends SalesforceConnector {

    // input parameters
    static final String S_OBJECT_IDS = "sObjectIds";
    //output parameter
    static final String DELETE_RESULTS_OUTPUT = "deleteResults";

    @Override
    protected List<String> validateExtraValues() {
        final List<String> errors = new ArrayList<>();
        @SuppressWarnings("unchecked")
        final List<String> sObjectIds = (List<String>) getInputParameter(S_OBJECT_IDS);
        if (sObjectIds == null || sObjectIds.isEmpty()) {
            errors.add("sObjectIds cannot be null or empty");
        }else if (sObjectIds.stream().anyMatch(id -> id == null || id.isEmpty())) {
            errors.add("An id of sObject to delete is null or empty");
        }
        return errors;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    protected final void executeFunction(final PartnerConnection connection) throws ConnectionException {
        final List<String> sObjectIds = (List<String>) getInputParameter(S_OBJECT_IDS);
        String[] ids = sObjectIds.toArray(new String[sObjectIds.size()]);
        setOutputParameter(DELETE_RESULTS_OUTPUT, Arrays.asList(connection.delete(ids)));
    }

}
