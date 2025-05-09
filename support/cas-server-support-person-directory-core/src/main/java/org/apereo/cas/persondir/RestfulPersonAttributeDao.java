package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.BasePersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDaoFilter;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpMethod;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Provides the ability to contact a URL resource to ask for attributes.
 * Support GET/POST endpoints, and provides the username in form of a parameter.
 * The response is expected to be a JSON map.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
public class RestfulPersonAttributeDao extends BasePersonAttributeDao {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    private UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider();

    private String url;
    private String basicAuthUsername;
    private String basicAuthPassword;
    private String method;
    private String principalId = "username";
    private Map<String, String> parameters = new LinkedHashMap<>();
    private Map<String, String> headers = new LinkedHashMap<>();

    @Override
    public PersonAttributes getPerson(final String uid, final Set<PersonAttributes> resultPeople, final PersonAttributeDaoFilter filter) {
        try {
            if (!this.isEnabled()) {
                return null;
            }
            val builder = HttpClientBuilder.create();
            val uriBuilder = new URIBuilder(this.url);
            if (StringUtils.isNotBlank(this.basicAuthUsername) && StringUtils.isNotBlank(this.basicAuthPassword)) {
                val provider = new BasicCredentialsProvider();
                val credentials = new UsernamePasswordCredentials(this.basicAuthUsername, this.basicAuthPassword.toCharArray());
                provider.setCredentials(new AuthScope(new HttpHost(uriBuilder.getHost())), credentials);
                builder.setDefaultCredentialsProvider(provider);
            }
            val client = builder.build();
            uriBuilder.addParameter(principalId, Objects.requireNonNull(uid, () -> principalId + " cannot be null"));
            this.parameters.forEach(uriBuilder::addParameter);

            val uri = uriBuilder.build();
            val request = method.equalsIgnoreCase(HttpMethod.GET.name()) ? new HttpGet(uri) : new HttpPost(uri);
            this.headers.forEach(request::addHeader);

            val response = client.execute(request);
            val attributes = MAPPER.readValue(response.getEntity().getContent(), Map.class);
            return new SimplePersonAttributes(uid, PersonAttributeDao.stuffAttributesIntoList(attributes));
        } catch (final Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @Override
    public Set<PersonAttributes> getPeople(final Map<String, Object> query,
                                           final PersonAttributeDaoFilter filter,
                                           final Set<PersonAttributes> resultPeople) {
        val queryAttributes = PersonAttributeDao.stuffAttributesIntoList(query);
        return getPeopleWithMultivaluedAttributes(queryAttributes, filter, resultPeople);
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
}
