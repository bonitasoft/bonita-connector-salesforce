package org.bonitasoft.engine.connector.salesforce.model;


import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.bonitasoft.engine.connector.salesforce.SalesForceService;
import org.bonitasoft.engine.connector.salesforce.converter.WrappedAttributeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit.Builder;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Danila Mazour
 */
public abstract class SalesforceConnector extends AbstractConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceConnector.class.getName());


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

    protected SalesForceService service;
    protected ObjectMapper mapper = new ObjectMapper();


    @Override protected void executeBusinessLogic() throws ConnectorException {

    }

    @Override public void validateInputParameters() throws ConnectorValidationException {

        checkMandatoryStringInput(USERNAME);
        checkMandatoryStringInput(PASSWORD);
        checkMandatoryStringInput(SECURITYTOKEN);
        checkMandatoryStringInput(AUTHENDPOINT);
        checkMandatoryStringInput(SERVICEENDPOINT);
        checkMandatoryStringInput(RESTENDPOINT);
        checkMandatoryStringInput(PROXYHOST);
        checkMandatoryStringInput(PROXYPORT);
        checkMandatoryStringInput(PROXYUSERNAME);
        checkMandatoryStringInput(PROXYPASSWORD);
        checkMandatoryStringInput(CONNECTIONTIMEOUT);
        checkMandatoryStringInput(READTIMEOUT);
    }


    protected void checkMandatoryStringInput(String input) throws ConnectorValidationException {
        String value = null;
        try {
            value = (String) getInputParameter(input);
        } catch (ClassCastException e) {
            throw new ConnectorValidationException(this, String.format("'%s' parameter must be a String", input));
        }

        if (value == null || value.isEmpty()) {
            throw new ConnectorValidationException(this,
                    String.format("Mandatory parameter '%s' is missing.", input));
        }
    }

    protected SalesForceService createService() {
        if (service == null) {
            OkHttpClient client = null;
            if (LOGGER.isDebugEnabled()) {
                HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
                interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            }

            Builder retrofitBuilder = new Builder()
                    .addConverterFactory(new WrappedAttributeConverter(mapper))
                    .addConverterFactory(JacksonConverterFactory.create())
                    .baseUrl(appendTraillingSlash(getRestEndPoint()));

            if (client != null) {
                retrofitBuilder.client(client);
            }

            service = retrofitBuilder.build().create(SalesForceService.class);
        }
        return service;
    }


    private static String appendTraillingSlash(String url) {
        return url.endsWith("/") ? url : url + "/";
    }

    String getRestEndPoint() {
        return (String) getInputParameter(RESTENDPOINT);
    }
}
