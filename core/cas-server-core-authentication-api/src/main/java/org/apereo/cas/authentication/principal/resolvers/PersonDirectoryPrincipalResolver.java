package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.attribute.PrincipalAttributeRepositoryFetcher;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.util.CollectionUtils;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.services.persondir.IPersonAttributeDao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.*;


/**
 * Resolves principals by querying a data source using the Person Directory API.
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

    private final PrincipalResolutionContext context;

    @Override
    public Principal resolve(final Credential credential, final Optional<Principal> currentPrincipal,
                             final Optional<AuthenticationHandler> handler) {

        LOGGER.trace("Attempting to resolve a principal via [{}]", getName());
        var principalId = extractPrincipalId(credential, currentPrincipal);
        if (StringUtils.isBlank(principalId)) {
            LOGGER.debug("Principal id [{}] could not be found", principalId);
            return null;
        }
        if (context.getPrincipalNameTransformer() != null) {
            principalId = context.getPrincipalNameTransformer().transform(principalId);
        }
        LOGGER.trace("Creating principal for [{}]", principalId);
        if (context.isResolveAttributes()) {
            val attributes = retrievePersonAttributes(principalId, credential, currentPrincipal, new HashMap<>(0));
            if (attributes == null || attributes.isEmpty()) {
                LOGGER.debug("Principal id [{}] did not specify any attributes", principalId);
                if (!context.isReturnNullIfNoAttributes()) {
                    val principal = buildResolvedPrincipal(principalId, new HashMap<>(0), credential, currentPrincipal, handler);
                    LOGGER.debug("Returning the principal with id [{}] without any attributes", principal);
                    return principal;
                }
                LOGGER.debug("[{}] is configured to return null if no attributes are found for [{}]", getClass().getName(), principalId);
                return null;
            }
            LOGGER.debug("Retrieved [{}] attribute(s) from the repository", attributes.size());
            val result = convertPersonAttributesToPrincipal(principalId, currentPrincipal, attributes);
            if (!result.isSuccess() && context.isReturnNullIfNoAttributes()) {
                LOGGER.warn("Principal resolution is unable to produce a result and will return null");
                return null;
            }

            val principal = buildResolvedPrincipal(result.getPrincipalId(), result.getAttributes(),
                credential, currentPrincipal, handler);
            LOGGER.debug("Final resolved principal by [{}] is [{}]", getName(), principal);
            return principal;
        }
        val principal = buildResolvedPrincipal(principalId, new HashMap<>(0),
            credential, currentPrincipal, handler);
        LOGGER.debug("Final resolved principal by [{}] without resolving attributes is [{}]", getName(), principal);
        return principal;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && credential.getId() != null;
    }

    @Override
    public IPersonAttributeDao getAttributeRepository() {
        return context.getAttributeRepository();
    }

    /**
     * Build resolved principal.
     *
     * @param id               the id
     * @param attributes       the attributes
     * @param credential       the credential
     * @param currentPrincipal the current principal
     * @param handler          the handler
     * @return the principal
     */
    protected Principal buildResolvedPrincipal(final String id, final Map<String, List<Object>> attributes,
                                               final Credential credential, final Optional<Principal> currentPrincipal,
                                               final Optional<AuthenticationHandler> handler) {
        return context.getPrincipalFactory().createPrincipal(id, attributes);
    }

    /**
     * Convert person attributes to principal pair.
     *
     * @param extractedPrincipalId the extracted principal id
     * @param currentPrincipal     the current principal
     * @param attributes           the attributes
     * @return the pair
     */
    @SuppressWarnings("unchecked")
    protected PrincipalResolutionResult convertPersonAttributesToPrincipal(final String extractedPrincipalId,
                                                                           final Optional<Principal> currentPrincipal,
                                                                           final Map<String, List<Object>> attributes) {
        val convertedAttributes = new LinkedHashMap<String, List<Object>>();
        attributes.forEach((key, attrValue) -> {
            val values = ((List<Object>) CollectionUtils.toCollection(attrValue, ArrayList.class))
                .stream()
                .filter(Objects::nonNull)
                .collect(toList());
            LOGGER.debug("Found attribute [{}] with value(s) [{}]", key, values);
            convertedAttributes.put(key, values);
        });

        val builder = PrincipalResolutionResult.builder();
        var principalId = extractedPrincipalId;

        if (StringUtils.isNotBlank(context.getPrincipalAttributeNames())) {
            val attrNames = org.springframework.util.StringUtils.commaDelimitedListToSet(context.getPrincipalAttributeNames());

            val principalIdAttributes = new LinkedHashMap<>(attributes);
            if (context.isUseCurrentPrincipalId() && currentPrincipal.isPresent()) {
                val currentPrincipalAttributes = currentPrincipal.get().getAttributes();
                LOGGER.trace("Merging current principal attributes [{}] with resolved attributes [{}]",
                    currentPrincipalAttributes, principalIdAttributes);
                context.getAttributeMerger().mergeAttributes(principalIdAttributes, currentPrincipalAttributes);
            }

            LOGGER.debug("Using principal attributes [{}] to determine principal id", principalIdAttributes);
            val result = attrNames.stream()
                .map(String::trim)
                .filter(principalIdAttributes::containsKey)
                .map(principalIdAttributes::get)
                .findFirst();

            if (result.isEmpty()) {
                LOGGER.warn("Principal resolution is set to resolve users via attribute(s) [{}], and yet "
                        + "the collection of attributes retrieved [{}] do not contain any of those attributes. This is "
                        + "likely due to misconfiguration and CAS will use [{}] as the final principal id",
                    context.getPrincipalAttributeNames(), principalIdAttributes.keySet(), principalId);
                builder.success(false);
            } else {
                val values = result.get();
                if (!values.isEmpty()) {
                    principalId = CollectionUtils.firstElement(values).map(Object::toString).orElseThrow();
                    LOGGER.debug("Found principal id attribute value [{}]", principalId);
                }
            }
        }
        return builder.principalId(principalId).attributes(convertedAttributes).build();
    }

    /**
     * Retrieve person attributes as a map.
     *
     * @param principalId      the principal id
     * @param credential       the credential whose id we have extracted.
     * @param currentPrincipal the current principal
     * @param queryAttributes  the query attributes
     * @return the map
     */
    protected Map<String, List<Object>> retrievePersonAttributes(final String principalId, final Credential credential,
                                                                 final Optional<Principal> currentPrincipal,
                                                                 final Map<String, List<Object>> queryAttributes) {

        queryAttributes.putIfAbsent("credentialId", CollectionUtils.wrapList(credential.getId()));
        queryAttributes.putIfAbsent("credentialClass", CollectionUtils.wrapList(credential.getClass().getSimpleName()));
        return PrincipalAttributeRepositoryFetcher.builder()
            .attributeRepository(context.getAttributeRepository())
            .principalId(principalId)
            .activeAttributeRepositoryIdentifiers(context.getActiveAttributeRepositoryIdentifiers())
            .currentPrincipal(currentPrincipal.orElse(null))
            .queryAttributes(queryAttributes)
            .build()
            .retrieve();
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
        val id = credential.getId();
        if (currentPrincipal.isPresent()) {
            val principal = currentPrincipal.get();
            LOGGER.debug("Principal is currently resolved as [{}]", principal);
            if (context.isUseCurrentPrincipalId()) {
                LOGGER.debug("Using the existing resolved principal id [{}]", principal.getId());
                return principal.getId();
            } else {
                LOGGER.debug("CAS will NOT be using the identifier from the resolved principal [{}] as it's not "
                    + "configured to use the currently-resolved principal id and will fall back onto using the identifier "
                    + "for the credential, that is [{}], for principal resolution", principal, id);
            }
        } else {
            LOGGER.debug("No principal is currently resolved and available. Falling back onto using the identifier "
                + " for the credential, that is [{}], for principal resolution", id);
        }
        LOGGER.debug("Extracted principal id [{}]", id);
        return StringUtils.isNotBlank(id) ? id.trim() : null;
    }

    @SuperBuilder
    @Getter
    static class PrincipalResolutionResult {
        private final String principalId;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new HashMap<>();

        @Builder.Default
        private final boolean success = true;
    }
}
