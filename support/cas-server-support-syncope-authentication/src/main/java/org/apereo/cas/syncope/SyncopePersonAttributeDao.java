package org.apereo.cas.syncope;

import org.apereo.cas.configuration.model.support.syncope.SyncopePrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.search.OrSearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.fiql.FiqlParser;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.NamedPersonImpl;
import org.springframework.http.HttpMethod;

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

/**
 * This is {@link SyncopePersonAttributeDao}.
 *
 * @author Francesco Chicchiriccò
 * @since 6.5.0
 */
@Slf4j
public class SyncopePersonAttributeDao extends BasePersonAttributeDao {

    private static final ObjectMapper MAPPER =
        JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();

    private static final FiqlParser<SearchBean> FIQL_PARSER = new FiqlParser<>(SearchBean.class);

    private final SyncopePrincipalAttributesProperties properties;

    public SyncopePersonAttributeDao(final SyncopePrincipalAttributesProperties properties) {
        this.properties = properties;
        visit(properties.getSearchFilter());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, List<Object>> stuffAttributesIntoList(final Map<String, ?> map) {
        return map.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }

    @Override
    public IPersonAttributes getPerson(final String uid, final IPersonAttributeDaoFilter filter) {
        val attributes = new HashMap<String, List<Object>>();
        syncopeSearch(uid).filter(sr -> sr.has("result")).ifPresent(sr -> {
            val result = sr.get("result").iterator();
            if (result.hasNext()) {
                attributes.putAll(SyncopeUserTOConverterUtils.convert(result.next()));
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
        return map.entrySet()
            .stream()
            .filter(e -> properties.getSearchFilter().contains(e.getKey()) && e.getValue() != null && !e.getValue().isEmpty())
            .findFirst()
            .map(e -> Set.of(getPerson(e.getValue().get(0).toString(), filter)))
            .orElseGet(() -> new LinkedHashSet<>(0));
    }

    /**
     * Visit search filter.
     *
     * @param searchFilter the search filter
     * @return the set
     */
    protected Set<String> visit(final String searchFilter) {
        val sc = FIQL_PARSER.parse(searchFilter);
        val searchProperties = new LinkedHashSet<String>();
        visitCompound(new OrSearchCondition<>(List.of(sc)), searchProperties);
        return searchProperties;
    }

    /**
     * Visit compound values.
     *
     * @param sc         the sc
     * @param properties the properties
     */
    protected void visitCompound(final SearchCondition<SearchBean> sc, final Set<String> properties) {
        sc.getSearchConditions().forEach(searchCond -> {
            if (searchCond.getStatement() == null) {
                visitCompound(searchCond, properties);
            } else {
                properties.add(visitPrimitive(searchCond));
            }
        });
    }

    /**
     * Visit primitive value.
     *
     * @param sc the sc
     * @return the string
     */
    protected String visitPrimitive(final SearchCondition<SearchBean> sc) {
        return sc.getStatement().getProperty();
    }

    /**
     * Syncope search optional.
     *
     * @param value the value
     * @return the optional
     */
    @SneakyThrows
    protected Optional<JsonNode> syncopeSearch(final String value) {
        HttpResponse response = null;
        try {
            val fiql = EncodingUtils.urlEncode(properties.getSearchFilter().replace("{user}", value).replace("{0}", value));
            val syncopeRestUrl = StringUtils.appendIfMissing(properties.getUrl(), "/")
                                 + "rest/users/?page=1&size=1&details=true&fiql=" + fiql;
            LOGGER.debug("Executing Syncope search via [{}]", syncopeRestUrl);

            val requestHeaders = new LinkedHashMap<String, Object>();
            requestHeaders.put("X-Syncope-Domain", properties.getDomain());
            requestHeaders.putAll(properties.getHeaders());
            val exec = HttpUtils.HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(syncopeRestUrl)
                .basicAuthUsername(properties.getBasicAuthUsername())
                .basicAuthPassword(properties.getBasicAuthPassword())
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
}
