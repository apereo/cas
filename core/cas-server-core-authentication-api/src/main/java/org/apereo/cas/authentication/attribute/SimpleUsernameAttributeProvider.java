package org.apereo.cas.authentication.attribute;

import module java.base;
import org.apereo.cas.authentication.principal.attribute.PersonAttributeDao;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

/**
 * Provides the username attribute based on a pre-configured string. Determines the username from a query Map based
 * on the configured attribute, {@link StringUtils#trimToNull(String)}, and if the username value does not contain a
 * wildcard.
 *
 * @author Eric Dalquist
 * @since 7.1.0
 */
@Slf4j
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleUsernameAttributeProvider implements UsernameAttributeProvider {
    private static final String DEFAULT_USERNAME_ATTRIBUTE = "username";

    private String usernameAttribute = DEFAULT_USERNAME_ATTRIBUTE;
    
    @Override
    public @Nullable String getUsernameFromQuery(final Map<String, List<Object>> query) {
        val usernameAttributeValues = getUsernameAttributeValues(query);
        LOGGER.debug("Username attribute value found from the query map is [{}]", usernameAttributeValues);
        
        if (usernameAttributeValues == null || usernameAttributeValues.isEmpty()) {
            return null;
        }

        val firstValue = usernameAttributeValues.getFirst();
        if (firstValue == null) {
            return null;
        }

        val username = StringUtils.trimToNull(String.valueOf(firstValue));
        if (username == null || username.contains(PersonAttributeDao.WILDCARD)) {
            return null;
        }

        return username;
    }

    private @Nullable List<Object> getUsernameAttributeValues(final Map<String, List<Object>> query) {
        if (query.containsKey(this.usernameAttribute)) {
            val usernameAttributeValues = query.get(this.usernameAttribute);
            LOGGER.debug("Using [{}] attribute to get username from the query map", this.usernameAttribute);
            return usernameAttributeValues;
        }
        val usernameAttributeValues = query.get(DEFAULT_USERNAME_ATTRIBUTE);
        LOGGER.debug("Using [{}] attribute to get username from the query map", DEFAULT_USERNAME_ATTRIBUTE);
        return usernameAttributeValues;
    }
}
