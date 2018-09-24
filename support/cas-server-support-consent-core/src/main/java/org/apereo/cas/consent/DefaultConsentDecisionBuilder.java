package org.apereo.cas.consent;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

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
@Slf4j
@RequiredArgsConstructor
public class DefaultConsentDecisionBuilder implements ConsentDecisionBuilder {
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final long serialVersionUID = 8220243983483982326L;

    private final transient CipherExecutor<Serializable, String> consentCipherExecutor;

    @Override
    public ConsentDecision update(final ConsentDecision consent, final Map<String, Object> attributes) {
        val encodedNames = buildAndEncodeConsentAttributes(attributes);
        consent.setAttributes(encodedNames);
        consent.setCreatedDate(LocalDateTime.now());
        return consent;
    }

    @Override
    public ConsentDecision build(final Service service,
                                 final RegisteredService registeredService,
                                 final String principalId,
                                 final Map<String, Object> attributes) {
        val consent = new ConsentDecision();
        consent.setPrincipal(principalId);
        consent.setService(service.getId());
        return update(consent, attributes);
    }

    @Override
    public boolean doesAttributeReleaseRequireConsent(final ConsentDecision decision,
                                                      final Map<String, Object> attributes) {
        val consentAttributes = getConsentableAttributesFrom(decision);

        if (decision.getOptions() == ConsentReminderOptions.ATTRIBUTE_NAME) {
            val consentAttributesHash = sha512ConsentAttributeNames(consentAttributes);
            val currentAttributesHash = sha512ConsentAttributeNames(attributes);
            return !StringUtils.equals(consentAttributesHash, currentAttributesHash);
        }

        if (decision.getOptions() == ConsentReminderOptions.ATTRIBUTE_VALUE) {
            val consentAttributesHash = sha512ConsentAttributeNames(consentAttributes);
            val currentAttributesHash = sha512ConsentAttributeNames(attributes);

            val consentAttributeValuesHash = sha512ConsentAttributeValues(consentAttributes);
            val currentAttributeValuesHash = sha512ConsentAttributeValues(attributes);

            return !StringUtils.equals(consentAttributesHash, currentAttributesHash)
                || !StringUtils.equals(consentAttributeValuesHash, currentAttributeValuesHash);
        }
        return true;
    }

    @Override
    public Map<String, Object> getConsentableAttributesFrom(final ConsentDecision decision) {
        try {
            val result = this.consentCipherExecutor.decode(decision.getAttributes());
            if (StringUtils.isBlank(result)) {
                LOGGER.warn("Unable to decipher attributes from consent decision [{}]", decision.getId());
                return new HashMap<>(0);
            }
            val names = EncodingUtils.decodeBase64ToString(result);
            val attributes = MAPPER.readValue(names, Map.class);
            return attributes;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not serialize attributes for consent decision");
        }
    }

    private String sha512ConsentAttributeNames(final Map<String, Object> attributes) {
        val allNames = String.join("|", attributes.keySet());
        return DigestUtils.sha512(allNames);
    }

    private String sha512ConsentAttributeValues(final Map<String, Object> attributes) {
        val allValues = attributes.values().stream()
            .map(CollectionUtils::toCollection)
            .map(c -> c.stream().map(Object::toString).collect(Collectors.joining()))
            .collect(Collectors.joining("|"));
        val attributeValues = DigestUtils.sha512(allValues);
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
            val json = MAPPER.writer(new MinimalPrettyPrinter()).writeValueAsString(attributes);
            val base64 = EncodingUtils.encodeBase64(json);
            return this.consentCipherExecutor.encode(base64);
        } catch (final Exception e) {
            throw new IllegalArgumentException("Could not serialize attributes for consent decision");
        }
    }
}
