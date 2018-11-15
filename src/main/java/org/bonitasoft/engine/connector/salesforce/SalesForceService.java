package org.bonitasoft.engine.connector.salesforce;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @author Danila Mazour
 */
public interface SalesForceService {

    @FormUrlEncoded
    @POST("services/oath2/token")
    Call<Map<String, String>> authenticate(
            @Field("usernameOrEmailAddress") String user,
            @Field("password") String password, @Field("securityToken") String token);



}
