package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.EncodingUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jooq.lambda.Unchecked;

import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Return only the collection of allowed attributes out of what's resolved
 * for the principal.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReturnEncryptedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = -5771481877391140569L;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private List<String> allowedAttributes = new ArrayList<>(0);

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attrs,
                                                           final RegisteredService registeredService, final Service selectedService) {
        return authorizeReleaseOfAllowedAttributes(principal, attrs, registeredService, selectedService);
    }


    /**
     * Authorize release of allowed attributes map.
     *
     * @param principal         the principal
     * @param attrs             the attributes
     * @param registeredService the registered service
     * @param selectedService   the selected service
     * @return the map
     */
    protected Map<String, List<Object>> authorizeReleaseOfAllowedAttributes(final Principal principal,
                                                                            final Map<String, List<Object>> attrs,
                                                                            final RegisteredService registeredService,
                                                                            final Service selectedService) {
        val publicKey = registeredService.getPublicKey();
        if (publicKey == null) {
            LOGGER.error("No public key is defined for service [{}]. No attributes will be released", registeredService);
            return new HashMap<>(0);
        }
        LOGGER.debug("Using service [{}] public key [{}] to initialize the cipher", registeredService.getServiceId(), publicKey);
        val cipher = publicKey.toCipher();
        if (cipher == null) {
            LOGGER.error("Unable to initialize cipher given the public key algorithm [{}]", publicKey.getAlgorithm());
            return new HashMap<>(0);
        }
        val resolvedAttributes = new TreeMap<String, List<Object>>(String.CASE_INSENSITIVE_ORDER);
        resolvedAttributes.putAll(attrs);
        val attributesToRelease = new HashMap<String, List<Object>>();
        getAllowedAttributes()
            .stream()
            .filter(resolvedAttributes::containsKey)
            .forEach(attr -> {
                LOGGER.debug("Found attribute [{}] in the list of allowed attributes. Encoding...", attr);
                val encodedValues = resolvedAttributes.get(attr)
                    .stream()
                    .map(Unchecked.function(value -> {
                        LOGGER.trace("Encrypting attribute [{}] with value [{}]", attr, value);
                        val result = EncodingUtils.encodeBase64(cipher.doFinal(value.toString().getBytes(StandardCharsets.UTF_8)));
                        LOGGER.trace("Encrypted attribute [{}] with value [{}]", attr, result);
                        return result;
                    }))
                    .collect(Collectors.<Object>toList());
                attributesToRelease.put(attr, encodedValues);
            });
        return attributesToRelease;
    }

}
