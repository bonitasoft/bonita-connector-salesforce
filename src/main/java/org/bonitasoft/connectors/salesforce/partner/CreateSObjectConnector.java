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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;

/**
 * @author Charles Souillard, Haris Subasic
 */
public class CreateSObjectConnector extends SalesforceConnector {

    // input parameters
    static final String S_OBJECT_TYPE = "sObjectType";
    static final String FIELD_VALUES = "fieldValues";

    @Override
    protected final void executeFunction(final PartnerConnection connection)
            throws ConnectionException {
        final SObject sObject = new SObject();
        sObject.setType((String) getInputParameter(S_OBJECT_TYPE));
        @SuppressWarnings("unchecked")
        final List<List<Object>> parametersList = (List<List<Object>>) getInputParameter(FIELD_VALUES);
        for (final List<Object> rows : parametersList) {
            addSObjectField(sObject, rows);
        }

        final SaveResult[] sResults = connection.create(new SObject[] { sObject });
        if (sResults != null && sResults.length > 0) {
            setOutputParameter(S_OBJECT_ID_OUTPUT, sResults[0].getId());
            setOutputParameter(SAVE_RESULT_OUTPUT, sResults[0]);
        }
    }

    private void addSObjectField(final SObject sObject, final List<Object> rows) {
        final Object keyContent = rows.get(0);
        final Object valueContent = rows.get(1);
        if (keyContent != null && valueContent != null) {
            sObject.setField(keyContent.toString(),
                    valueContent instanceof Serializable ? valueContent : valueContent.toString());
        }
    }

    @Override
    protected List<String> validateExtraValues() {
        final List<String> errors = new ArrayList<>();
        final String objType = ((String) getInputParameter(S_OBJECT_TYPE));
        if (objType == null || objType.isEmpty()) {
            errors.add("objectType cannot be null or empty");
        }
        @SuppressWarnings("unchecked")
        final List<List<String>> fieldValues = (List<List<String>>) getInputParameter(FIELD_VALUES);
        if (fieldValues == null || fieldValues.isEmpty()) {
            errors.add("fieldValues cannot be null or empty");
        } else {
            fieldValues.stream()
                    .filter(row -> row.size() != 2)
                    .findFirst()
                    .ifPresent(row -> errors
                            .add(String.format("fieldValue at index %s contains %s entries instead of 2.",
                                    fieldValues.indexOf(row), row.size())));
        }
        return errors;
    }

}
