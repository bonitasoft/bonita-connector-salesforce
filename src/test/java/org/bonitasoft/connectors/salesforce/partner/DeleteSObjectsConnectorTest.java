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
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sforce.soap.partner.DeleteResult;
import com.sforce.soap.partner.PartnerConnection;

@ExtendWith(MockitoExtension.class)
class DeleteSObjectsConnectorTest {
    
    @Mock
    private PartnerConnection connection;

    @ParameterizedTest
    @MethodSource("nullAndEmptyList")
    void should_sObjectIds_be_mandatory_parameters(List<String> ids) throws Exception {
        // Given 
        DeleteSObjectsConnector connector = new DeleteSObjectsConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(DeleteSObjectsConnector.S_OBJECT_IDS, ids);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("sObjectIds cannot be null or empty");
    }
    
    @ParameterizedTest
    @MethodSource("invalidIdsList")
    void should_sObjectIds_not_contains_null_or_empty_values(List<String> ids) throws Exception {
        // Given 
        DeleteSObjectsConnector connector = new DeleteSObjectsConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(DeleteSObjectsConnector.S_OBJECT_IDS, ids);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("An id of sObject to delete is null or empty");
    }
    
    @Test
    void should_execute_delete_function() throws Exception {
        // Given 
        DeleteSObjectsConnector connector = spy(new DeleteSObjectsConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(DeleteSObjectsConnector.S_OBJECT_IDS, asList("1","2"));
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        DeleteResult deleteResult = new DeleteResult();
        deleteResult.setId("objectId");
        DeleteResult[] results = new DeleteResult[] { deleteResult };
        when(connection.delete(Mockito.any())).thenReturn(results);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(connection).delete(argumentCaptor.capture());

        String[] idToDelete = argumentCaptor.getValue();
        assertThat(idToDelete).containsExactly("1","2");
        assertThat(output).contains(entry(DeleteSObjectsConnector.DELETE_RESULTS_OUTPUT, asList(results)));
    }
    
    private static Stream<Arguments> nullAndEmptyList() {
        return Stream.of(
          null,
          Arguments.of(new ArrayList<>())
        );
    }
    
    private static Stream<Arguments> invalidIdsList() {
        return Stream.of(
          Arguments.of(asList("1", "")),
          Arguments.of(asList(null, "2"))
        );
    }

}
