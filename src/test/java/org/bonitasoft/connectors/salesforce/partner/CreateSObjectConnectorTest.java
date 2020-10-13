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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

@ExtendWith(MockitoExtension.class)
class CreateSObjectConnectorTest {

    @Mock
    private PartnerConnection connection;

    @Test
    void should_objectType_and_fieldValues_be_mandatory_parameters() throws Exception {
        // Given 
        CreateSObjectConnector connector = new CreateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("objectType cannot be null or empty")
                .hasMessageContaining("fieldValues cannot be null or empty");
    }

    @Test
    void should_objectType_and_fieldValues_be_not_empty() throws Exception {
        // Given 
        CreateSObjectConnector connector = new CreateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(CreateSObjectConnector.S_OBJECT_TYPE, "");
        parameters.put(CreateSObjectConnector.FIELD_VALUES, new ArrayList<>());
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("objectType cannot be null or empty")
                .hasMessageContaining("fieldValues cannot be null or empty");
    }
    
    @Test
    void should_fieldValues_have_only_two_elements_per_row() throws Exception {
        // Given 
        CreateSObjectConnector connector = new CreateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(CreateSObjectConnector.S_OBJECT_TYPE, "MyType");
        parameters.put(CreateSObjectConnector.FIELD_VALUES, asList(asList("fieldName","fieldValue","tooManyElement")));
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("fieldValue at index 0 contains 3 entries instead of 2.");
    }


    @Test
    void should_execute_create_function() throws Exception {
        // Given 
        CreateSObjectConnector connector = spy(new CreateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        parameters.put(CreateSObjectConnector.S_OBJECT_TYPE, objectType);
        parameters.put(CreateSObjectConnector.FIELD_VALUES, asList(asList("name", "romain")));
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        SaveResult saveResult = new SaveResult();
        saveResult.setId("objectId");
        SaveResult[] results = new SaveResult[] { saveResult };
        when(connection.create(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).create(argumentCaptor.capture());

        SObject[] objectsToCreate = argumentCaptor.getValue();
        assertThat(objectsToCreate).hasSize(1);
        assertThat(objectsToCreate[0].getType()).isEqualTo(objectType);
        assertThat(objectsToCreate[0].getField("name")).isEqualTo("romain");
        assertThat(output).contains(
                entry(SalesforceConnector.S_OBJECT_ID_OUTPUT, "objectId"),
                entry(SalesforceConnector.SAVE_RESULT_OUTPUT, saveResult));

    }
    
    @Test
    void should_use_toString_value_when_field_value_is_not_serializable() throws Exception {
        // Given 
        CreateSObjectConnector connector = spy(new CreateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        parameters.put(CreateSObjectConnector.S_OBJECT_TYPE, objectType);
        
        parameters.put(CreateSObjectConnector.FIELD_VALUES, asList(asList("user", new User("romain"))));
        connector.setInputParameters(parameters);

        SaveResult saveResult = new SaveResult();
        saveResult.setId("objectId");
        SaveResult[] results = new SaveResult[] { saveResult };
        when(connection.create(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).create(argumentCaptor.capture());

        SObject[] objectsToCreate = argumentCaptor.getValue();
        assertThat(objectsToCreate).hasSize(1);
        assertThat(objectsToCreate[0].getType()).isEqualTo(objectType);
        assertThat(objectsToCreate[0].getField("user")).isEqualTo("Name: romain");
        assertThat(output).contains(
                entry(SalesforceConnector.S_OBJECT_ID_OUTPUT, "objectId"),
                entry(SalesforceConnector.SAVE_RESULT_OUTPUT, saveResult));
    }

    @ParameterizedTest
    @CsvSource({",someValue", "someField,"})
    void should_ignore_empty_fields(String fieldName, String fieldValue) throws Exception {
        // Given 
        CreateSObjectConnector connector = spy(new CreateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        parameters.put(CreateSObjectConnector.S_OBJECT_TYPE, objectType);
        parameters.put(CreateSObjectConnector.FIELD_VALUES, asList(asList(fieldName, fieldValue)));
        connector.setInputParameters(parameters);

        doReturn(connection).when(connector).getConnection();

        // When
        connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).create(argumentCaptor.capture());

        SObject[] objectsToCreate = argumentCaptor.getValue();
        Iterator<XmlObject> children = objectsToCreate[0].getChildren();
        assertThat(children.next().getValue()).isEqualTo(objectType);
        assertThat(children.hasNext()).isFalse();
    }
    
    private class User {
        
        private String name ;

        User(String name){
            this.name = name;
        }
        
        @Override
        public String toString() {
            return String.format("Name: %s", name);
        }
        
    }

}
