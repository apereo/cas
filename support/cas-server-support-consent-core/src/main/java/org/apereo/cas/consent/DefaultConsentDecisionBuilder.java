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
 * This is {@link DefaultConsentDecisionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultConsentDecisionBuilder implements ConsentDecisionBuilder {
    private final CipherExecutor<Serializable, String> consentCipherExecutor;

    public DefaultConsentDecisionBuilder(final CipherExecutor consentCipherExecutor) {
        this.consentCipherExecutor = consentCipherExecutor;
    }

    @Override
    public ConsentDecision update(final ConsentDecision consent, final Map<String, Object> attributes) {
        final String encodedNames = buildAndEncodeConsentAttributeNames(attributes);
        consent.setAttributeNames(encodedNames);
        final String encodedValues = buildAndEncodeConsentAttributeValues(attributes);
        consent.setAttributeValues(encodedValues);
        consent.setCreatedDate(LocalDateTime.now());
        return consent;
    }

    @Override
    public ConsentDecision build(final Service service,
                                 final RegisteredService registeredService,
                                 final String principalId,
                                 final Map<String, Object> attributes) {
        final ConsentDecision consent = new ConsentDecision();
        consent.setPrincipal(principalId);
        consent.setService(service.getId());
        return update(consent, attributes);
    }

    @Override
    public boolean doesAttributeReleaseRequireConsent(final ConsentDecision decision,
                                                      final Map<String, Object> attributes) {
        if (decision.getOptions() == ConsentOptions.ATTRIBUTE_NAME) {
            final String consentAttributesHash = buildConsentAttributeNames(attributes);
            final String decodedNames = this.consentCipherExecutor.decode(decision.getAttributeNames());
            return !StringUtils.equals(consentAttributesHash, decodedNames);
        }
        if (decision.getOptions() == ConsentOptions.ATTRIBUTE_VALUE) {
            final Pair<String, String> pair = buildConsentAttributes(attributes);
            final String decNames = this.consentCipherExecutor.decode(decision.getAttributeNames());
            final String decValues = this.consentCipherExecutor.decode(decision.getAttributeValues());
            return !StringUtils.equals(decNames, pair.getKey()) || !StringUtils.equals(decValues, pair.getValue());
        }
        return true;
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
     * Build consent attribute values string.
     *
     * @param attributes the attributes
     * @return the string
     */
    private String buildAndEncodeConsentAttributeValues(final Map<String, Object> attributes) {
        final String values = buildConsentAttributeValues(attributes);
        return this.consentCipherExecutor.encode(values);
    }

    /**
     * Build consent attribute values.
     *
     * @param attributes the attributes
     * @return the string
     */
    protected String buildConsentAttributeValues(final Map<String, Object> attributes) {
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
    protected String buildAndEncodeConsentAttributeNames(final Map<String, Object> attributes) {
        final String names = buildConsentAttributeNames(attributes);
        return this.consentCipherExecutor.encode(names);
    }

    /**
     * Build consent attribute names.
     *
     * @param attributes the attributes
     * @return the string
     */
    protected String buildConsentAttributeNames(final Map<String, Object> attributes) {
        final String allNames = attributes.keySet()
                .stream()
                .collect(Collectors.joining("|"));
        return DigestUtils.sha512(allNames);
    }
}
