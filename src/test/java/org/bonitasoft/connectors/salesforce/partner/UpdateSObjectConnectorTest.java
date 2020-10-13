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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.bind.XmlObject;

@ExtendWith(MockitoExtension.class)
class UpdateSObjectConnectorTest {
    
    @Mock
    private PartnerConnection connection;

    @ParameterizedTest
    @NullAndEmptySource
    void should_sObjectId_be_mandatory_parameters(String id) throws Exception {
        // Given 
        UpdateSObjectConnector connector = new UpdateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(UpdateSObjectConnector.S_OBJECT_ID, id);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("The parameter cannot be null or empty: sObjectId");
    }
    
    @ParameterizedTest
    @CsvSource({"tooShort","toooooooooooooooooooooLonnnnnnnnng"})
    void should_sObjectId_have_proper_length(String id) throws Exception {
        // Given 
        UpdateSObjectConnector connector = new UpdateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(UpdateSObjectConnector.S_OBJECT_ID, id);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining(  "Length of sObjectId should be 15 or 18! Id is: "+id);
    }
    
  
    
    @ParameterizedTest
    @NullAndEmptySource
    void should_sObjectType_be_mandatory_parameters(String type) throws Exception {
        // Given 
        UpdateSObjectConnector connector = new UpdateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(UpdateSObjectConnector.S_OBJECT_TYPE, type);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("The parameter cannot be null or empty: sObjectId");
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    void should_fieldValues_be_not_empty(List<List<Object>> fieldValues) throws Exception {
        // Given 
        UpdateSObjectConnector connector = new UpdateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(UpdateSObjectConnector.FIELD_VALUES, fieldValues);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("fieldValues cannot be null or empty");
    }
    
    @Test
    void should_fieldValues_have_only_two_elements_per_row() throws Exception {
        // Given 
        UpdateSObjectConnector connector = new UpdateSObjectConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(UpdateSObjectConnector.FIELD_VALUES, asList(asList("fieldName","fieldValue","tooManyElement")));
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("fieldValue at index 0 contains 3 entries instead of 2.");
    }
    
    @Test
    void should_execute_update_function() throws Exception {
        // Given 
        UpdateSObjectConnector connector = spy(new UpdateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        parameters.put(UpdateSObjectConnector.S_OBJECT_ID, "monObjectIdent1");
        parameters.put(UpdateSObjectConnector.S_OBJECT_TYPE, objectType);
        parameters.put(UpdateSObjectConnector.FIELD_VALUES, asList(asList("name", "romain")));
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        SaveResult saveResult = new SaveResult();
        saveResult.setId("objectId");
        SaveResult[] results = new SaveResult[] { saveResult };
        when(connection.update(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).update(argumentCaptor.capture());

        SObject[] objectsToCreate = argumentCaptor.getValue();
        assertThat(objectsToCreate).hasSize(1);
        assertThat(objectsToCreate[0].getType()).isEqualTo(objectType);
        assertThat(objectsToCreate[0].getField("name")).isEqualTo("romain");
        assertThat(output).contains(
                entry(UpdateSObjectConnector.SAVE_RESULT, saveResult));

    }
    
    @Test
    void should_use_toString_value_when_field_value_is_not_serializable() throws Exception {
        // Given 
        UpdateSObjectConnector connector = spy(new UpdateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        parameters.put(UpdateSObjectConnector.S_OBJECT_ID, "monObjectIdent1");
        parameters.put(UpdateSObjectConnector.S_OBJECT_TYPE, objectType);
        parameters.put(UpdateSObjectConnector.FIELD_VALUES, asList(asList("user", new User("romain"))));
        connector.setInputParameters(parameters);

        SaveResult saveResult = new SaveResult();
        SaveResult[] results = new SaveResult[] { saveResult };
        when(connection.update(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).update(argumentCaptor.capture());

        SObject[] objectsToUpdate = argumentCaptor.getValue();
        assertThat(objectsToUpdate).hasSize(1);
        assertThat(objectsToUpdate[0].getType()).isEqualTo(objectType);
        assertThat(objectsToUpdate[0].getField("user")).isEqualTo("Name: romain");
        assertThat(output).contains(
                entry(SalesforceConnector.SAVE_RESULT_OUTPUT, saveResult));
    }

    @ParameterizedTest
    @CsvSource({",someValue", "someField,"})
    void should_ignore_empty_fields(String fieldName, String fieldValue) throws Exception {
        // Given 
        UpdateSObjectConnector connector = spy(new UpdateSObjectConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        String objectType = "Customer";
        String id = "monObjectIdent1";
        parameters.put(UpdateSObjectConnector.S_OBJECT_ID, id);
        parameters.put(UpdateSObjectConnector.S_OBJECT_TYPE, objectType);
        parameters.put(UpdateSObjectConnector.FIELD_VALUES, asList(asList(fieldName, fieldValue)));
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        SaveResult saveResult = new SaveResult();
        saveResult.setId("objectId");
        SaveResult[] results = new SaveResult[] { saveResult };
        when(connection.update(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        connector.execute();

        // Then
        ArgumentCaptor<SObject[]> argumentCaptor = ArgumentCaptor.forClass(SObject[].class);
        verify(connection).update(argumentCaptor.capture());

        SObject[] objectsToUpdate = argumentCaptor.getValue();
        Iterator<XmlObject> children = objectsToUpdate[0].getChildren();
        assertThat(children.next().getValue()).isEqualTo(objectType);
        assertThat(children.next().getValue()).isEqualTo(id);
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
