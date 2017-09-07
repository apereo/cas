package org.apereo.cas.consent;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link ConsentDecisionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ConsentDecisionBuilder {
    private final CipherExecutor<Serializable, String> consentCipherExecutor;

    public ConsentDecisionBuilder(final CipherExecutor consentCipherExecutor) {
        this.consentCipherExecutor = consentCipherExecutor;
    }

    /**
     * Update consent decision.
     *
     * @param consent    the consent
     * @param attributes the attributes
     * @return the consent decision
     */
    public ConsentDecision update(final ConsentDecision consent, final Map<String, Object> attributes) {
        final String encodedNames = buildAndEncodeConsentAttributeNames(attributes);
        consent.setAttributeNames(encodedNames);

        final String encodedValues = buildAndEncodeConsentAttributeValues(attributes);
        consent.setAttributeValues(encodedValues);

        consent.setCreatedDate(LocalDateTime.now());
        return consent;
    }
    
    /**
     * Build consent decision consent decision.
     *
     * @param service           the service
     * @param registeredService the registered service
     * @param principalId       the principal id
     * @param attributes        the attributes
     * @return the consent decision
     */
    public ConsentDecision build(final Service service,
                                 final RegisteredService registeredService,
                                 final String principalId,
                                 final Map<String, Object> attributes) {
        final ConsentDecision consent = new ConsentDecision();
        consent.setPrincipal(principalId);
        consent.setService(service.getId());
        return update(consent, attributes);
    }

    /**
     * Build consent attributes pair and encoded it.
     *
     * @param attributes the attributes
     * @return the pair
     */
    private Pair<String, String> buildAndEncodeConsentAttributes(final Map<String, Object> attributes) {
        return Pair.of(buildAndEncodeConsentAttributeNames(attributes), buildAndEncodeConsentAttributeValues(attributes));
    }

    /**
     * Build consent attributes pair.
     *
     * @param attributes the attributes
     * @return the pair
     */
    private Pair<String, String> buildConsentAttributes(final Map<String, Object> attributes) {
        return Pair.of(buildConsentAttributeNames(attributes), buildConsentAttributeValues(attributes));
    }

    /**
     * Is consent decision valid for attributes boolean.
     *
     * @param decision   the decision
     * @param attributes the attributes
     * @return the boolean
     */
    public boolean doesAttributeReleaseRequireConsent(final ConsentDecision decision,
                                                      final Map<String, Object> attributes) {
        switch (decision.getOptions()) {
            case ALWAYS:
                return true;
            case ATTRIBUTE_NAME:
                final String consentAttributesHash = buildConsentAttributeNames(attributes);
                final String decodedNames = this.consentCipherExecutor.decode(decision.getAttributeNames());
                return !StringUtils.equals(consentAttributesHash, decodedNames);
            case ATTRIBUTE_VALUE:
                final Pair<String, String> pair = buildConsentAttributes(attributes);
                final String decNames = this.consentCipherExecutor.decode(decision.getAttributeNames());
                final String decValues = this.consentCipherExecutor.decode(decision.getAttributeValues());
                return !StringUtils.equals(decNames, pair.getKey())
                        || !StringUtils.equals(decValues, pair.getValue());
            default:
                return false;
        }
    }

    /**
     * Build consent attribute values string.
     *
     * @param attributes the attributes
     * @return the string
     */
    private String buildAndEncodeConsentAttributeValues(final Map<String, Object> attributes) {
        final String values = buildConsentAttributeValues(attributes);
        return this.consentCipherExecutor.encode(values);
    }

    private String buildConsentAttributeValues(final Map<String, Object> attributes) {
        final String allValues = attributes.values().stream()
                .map(CollectionUtils::toCollection)
                .map(c -> c.stream().map(Object::toString).collect(Collectors.joining()))
                .collect(Collectors.joining("|"));
        return DigestUtils.sha512(allValues);
    }

    /**
     * Build consent attribute names string.
     *
     * @param attributes the attributes
     * @return the string
     */
    private String buildAndEncodeConsentAttributeNames(final Map<String, Object> attributes) {
        final String names = buildConsentAttributeNames(attributes);
        return this.consentCipherExecutor.encode(names);
    }

    private String buildConsentAttributeNames(final Map<String, Object> attributes) {
        final String allNames = attributes.keySet()
                .stream()
                .collect(Collectors.joining("|"));
        return DigestUtils.sha512(allNames);
    }
}
