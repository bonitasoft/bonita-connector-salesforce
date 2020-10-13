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

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;

@ExtendWith(MockitoExtension.class)
class QuerySObjectsConnectorTest {

    @Mock
    private PartnerConnection connection;

    @ParameterizedTest
    @NullAndEmptySource
    void should_query_be_mandatory_parameters(String query) throws Exception {
        // Given 
        QuerySObjectsConnector connector = new QuerySObjectsConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(QuerySObjectsConnector.QUERY, query);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("Query cannot be empty or null");
    }

    @ParameterizedTest
    @MethodSource("queryResultProvider")
    @NullSource
    void should_execute_query_function(QueryResult queryResult) throws Exception {
        // Given 
        QuerySObjectsConnector connector = spy(new QuerySObjectsConnector());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(QuerySObjectsConnector.QUERY, "Some query");
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        when(connection.query(Mockito.any())).thenReturn(queryResult);
        doReturn(connection).when(connector).getConnection();

        // When
        Map<String, Object> output = connector.execute();

        // Then
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(connection).query(argumentCaptor.capture());

        String query = argumentCaptor.getValue();
        assertThat(query).isEqualTo("Some query");
        assertThat(output).contains(entry(QuerySObjectsConnector.QUERY_RESULT, queryResult),
                entry(QuerySObjectsConnector.S_OBJECTS, queryResult == null ? Collections.emptyList() : asList(queryResult.getRecords())));
    }

    private static Stream<Arguments> queryResultProvider() {
        return Stream.of(Arguments.of(new QueryResult()));
    }

}
