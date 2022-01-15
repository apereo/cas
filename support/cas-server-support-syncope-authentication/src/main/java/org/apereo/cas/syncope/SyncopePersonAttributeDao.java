package org.apereo.cas.syncope;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.cxf.jaxrs.ext.search.OrSearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.fiql.FiqlParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.springframework.http.HttpMethod;

/**
 * This is {@link SyncopePersonAttributeDao}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@Getter
@Setter
@Slf4j
public class SyncopePersonAttributeDao extends BasePersonAttributeDao {

    private static final ObjectMapper MAPPER =
            JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();

    private static final FiqlParser<SearchBean> FIQL_PARSER = new FiqlParser<>(SearchBean.class);

    @SuppressWarnings("unchecked")
    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> map) {
        return map.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }

    private final String syncopeUrl;

    private final String syncopeDomain;

    private final String basicAuthUsername;

    private final String basicAuthPassword;

    private final Map<String, String> headers;

    private final String searchFilter;

    private final Set<String> searchFilterProperties;

    public SyncopePersonAttributeDao(
            final String syncopeUrl,
            final String syncopeDomain,
            final String basicAuthUsername,
            final String basicAuthPassword,
            final Map<String, String> headers,
            final String searchFilter) {

        this.syncopeUrl = syncopeUrl;
        this.syncopeDomain = syncopeDomain;
        this.basicAuthUsername = basicAuthUsername;
        this.basicAuthPassword = basicAuthPassword;
        this.headers = headers;
        this.searchFilter = searchFilter;
        this.searchFilterProperties = visit(searchFilter);
    }

    protected Set<String> visit(final String searchFilter) {
        SearchCondition<SearchBean> sc = FIQL_PARSER.parse(searchFilter);
        Set<String> properties = new LinkedHashSet<>();
        visitCompound(new OrSearchCondition<>(List.of(sc)), properties);
        return properties;
    }

    protected void visitCompound(final SearchCondition<SearchBean> sc, final Set<String> properties) {
        sc.getSearchConditions().forEach(searchCond -> {
            if (searchCond.getStatement() == null) {
                visitCompound(searchCond, properties);
            } else {
                properties.add(visitPrimitive(searchCond));
            }
        });
    }

    protected String visitPrimitive(final SearchCondition<SearchBean> sc) {
        return sc.getStatement().getProperty();
    }

    @SneakyThrows
    protected Optional<JsonNode> syncopeSearch(final String value) {
        HttpResponse response = null;
        try {
            val syncopeRestUrl = this.syncopeUrl + "/rest/users/?page=1&size=1&details=true"
                    + "&fiql=" + EncodingUtils.urlEncode(searchFilter.replace("{user}", value).replace("{0}", value));
            val requestHeaders = new LinkedHashMap<String, Object>();
            requestHeaders.put("X-Syncope-Domain", this.syncopeDomain);
            requestHeaders.putAll(headers);
            val exec = HttpUtils.HttpExecutionRequest.builder()
                    .method(HttpMethod.GET)
                    .url(syncopeRestUrl)
                    .basicAuthUsername(basicAuthUsername)
                    .basicAuthPassword(basicAuthPassword)
                    .headers(requestHeaders)
                    .build();
            response = Objects.requireNonNull(HttpUtils.execute(exec));
            LOGGER.debug("Received http response status as [{}]", response.getStatusLine());
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                val result = EntityUtils.toString(response.getEntity());
                LOGGER.debug("Received user object as [{}]", result);
                return Optional.of(MAPPER.readTree(result));
            }
        } finally {
            HttpUtils.close(response);
        }
        return Optional.empty();
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, List<Object>>();
        syncopeSearch(uid).filter(sr -> sr.has("result")).ifPresent(sr -> {
            val result = sr.get("result").iterator();
            if (result.hasNext()) {
                attributes.putAll(SyncopeUserTOConverter.convert(result.next()));
            }
        });
        return new NamedPersonImpl(uid, attributes);
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> map, final IPersonAttributeDaoFilter filter) {
        return getPeopleWithMultivaluedAttributes(stuffAttributesIntoList(map), filter);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(
            final Map<String, List<Object>> map, final IPersonAttributeDaoFilter filter) {

        return map.entrySet().stream().
                filter(e -> this.searchFilter.contains(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty()).
                findFirst().
                map(e -> Set.of(getPerson(e.getValue().get(0).toString(), filter))).
                orElseGet(() -> new LinkedHashSet<>(0));
    }

    @Override
    public Set<String> getPossibleUserAttributeNames(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }

    @Override
    public Set<String> getAvailableQueryAttributes(final IPersonAttributeDaoFilter filter) {
        return new LinkedHashSet<>(0);
    }
}
