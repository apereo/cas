package org.apereo.cas.azure.ad.authentication;

import module java.base;
import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.util.function.FunctionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.ReflectionUtils;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Provides the ability to fetch attributes from azure active directory
 * using the graph api.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
public class MicrosoftGraphPersonAttributeDao extends BasePersonAttributeDao {
    private final UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private String tenant;

    private String resource = "https://graph.microsoft.com/";

    private String scope;

    private String grantType = "client_credentials";

    private String clientId;

    private String clientSecret;

    private String properties;

    private String apiBaseUrl = "https://graph.microsoft.com/v1.0/";

    private String loginBaseUrl = "https://login.microsoftonline.com/%s/";

    private String domain;

    /**
     * NONE,BASIC,HEADERS or BODY.
     */
    private String loggingLevel = "BASIC";

    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resultPeople, final PersonAttributeDaoFilter filter) {
        return FunctionUtils.doUnchecked(() -> {
            val loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(loggingLevel.toUpperCase(Locale.ENGLISH)));

            val token = getToken();
            val client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    val request = chain.request().newBuilder().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).build();
                    return chain.proceed(request);
                })
                .addInterceptor(loggingInterceptor)
                .build();
            val retrofit = new Retrofit
                .Builder()
                .baseUrl(this.apiBaseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(client)
                .build();

            val service = retrofit.create(GraphApiService.class);
            val user = this.domain == null ? uid : uid + '@' + this.domain;
            val call = service.getUserByUserPrincipalName(user,
                StringUtils.defaultIfBlank(this.properties, String.join(",", User.getDefaultFieldQuery())));

            val userCallResult = call.execute();
            if (userCallResult.isSuccessful()) {
                val response = userCallResult.body();
                val attributes = response.buildAttributes();
                return new SimplePersonAttributes(uid, PersonAttributeDao.stuffAttributesIntoList(attributes));
            }
            try (val errorBody = userCallResult.errorBody()) {
                throw new RuntimeException("error requesting token (" + userCallResult.code() + "): " + errorBody.string());
            }
        });
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                            final PersonAttributeDaoFilter filter,
                                            final Set<PersonAttributes> resultPeople) {
        return getPeopleWithMultivaluedAttributes(PersonAttributeDao.stuffAttributesIntoList(query), filter, resultPeople);
    }

    @Override
    public Set<PersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query,
                                                                     final PersonAttributeDaoFilter filter,
                                                                     final Set<PersonAttributes> resultPeople) {
        val people = new LinkedHashSet<PersonAttributes>();
        val username = usernameAttributeProvider.getUsernameFromQuery(query);
        val person = getPerson(username, resultPeople, filter);
        if (person != null) {
            people.add(person);
        }
        return people;
    }

    private String getToken() throws Exception {
        val loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.valueOf(loggingLevel.toUpperCase(Locale.ENGLISH)));

        val client = new OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build();

        val retrofit = new Retrofit.Builder()
            .baseUrl(String.format(this.loginBaseUrl, this.tenant))
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build();
        val service = retrofit.create(GraphAuthApiService.class);
        val response = service.getOauth2Token(this.grantType, this.clientId, this.clientSecret, this.scope, this.resource).execute();
        if (response.isSuccessful()) {
            val info = response.body();
            return Objects.requireNonNull(info).accessToken;
        }
        try (val errorBody = response.errorBody()) {
            throw new RuntimeException("error requesting token (" + response.code() + "): " + errorBody);
        }
    }

    private interface GraphApiService {
        @GET("users/{upn}")
        Call<User> getUserByUserPrincipalName(
            @Path("upn")
            String upn,
            @Query(value = "$select", encoded = true)
            String selectQuery);
    }

    private interface GraphAuthApiService {
        @FormUrlEncoded
        @POST("oauth2/token")
        Call<OAuthTokenInfo> getOauth2Token(
            @Field("grant_type")
            String grantType,
            @Field("client_id")
            String clientId,
            @Field("client_secret")
            String clientSecret,
            @Field("scope")
            String scope,
            @Field("resource")
            String resource
        );
    }

    @Getter
    @Setter
    private static final class User implements Serializable {
        @Serial
        private static final long serialVersionUID = 8497244140827305607L;

        private String userPrincipalName;

        private String id;

        private boolean accountEnabled;

        private String displayName;

        private String mail;

        private String jobTitle;

        private String officeLocation;

        private String preferredLanguage;

        private String mobilePhone;

        private String surname;

        private String givenName;

        private String passwordPolicies;

        private String preferredName;

        private List<String> businessPhones = new ArrayList<>();

        private List<String> schools = new ArrayList<>();

        private List<String> skills = new ArrayList<>();

        private String postalCode;

        private String consentProvidedForMinor;

        private String aboutMe;

        private String streetAddress;

        private String userType;

        private String usageLocation;

        private String state;

        private String ageGroup;

        private String otherMails;

        private String city;

        private String country;

        private String countryName;

        private String department;

        private String employeeId;

        private String faxNumber;

        private String mailNickname;

        private String onPremisesSamAccountName;

        static List<String> getDefaultFieldQuery() {
            return List.of("businessPhones,displayName,givenName,id,"
                + "jobTitle,mail,givenName,employeeId,"
                + "mobilePhone,officeLocation,accountEnabled,"
                + "preferredLanguage,surname,userPrincipalName");
        }

        @JsonIgnore
        private Map<String, Object> buildAttributes() {
            val fields = new HashMap<String, Object>();
            ReflectionUtils.doWithFields(getClass(), field -> {
                field.setAccessible(true);
                fields.put(field.getName(), field.get(this));
            });
            return fields;
        }
    }

    @Getter
    @Setter
    private static final class OAuthTokenInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = -8586825191767772463L;

        @Json(name = "token_type")
        private String tokenType;

        @Json(name = "scope")
        private String scope;

        @Json(name = "expires_in")
        private int expiresIn;

        @Json(name = "expires_on")
        private int expiresOn;

        @Json(name = "not_before")
        private int notBefore;

        @Json(name = "resource")
        private String resource;

        @Json(name = "access_token")
        private String accessToken;
    }
}
