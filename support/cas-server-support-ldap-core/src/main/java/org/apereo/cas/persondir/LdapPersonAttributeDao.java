package org.apereo.cas.persondir;

import org.apereo.cas.authentication.attribute.AbstractQueryPersonAttributeDao;
import org.apereo.cas.authentication.attribute.SimplePersonAttributes;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.FilterTemplate;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchScope;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchResultHandler;
import javax.naming.directory.SearchControls;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Person directory {@link org.apereo.cas.authentication.principal.attribute.PersonAttributeDao}
 * implementation that queries an LDAP directory
 * with ldaptive components to populate person attributes.
 *
 * @author Marvin S. Addison
 * @since 7.1.0
 */
@Slf4j
@Setter
@Getter
public class LdapPersonAttributeDao extends AbstractQueryPersonAttributeDao<FilterTemplate> implements AutoCloseable {

    private String baseDN;

    private SearchControls searchControls;

    private ConnectionFactory connectionFactory;

    private String searchFilter;

    private String[] binaryAttributes;

    private LdapEntryHandler[] entryHandlers;

    private SearchResultHandler[] searchResultHandlers;

    @Override
    protected List<PersonAttributes> getPeopleForQuery(final FilterTemplate filter, final String userName) {
        try {
            Objects.requireNonNull(this.searchFilter, "Search filter cannot be null");
            val search = new SearchOperation(this.connectionFactory);
            search.setEntryHandlers(entryHandlers);
            search.setSearchResultHandlers(searchResultHandlers);
            val request = createRequest(filter);
            val response = search.execute(request);

            val peopleAttributes = new ArrayList<PersonAttributes>(response.entrySize());
            for (val entry : response.getEntries()) {
                val attributes = convertLdapEntryToMap(entry);
                if (response.getDiagnosticMessage() != null && !response.getDiagnosticMessage().isEmpty()) {
                    val values = new ArrayList<>();
                    values.add(response.getDiagnosticMessage());
                    attributes.put("diagnosticMessage", values);
                }
                if (response.getMatchedDN() != null && !response.getMatchedDN().isEmpty()) {
                    val values = new ArrayList<>();
                    values.add(response.getMatchedDN());
                    attributes.put("matchedDN", values);
                }

                val userNameAttribute = this.getConfiguredUserNameAttribute();
                val person = attributes.containsKey(userNameAttribute)
                    ? SimplePersonAttributes.fromAttribute(userNameAttribute, attributes)
                    : new SimplePersonAttributes(userName, attributes);
                peopleAttributes.add(person);
            }
            return peopleAttributes;
        } catch (final LdapException e) {
            throw new RuntimeException("Failed executing LDAP query " + filter, e);
        }
    }

    @Override
    protected FilterTemplate appendAttributeToQuery(final FilterTemplate filter, final String attribute, final List<Object> values) {
        val query = Objects.requireNonNullElseGet(filter, () -> {
            Objects.requireNonNull(this.searchFilter, "Search filter cannot be null");
            return new FilterTemplate(this.searchFilter);
        });
        if (this.isUseAllQueryAttributes() && values.size() > 1 && (this.searchFilter.contains("{0}") || this.searchFilter.contains("{user}"))) {
            LOGGER.warn("Query value will be indeterminate due to multiple attributes and no username indicator. Use attribute [{}] in query instead", attribute);
        }

        if (!values.isEmpty() && this.searchFilter.contains('{' + attribute + '}')) {
            query.setParameter(attribute, values.getFirst().toString());
            LOGGER.debug("Updated LDAP search query parameter [{}]. Query: [{}]", attribute, query.format());
        }
        return query;
    }

    @Override
    protected FilterTemplate finalizeQueryBuilder(final FilterTemplate filterTemplate, final Map<String, List<Object>> query) {
        if (filterTemplate.getParameters().isEmpty()) {
            val username = getUsernameAttributeProvider().getUsernameFromQuery(query);
            if (searchFilter.contains("{0}")) {
                LOGGER.debug("Filter template has not assigned any parameters. Using [{}] to populate the placeholder by index.", username);
                filterTemplate.setParameter(0, username);
            }
            if (this.searchFilter.contains("{user}")) {
                LOGGER.debug("Filter template has not assigned any parameters. Using [{}] to populate the placeholder variable.", username);
                filterTemplate.setParameter("user", username);
            }
        }
        return filterTemplate;
    }

    @SuppressWarnings("EnumOrdinal")
    protected SearchRequest createRequest(final FilterTemplate filter) {
        val request = new SearchRequest();
        request.setBaseDn(this.baseDN);
        request.setFilter(filter);
        request.setBinaryAttributes(binaryAttributes);

        if (getResultAttributeMapping() != null && !getResultAttributeMapping().isEmpty()) {
            val attributes = getResultAttributeMapping().keySet().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
            request.setReturnAttributes(attributes);
        } else if (searchControls.getReturningAttributes() != null && searchControls.getReturningAttributes().length > 0) {
            request.setReturnAttributes(searchControls.getReturningAttributes());
        } else {
            request.setReturnAttributes(ReturnAttributes.ALL_USER.value());
        }

        var searchScope = SearchScope.SUBTREE;
        for (val scope : SearchScope.values()) {
            if (scope.ordinal() == searchControls.getSearchScope()) {
                searchScope = scope;
            }
        }
        request.setSearchScope(searchScope);
        request.setSizeLimit(Long.valueOf(searchControls.getCountLimit()).intValue());
        request.setTimeLimit(Duration.ofSeconds(searchControls.getTimeLimit()));
        return request;
    }

    protected Map<String, List<Object>> convertLdapEntryToMap(final LdapEntry entry) {
        val attributeMap = new LinkedHashMap<String, List<Object>>(entry.size());
        for (val attr : entry.getAttributes()) {
            attributeMap.put(attr.getName(), new ArrayList<>(attr.getStringValues()));
        }
        LOGGER.debug("Converted LDAP DN entry [{}] to attribute map [{}]", entry.getDn(), attributeMap);
        return attributeMap;
    }

    @Override
    public void close() {
        if (connectionFactory != null) {
            connectionFactory.close();
        }
    }
}
