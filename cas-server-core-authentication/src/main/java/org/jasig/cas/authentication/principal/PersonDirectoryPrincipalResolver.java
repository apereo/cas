package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.util.Pair;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.StubPersonAttributeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves principals by querying a data source using the Jasig
 * <a href="http://developer.jasig.org/projects/person-directory/1.5.0-SNAPSHOT/apidocs/">Person Directory API</a>.
 * The {@link org.jasig.cas.authentication.principal.Principal#getAttributes()} are populated by the results of the
 * query and the principal ID may optionally be set by proving an attribute whose first non-null value is used;
 * otherwise the credential ID is used for the principal ID.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("personDirectoryPrincipalResolver")
public class PersonDirectoryPrincipalResolver implements PrincipalResolver {

    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Repository of principal attributes to be retrieved.
     */
    @NotNull
    protected IPersonAttributeDao attributeRepository = new StubPersonAttributeDao(new HashMap<String, List<Object>>());

    /**
     * Factory to create the principal type.
     **/
    @NotNull
    protected PrincipalFactory principalFactory = new DefaultPrincipalFactory();

    /**
     * return null if no attributes are found.
     */
    @Value("${cas.principal.resolver.persondir.return.null:false}")
    protected boolean returnNullIfNoAttributes;

    /**
     * Optional principal attribute name.
     */
    protected String principalAttributeName;

    @Autowired
    public final void setAttributeRepository(@Qualifier("attributeRepository")
                                             final IPersonAttributeDao attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    public void setReturnNullIfNoAttributes(final boolean returnNullIfNoAttributes) {
        this.returnNullIfNoAttributes = returnNullIfNoAttributes;
    }

    /**
     * Sets the name of the attribute whose first non-null value should be used for the principal ID.
     *
     * @param attribute Name of attribute containing principal ID.
     */
    @Autowired
    public void setPrincipalAttributeName(@Value("${cas.principal.resolver.persondir.principal.attribute:}")
                                          final String attribute) {
        this.principalAttributeName = attribute;
    }

    /**
     * Sets principal factory to create principal objects.
     *
     * @param principalFactory the principal factory
     */
    @Autowired
    public void setPrincipalFactory(@Qualifier("principalFactory") final PrincipalFactory principalFactory) {
        this.principalFactory = principalFactory;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && credential.getId() != null;
    }

    @Override
    public Principal resolve(final Credential credential) {
        logger.debug("Attempting to resolve a principal...");

        final String principalId = extractPrincipalId(credential);

        if (principalId == null) {
            logger.debug("Got null for extracted principal ID; returning null.");
            return null;
        }

        logger.debug("Creating SimplePrincipal for [{}]", principalId);

        final Map<String, List<Object>> attributes = retrievePersonAttributes(principalId, credential);
        logger.debug("Principal id [{}] could not be found", principalId);

        if (attributes == null || attributes.isEmpty()) {
            logger.debug("Principal id [{}] did not specify any attributes", principalId);

            if (!this.returnNullIfNoAttributes) {
                logger.debug("Returning the principal with id [{}] without any attributes", principalId);
                return this.principalFactory.createPrincipal(principalId);
            }
            logger.debug("[{}] is configured to return null if no attributes are found for [{}]",
                    this.getClass().getName(), principalId);
            return null;
        }
        logger.debug("Retrieved [{}] attribute(s) from the repository", attributes.size());


        final Pair<String, Map<String, Object>> pair = convertPersonAttributesToPrincipal(principalId, attributes);
        return this.principalFactory.createPrincipal(pair.getFirst(), pair.getSecond());
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
        final Map<String, Object> convertedAttributes = new HashMap<>();
        String principalId = extractedPrincipalId;
        for (final Map.Entry<String, List<Object>> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            final List<Object> values = entry.getValue();
            if (StringUtils.isNotBlank(this.principalAttributeName)
                    && key.equalsIgnoreCase(this.principalAttributeName)) {
                if (values.isEmpty()) {
                    logger.debug("{} is empty, using {} for principal", this.principalAttributeName, extractedPrincipalId);
                } else {
                    principalId = values.get(0).toString();
                    logger.debug(
                            "Found principal attribute value {}; removing {} from attribute map.",
                            extractedPrincipalId,
                            this.principalAttributeName);
                }
            } else {
                convertedAttributes.put(key, values.size() == 1 ? values.get(0) : values);
            }
        }

        return new Pair<>(principalId, convertedAttributes);
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
        final IPersonAttributes personAttributes = this.attributeRepository.getPerson(principalId);
        final Map<String, List<Object>> attributes;

        if (personAttributes == null) {
            attributes = null;
        } else {
            attributes = personAttributes.getAttributes();
        }
        return attributes;
    }


    /**
     * Extracts the id of the user from the provided credential. This method should be overridden by subclasses to
     * achieve more sophisticated strategies for producing a principal ID from a credential.
     *
     * @param credential the credential provided by the user.
     * @return the username, or null if it could not be resolved.
     */
    protected String extractPrincipalId(final Credential credential) {
        return credential.getId();
    }
}
