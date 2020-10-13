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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;

@ExtendWith(MockitoExtension.class)
class SalesforceConnectorTest {
    
    @Mock
    private PartnerConnection connection;

    @ParameterizedTest
    @NullAndEmptySource
    void should_username_be_mandatory(String username) throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.USERNAME, username);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("username must be set");
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    void should_password_be_mandatory(String password) throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.PASSWORD, password);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("password must be set");
    }
    
    @ParameterizedTest
    @NullAndEmptySource
    void should_security_token_be_mandatory(String token) throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.SECURITYTOKEN, token);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("security token must be set");
    }
    
    @Test
    void should_connection_timeout_parameter_be_a_positive_int() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.CONNECTIONTIMEOUT, -10);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("connectionTimeout cannot be less than 0!");
    }
    
    @Test
    void should_read_timeout_parameter_be_a_positive_int() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.READTIMEOUT, -10);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("readTimeout cannot be less than 0!");
    }
    
    @Test
    void should_proxyPort_parameter_be_a_positive_int() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.PROXYPORT, -12);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("proxyPort cannot be less than 0!");
    }
    
    @Test
    void should_proxyPort_parameter_be_a_positive_lesser_than_port_max_range() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.PROXYPORT, 65536);
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("proxyPort cannot be greater than 65535!");
    }
    
    @Test
    void should_authEndpoint_ends_with_proper_api_version() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.AUTHENDPOINT, "http://some/auth/end/point");
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("Please ensure the API version is 24.0");
    }
    
    @Test
    void should_serviceEndpoint_ends_with_proper_api_version() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.SERVICEENDPOINT, "http://some/service/end/point");
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("Please ensure the API version is 24.0");
    }
    
    @Test
    void should_restEndpoint_ends_with_proper_api_version() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.RESTENDPOINT, "http://some/rest/end/point");
        connector.setInputParameters(parameters);

        // When
        ConnectorValidationException exception = assertThrows(ConnectorValidationException.class,
                () -> connector.validateInputParameters());

        // Then
        assertThat(exception)
                .hasMessageContaining("Please ensure the API version is 24.0");
    }
    
    @Test
    void should_validate_minimal_configuration() throws Exception {
        // Given
        SalesforceConnector connector = new TestSalesforceConnector();
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        connector.setInputParameters(parameters);

        // When
        assertDoesNotThrow(() -> connector.validateInputParameters());
    }
    
    @Test
    void should_create_a_new_connection() throws Exception {
        // Given
        SalesforceConnector connector = spy(new TestSalesforceConnector());
        doReturn(connection).when(connector).newConnection(Mockito.any());
        Map<String, Object> parameters = ConnectorConfiguration.defaultConfiguration();
        parameters.put(SalesforceConnector.CONNECTIONTIMEOUT, 100);
        parameters.put(SalesforceConnector.READTIMEOUT, 100);
        parameters.put(SalesforceConnector.PROXYPORT, 666);
        parameters.put(SalesforceConnector.AUTHENDPOINT, "http://some/auth/end/point/24.0");
        parameters.put(SalesforceConnector.SERVICEENDPOINT, "http://some/service/end/point/24.0");
        parameters.put(SalesforceConnector.RESTENDPOINT, "http://some/rest/end/point/24.0");
        connector.setInputParameters(parameters);
        connector.validateInputParameters();

        // When
        connector.connect();
        //Then 
        assertThat(connector.getConnection()).isEqualTo(connection);
        
        // When
        connector.execute();
        
        // Then
        verify(connector).executeBusinessLogic();
        
        //When 
        connector.disconnect();
        
        // Then
        assertThat(connector.getConnection()).isNull();
    }


    class TestSalesforceConnector extends SalesforceConnector {

        @Override
        protected void executeFunction(PartnerConnection connection) throws ConnectionException {
            // Ignore
        }

        @Override
        protected List<String> validateExtraValues() {
            return Collections.emptyList();
        }

    }

}
