package org.apereo.cas.consent;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultConsentDecisionBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultConsentDecisionBuilder implements ConsentDecisionBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConsentDecisionBuilder.class);
    
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final CipherExecutor<Serializable, String> consentCipherExecutor;

    public DefaultConsentDecisionBuilder(final CipherExecutor consentCipherExecutor) {
        this.consentCipherExecutor = consentCipherExecutor;
    }

    @Override
    public ConsentDecision update(final ConsentDecision consent, final Map<String, Object> attributes) {
        final String encodedNames = buildAndEncodeConsentAttributes(attributes);
        consent.setAttributes(encodedNames);
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
        final Map<String, Object> consentAttributes = getConsentableAttributesFrom(decision);

        if (decision.getOptions() == ConsentOptions.ATTRIBUTE_NAME) {
            final String consentAttributesHash = sha512ConsentAttributeNames(consentAttributes);
            final String currentAttributesHash = sha512ConsentAttributeNames(attributes);
            return !StringUtils.equals(consentAttributesHash, currentAttributesHash);
        }
        
        if (decision.getOptions() == ConsentOptions.ATTRIBUTE_VALUE) {
            final String consentAttributesHash = sha512ConsentAttributeNames(consentAttributes);
            final String currentAttributesHash = sha512ConsentAttributeNames(attributes);

            final String consentAttributeValuesHash = sha512ConsentAttributeValues(consentAttributes);
            final String currentAttributeValuesHash = sha512ConsentAttributeValues(attributes);
            
            return !StringUtils.equals(consentAttributesHash, currentAttributesHash) 
                    || !StringUtils.equals(consentAttributeValuesHash, currentAttributeValuesHash);
        }
        return true;
    }

    @Override
    public Map<String, Object> getConsentableAttributesFrom(final ConsentDecision decision) {
        try {
            final String result = this.consentCipherExecutor.decode(decision.getAttributes());
            if (StringUtils.isBlank(result)) {
                LOGGER.warn("Unable to decipher attributes from consent decision [{}]", decision.getId());
                return new HashMap<>(0);
            }
            final String names = EncodingUtils.decodeBase64ToString(result);
            final Map<String, Object> attributes = MAPPER.readValue(names, Map.class);
            return attributes;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not serialize attributes for consent decision");
        }
    }

    private String sha512ConsentAttributeNames(final Map<String, Object> attributes) {
        final String allNames = attributes.keySet().stream().collect(Collectors.joining("|"));
        return DigestUtils.sha512(allNames);
    }

    private String sha512ConsentAttributeValues(final Map<String, Object> attributes) {
        final String allValues = attributes.values().stream()
                .map(CollectionUtils::toCollection)
                .map(c -> c.stream().map(Object::toString).collect(Collectors.joining()))
                .collect(Collectors.joining("|"));
        final String attributeValues = DigestUtils.sha512(allValues);
        return attributeValues;
    }

    /**
     * Build consent attribute names string.
     *
     * @param attributes the attributes
     * @return the string
     */
    protected String buildAndEncodeConsentAttributes(final Map<String, Object> attributes) {
        try {
            final String json = MAPPER.writer(new MinimalPrettyPrinter()).writeValueAsString(attributes);
            final String base64 = EncodingUtils.encodeBase64(json);
            return this.consentCipherExecutor.encode(base64);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not serialize attributes for consent decision");
        }
    }
}
