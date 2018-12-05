/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.connectors.salesforce.partner;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * @author Charles Souillard, Haris Subašić
 */

public abstract class SalesforceConnector extends AbstractConnector {

    // why partner or enterprise:
    // http://www.salesforce.com/us/developer/docs/api/Content/sforce_api_partner.htm

    // partner jar
    // http://www.salesforce.com/us/developer/docs/api_asynch/Content/asynch_api_code_set_up_client.htm
    // http://code.google.com/p/sfdc-wsc/issues/detail?id=26
    // http://code.google.com/p/sfdc-wsc/downloads/list

    // getting a working jar file
    // http://www.salesforce.com/us/developer/docs/api/Content/sforce_api_quickstart_steps.htm

    private static final Logger LOGGER = Logger.getLogger(AbstractConnector.class.getName());
    
    public static final String USERNAME = "username";

    public static final String PASSWORD = "password";

    public static final String SECURITYTOKEN = "securityToken";

    public static final String AUTHENDPOINT = "authEndpoint";

    public static final String SERVICEENDPOINT = "serviceEndpoint";

    public static final String RESTENDPOINT = "restEndpoint";

    public static final String PROXYHOST = "proxyHost";

    public static final String PROXYPORT = "proxyPort";

    public static final String PROXYUSERNAME = "proxyUsername";

    public static final String PROXYPASSWORD = "proxyPassword";

    public static final String CONNECTIONTIMEOUT = "connectionTimeout";

    public static final String READTIMEOUT = "readTimeout";

    private PartnerConnection connection;

    protected abstract void executeFunction(final PartnerConnection connection) throws Exception;

    protected abstract List<String> validateExtraValues();

    @Override
    public void connect() throws ConnectorException {
        final ConnectorConfig config = getConnectorConfiguration();
        try {
            connection = com.sforce.soap.partner.Connector.newConnection(config);
        }catch (ConnectionException e) {
            throw new ConnectorException("Failed to login to Salesforce.", e);
        }
    }
    
    @Override
    public void disconnect() throws ConnectorException {
        if(connection != null ) {
            try {
                connection.logout();
            } catch (ConnectionException e) {
                LOGGER.warning("An error occurred while disconnecting from Salesforce: "+ e.toString());
            }
        }
    }
    
    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            this.executeFunction(connection);
        } catch (final Exception e) {
            throw new ConnectorException(e);
        }
    }

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        final List<String> errors = new ArrayList<String>(1);
        // validate login / password are non-empty
        final String username = ((String) getInputParameter(USERNAME));
        if (username == null || username.isEmpty()) {
            errors.add("username must be set");
        }
        final String password = ((String) getInputParameter(PASSWORD));
        if (password == null || password.isEmpty()) {
            errors.add("password must be set");
        }
        final String securityToken = ((String) getInputParameter(SECURITYTOKEN));
        if (securityToken == null || securityToken.isEmpty()) {
            errors.add("security token must be set");
        }
        // validate timeout and port number
        final Integer connectionTimeout = ((Integer) getInputParameter(CONNECTIONTIMEOUT));
        if (connectionTimeout != null) {
            if (connectionTimeout < 0) {
                errors.add("connectionTimeout cannot be less than 0!");
            }
        }
        if (connectionTimeout != null) {
            final Integer readTimeout = ((Integer) getInputParameter(READTIMEOUT));
            if (readTimeout < 0) {
                errors.add("readTimeout cannot be less than 0!");
            }
        }
        if (connectionTimeout != null) {
            final Integer proxyPort = ((Integer) getInputParameter(PROXYPORT));
            if (proxyPort < 0) {
                errors.add("proxyPort cannot be less than 0!");
            } else if (proxyPort > 65535) {
                errors.add("proxyPort cannot be greater than 65535!");
            }
        }

        String authEndpoint = (String) getInputParameter(SalesforceConnector.AUTHENDPOINT);
        if (authEndpoint != null && !authEndpoint.endsWith("24.0")) {
            errors.add("Please ensure the API version is 24.0");
        }
        String serviceEndpoint = (String) getInputParameter(SalesforceConnector.SERVICEENDPOINT);
        if (serviceEndpoint != null && !serviceEndpoint.endsWith("24.0")) {
            errors.add("Please ensure the API version is 24.0");
        }

        String restEndpoint = (String) getInputParameter(SalesforceConnector.RESTENDPOINT);
        if (restEndpoint != null && !restEndpoint.endsWith("24.0")) {
            errors.add("Please ensure the API version is 24.0");
        }

        errors.addAll(validateExtraValues());

        if (!errors.isEmpty()) {
            throw new ConnectorValidationException(this, errors);
        }

    }

    protected ConnectorConfig getConnectorConfiguration() {
        final ConnectorConfig config = new ConnectorConfig();
        final String username = (String) getInputParameter(USERNAME);
        config.setUsername(username);
        final String password = (String) getInputParameter(PASSWORD);
        final String Securitytoken = (String) getInputParameter(SECURITYTOKEN);
        final String passwordSecuritytoken = password + Securitytoken;
        config.setPassword(passwordSecuritytoken);
        final String authendpoint = (String) getInputParameter(AUTHENDPOINT);
        if (authendpoint != null) {
            config.setAuthEndpoint(authendpoint);
        }
        final String proxyhost = (String) getInputParameter(PROXYHOST);
        final Integer proxyport = (Integer) getInputParameter(PROXYPORT);
        if (proxyhost != null && proxyhost.length() > 0 && proxyport != null && proxyport > 0) {
            config.setProxy(proxyhost, proxyport);
        }
        final String proxypassword = (String) getInputParameter(PROXYPASSWORD);
        if (proxypassword != null) {
            config.setProxyPassword(proxypassword);
        }
        final String proxyusername = (String) getInputParameter(PROXYUSERNAME);
        if (proxyusername != null) {
            config.setProxyUsername(proxyusername);
        }
        final Integer connectiontimeout = (Integer) getInputParameter(CONNECTIONTIMEOUT);
        if (connectiontimeout != null && connectiontimeout > 0) {
            config.setConnectionTimeout(connectiontimeout);
        }
        final Integer readtimeout = (Integer) getInputParameter(READTIMEOUT);
        if (readtimeout != null && readtimeout > 0) {
            config.setReadTimeout(readtimeout);
        }
        final String restendpoint = (String) getInputParameter(RESTENDPOINT);
        if (restendpoint != null) {
            config.setRestEndpoint(restendpoint);
        }
        final String serviceendpoint = (String) getInputParameter(SERVICEENDPOINT);
        if (serviceendpoint != null) {
            config.setServiceEndpoint(serviceendpoint);
        }
        config.setPrettyPrintXml(true);

        return config;
    }

    protected String getErrorIfIdLengthInvalid(String id) {
        id = (String) getInputParameter(id);
        if (id.length() != 15 && id.length() != 18) {
            return "Length of sObjectId should be 15 or 18! Id is: " + id;
        }
        return null;
    }

    protected String getErrorIfNullOrEmptyParam(final String paramName) {
        Object param = getInputParameter(paramName);
        if (param != null) {
            param = param.toString().trim();
            if (((String) param).length() > 0) {
                return null;
            }
        }
        return "The parameter cannot be null or empty: " + paramName;
    }

}
