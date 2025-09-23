package org.apereo.cas.authentication.attribute;

import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Base {@link PersonAttributeDao} that provides implementations of the deprecated methods.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
@ToString
@EqualsAndHashCode
public abstract class BasePersonAttributeDao implements PersonAttributeDao {

    @Getter
    @Setter
    private int order;

    @Getter
    private String[] id = {getClass().getSimpleName()};

    @Getter
    @Setter
    private boolean enabled = true;

    @Getter
    @Setter
    private Map<String, Object> tags = new LinkedCaseInsensitiveMap<>();

    @Override
    public int compareTo(final PersonAttributeDao o) {
        return Integer.compare(this.order, o.getOrder());
    }

    public void setId(final String... id) {
        this.id = id;
    }
    
    protected PersonAttributes getSinglePerson(final Set<PersonAttributes> people) {
        try {
            return DataAccessUtils.singleResult(people);
        } catch (final IncorrectResultSizeDataAccessException e) {
            LOGGER.warn("Unexpected multiple people returned from person attribute DAO: [{}] : [{}]", e.getClass().getName(), e.getMessage());
            people.forEach(p -> LOGGER.debug("Person: [{}]", p));
            throw e;
        }
    }

    protected Map<String, List<Object>> toMultivaluedMap(final Map<String, Object> seed) {
        val multiSeed = new LinkedCaseInsensitiveMap<List<Object>>(seed.size());
        for (val seedEntry : seed.entrySet()) {
            val seedName = seedEntry.getKey();
            val seedValue = seedEntry.getValue();
            if (seedValue instanceof final List list) {
                multiSeed.put(seedName, list);
            } else if (seedValue != null) {
                multiSeed.put(seedName, List.of(seedValue));
            }
        }
        return multiSeed;
    }

    /**
     * Translate from a more flexible Attribute to Attribute mapping format to a Map
     * from String to Set of Strings.
     * <p>
     * The point of the map is to map from attribute names in the underlying data store
     * (e.g., JDBC column names, LDAP attribute names) to uPortal attribute names.
     * Any given underlying data store attribute might map to zero uPortal
     * attributes (not appear in the map at all), map to exactly one uPortal attribute
     * (appear in the Map as a mapping from a String to a String or as a mapping
     * from a String to a Set containing just one String), or map to several uPortal
     * attribute names (appear in the Map as a mapping from a String to a Set
     * of Strings).
     * <p>
     * This method takes as its argument a {@link Map} that must have keys of
     * type {@link String} and values of type {@link String} or {@link Set} of
     * {@link String}s.  The argument must not be null and must have no null
     * keys.  It must contain no keys other than Strings and no values other
     * than Strings or Sets of Strings.  This method will convert any non-string
     * values to a String using the object's toString() method.
     * <p>
     * This method returns a Map equivalent to its argument except wherever there
     * was a String value in the Map there will instead be an immutable Set containing
     * the String value.  That is, the return value is normalized to be a Map from
     * String to Set (of String).
     *
     * @param mapping {@link Map} from String names of attributes in the underlying store
     *                to attribute names or Sets of such names.
     * @return a Map from String to Set of Strings
     */
    public static Map<String, Set<String>> parseAttributeToAttributeMapping(final Map<String, ?> mapping) {
        val mappedAttributesBuilder = new LinkedCaseInsensitiveMap<Set<String>>();
        for (val mappingEntry : mapping.entrySet()) {
            val sourceAttrName = mappingEntry.getKey();
            val mappedAttribute = mappingEntry.getValue();
            switch (mappedAttribute) {
                case null -> mappedAttributesBuilder.put(sourceAttrName, null);
                case final String value -> {
                    val mappedSet = new HashSet<String>();
                    mappedSet.add(mappedAttribute.toString());
                    mappedAttributesBuilder.put(sourceAttrName, mappedSet);
                }
                case final Collection sourceSet -> {
                    val mappedSet = new LinkedHashSet<String>();
                    for (val sourceObj : sourceSet) {
                        if (sourceObj != null) {
                            mappedSet.add(sourceObj.toString());
                        } else {
                            mappedSet.add(null);
                        }
                    }
                    mappedAttributesBuilder.put(sourceAttrName, mappedSet);
                }
                default -> throw new IllegalArgumentException("Invalid mapped type. key=" + sourceAttrName + ", value type=" + mappedAttribute.getClass().getName());
            }
        }

        return new HashMap<>(mappedAttributesBuilder);
    }

    protected <T> Collection<T> flattenCollection(final Collection<?> source) {
        val result = new ArrayList<T>();
        for (val value : source) {
            if (value instanceof Collection) {
                val flatCollection = flattenCollection((Collection<Object>) value);
                result.addAll((Collection<T>) flatCollection);
            } else {
                result.add((T) value);
            }
        }
        return result;
    }

    protected Pattern compilePattern(final String queryString) {
        var queryBuilder = new StringBuilder();
        var queryMatcher = PersonAttributeDao.WILDCARD_PATTERN.matcher(queryString);
        if (!queryMatcher.find()) {
            return Pattern.compile(Pattern.quote(queryString));
        }

        var start = queryMatcher.start();
        var previousEnd = -1;
        if (start > 0) {
            var queryPart = queryString.substring(0, start);
            var quotedQueryPart = Pattern.quote(queryPart);
            queryBuilder.append(quotedQueryPart);
        }
        queryBuilder.append(".*");

        do {
            start = queryMatcher.start();

            if (previousEnd != -1) {
                var queryPart = queryString.substring(previousEnd, start);
                var quotedQueryPart = Pattern.quote(queryPart);
                queryBuilder.append(quotedQueryPart);
                queryBuilder.append(".*");
            }

            previousEnd = queryMatcher.end();
        } while (queryMatcher.find());

        if (previousEnd < queryString.length()) {
            var queryPart = queryString.substring(previousEnd);
            var quotedQueryPart = Pattern.quote(queryPart);
            queryBuilder.append(quotedQueryPart);
        }

        return Pattern.compile(queryBuilder.toString());
    }
    
}
