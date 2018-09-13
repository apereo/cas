package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalFactoryUtils;
import org.apereo.cas.authentication.principal.PrincipalResolver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.StubPersonAttributeDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves principals by querying a data source using the Jasig
 * <a href="http://developer.jasig.org/projects/person-directory/1.5.0-SNAPSHOT/apidocs/">Person Directory API</a>.
 * The {@link Principal#getAttributes()} are populated by the results of the
 * query and the principal ID may optionally be set by proving an attribute whose first non-null value is used;
 * otherwise the credential ID is used for the principal ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
@ToString
@RequiredArgsConstructor
@Getter
@Setter
public class PersonDirectoryPrincipalResolver implements PrincipalResolver {

    /**
     * Repository of principal attributes to be retrieved.
     */
    protected final IPersonAttributeDao attributeRepository;

    /**
     * Factory to create the principal type.
     **/
    protected final PrincipalFactory principalFactory;

    /**
     * return null if no attributes are found.
     */
    protected final boolean returnNullIfNoAttributes;

    /**
     * Transform principal name.
     */
    protected final PrincipalNameTransformer principalNameTransformer;

    /**
     * Optional principal attribute name.
     */
    protected final String principalAttributeName;

    /**
     * Use the current principal id for extraction.
     */
    protected boolean useCurrentPrincipalId;

    public PersonDirectoryPrincipalResolver() {
        this(new StubPersonAttributeDao(new HashMap<>()), PrincipalFactoryUtils.newPrincipalFactory(), false,
            String::trim, null);
    }

    public PersonDirectoryPrincipalResolver(final IPersonAttributeDao attributeRepository, final String principalAttributeName) {
        this(attributeRepository, PrincipalFactoryUtils.newPrincipalFactory(), false, formUserId -> formUserId, principalAttributeName);
    }

    public PersonDirectoryPrincipalResolver(final IPersonAttributeDao attributeRepository) {
        this(attributeRepository, PrincipalFactoryUtils.newPrincipalFactory(), false, formUserId -> formUserId, null);
    }

    public PersonDirectoryPrincipalResolver(final boolean returnNullIfNoAttributes, final String principalAttributeName) {
        this(new StubPersonAttributeDao(new HashMap<>()), PrincipalFactoryUtils.newPrincipalFactory(),
            returnNullIfNoAttributes, String::trim, principalAttributeName);
    }

    public PersonDirectoryPrincipalResolver(final IPersonAttributeDao attributeRepository,
                                            final PrincipalFactory principalFactory, final boolean returnNullIfNoAttributes,
                                            final String principalAttributeName) {
        this(attributeRepository, principalFactory, returnNullIfNoAttributes, formUserId -> formUserId, principalAttributeName);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && credential.getId() != null;
    }

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal, final Optional<AuthenticationHandler> handler) {
        LOGGER.debug("Attempting to resolve a principal...");
        String principalId = extractPrincipalId(credential, currentPrincipal);
        if (StringUtils.isBlank(principalId)) {
            LOGGER.debug("Principal id [{}] could not be found", principalId);
            return null;
        }
        if (principalNameTransformer != null) {
            principalId = principalNameTransformer.transform(principalId);
        }
        if (StringUtils.isBlank(principalId)) {
            LOGGER.debug("Principal id [{}] could not be found", principalId);
            return null;
        }
        LOGGER.debug("Creating principal for [{}]", principalId);
        final Map<String, List<Object>> attributes = retrievePersonAttributes(principalId, credential);
        if (attributes == null || attributes.isEmpty()) {
            LOGGER.debug("Principal id [{}] did not specify any attributes", principalId);
            if (!this.returnNullIfNoAttributes) {
                LOGGER.debug("Returning the principal with id [{}] without any attributes", principalId);
                return this.principalFactory.createPrincipal(principalId);
            }
            LOGGER.debug("[{}] is configured to return null if no attributes are found for [{}]", this.getClass().getName(), principalId);
            return null;
        }
        LOGGER.debug("Retrieved [{}] attribute(s) from the repository", attributes.size());
        final Pair<String, Map<String, Object>> pair = convertPersonAttributesToPrincipal(principalId, attributes);
        return this.principalFactory.createPrincipal(pair.getKey(), pair.getValue());
    }

    /**
     * Convert person attributes to principal pair.
     *
     * @param extractedPrincipalId the extracted principal id
     * @param attributes           the attributes
     * @return the pair
     */
    protected Pair<String, Map<String, Object>> convertPersonAttributesToPrincipal(final String extractedPrincipalId,
                                                                                   final Map<String, List<Object>> attributes) {
        final String[] principalId = {extractedPrincipalId};
        final Map<String, Object> convertedAttributes = new HashMap<>();
        attributes.entrySet().forEach(entry -> {
            final String key = entry.getKey();
            LOGGER.debug("Found attribute [{}]", key);
            final List<Object> values = entry.getValue();
            if (StringUtils.isNotBlank(this.principalAttributeName) && key.equalsIgnoreCase(this.principalAttributeName)) {
                if (values.isEmpty()) {
                    LOGGER.debug("[{}] is empty, using [{}] for principal", this.principalAttributeName, extractedPrincipalId);
                } else {
                    principalId[0] = values.get(0).toString();
                    LOGGER.debug("Found principal attribute value [{}]; removing [{}] from attribute map.", extractedPrincipalId, this.principalAttributeName);
                }
            } else {
                convertedAttributes.put(key, values.size() == 1 ? values.get(0) : values);
            }
        });
        return Pair.of(principalId[0], convertedAttributes);
    }

    /**
     * Retrieve person attributes map.
     *
     * @param principalId the principal id
     * @param credential  the credential whose id we have extracted. This is passed so that implementations
     *                    can extract useful bits of authN info such as attributes into the principal.
     * @return the map
     */
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential) {
        LOGGER.debug("Retrieving person attributes for principal id [{}]", principalId);
        final IPersonAttributes personAttributes = this.attributeRepository.getPerson(principalId);
        final Map<String, List<Object>> attributes;
        if (personAttributes == null) {
            attributes = null;
        } else {
            attributes = personAttributes.getAttributes();
        }
        LOGGER.debug("Found person attributes [{}] for principal id [{}]", attributes, principalId);
        return attributes;
    }

    /**
     * Extracts the id of the user from the provided credential. This method should be overridden by subclasses to
     * achieve more sophisticated strategies for producing a principal ID from a credential.
     *
     * @param credential       the credential provided by the user.
     * @param currentPrincipal the current principal
     * @return the username, or null if it could not be resolved.
     */
    protected String extractPrincipalId(final Credential credential, final Optional<Principal> currentPrincipal) {
        LOGGER.debug("Extracting credential id based on existing credential [{}]", credential);
        if (currentPrincipal != null && currentPrincipal.isPresent()) {
            final Principal principal = currentPrincipal.get();
            LOGGER.debug("Principal is currently resolved is [{}]", principal);
            if (useCurrentPrincipalId) {
                LOGGER.debug("Using the existing resolved principal id [{}]", principal.getId());
                return principal.getId();
            }
        }
        final String id = credential.getId();
        LOGGER.debug("Extracted principal id [{}]", id);
        return id;
    }

}
