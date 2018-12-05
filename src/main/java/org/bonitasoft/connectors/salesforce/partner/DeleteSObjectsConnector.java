/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
 */
package org.bonitasoft.connectors.salesforce.partner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;

public class DeleteSObjectsConnector extends SalesforceConnector {

	// input parameters
	private static final String S_OBJECT_IDS = "sObjectIds";

	// output parameters
	protected DeleteResult deleteResults;

    public DeleteSObjectsConnector() {
    }

    @Override
    protected List<String> validateExtraValues() {
        final List<String> errors = new ArrayList<String>(1);
        @SuppressWarnings("unchecked")
        final List<String> sObjectIds = (List<String>) getInputParameter(S_OBJECT_IDS);
        if (sObjectIds == null || sObjectIds.size() == 0) {
            errors.add("sObjectIds cannot be null or empty");
            return errors;
        }
        for (String id : sObjectIds) {
			if (id == null || id.length() == 0){
                errors.add("An id of sObject to delete is null or empty");
                return errors;
            }
        }
        return errors;
    }

    @Override
    protected final void executeFunction(final PartnerConnection connection) throws Exception {
        @SuppressWarnings("unchecked")
        final List<String> sObjectIds = (List<String>) getInputParameter(S_OBJECT_IDS);
        String[] ids = sObjectIds.toArray(new String[sObjectIds.size()]);
        List<DeleteResult> deleteResults = Arrays.asList(connection.delete(ids));
        setOutputParameter("deleteResults", deleteResults);
    }

}
