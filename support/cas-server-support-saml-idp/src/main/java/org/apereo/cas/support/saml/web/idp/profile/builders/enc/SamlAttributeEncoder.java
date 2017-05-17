package org.apereo.cas.support.saml.web.idp.profile.builders.enc;

import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link SamlAttributeEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlAttributeEncoder implements ProtocolAttributeEncoder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlAttributeEncoder.class);

    private static void transformMicrosoftSchemaAttributes(final Map<String, Object> attributes) {
        final Set<Pair<String, Object>> attrs = attributes.keySet().stream()
                .filter(s -> s.toLowerCase().startsWith("http_//schemas.microsoft.com"))
                .map(s -> Pair.of(s.replace('_', ':'), attributes.get(s)))
                .collect(Collectors.toSet());
        transformFilteredAttributes(attributes, attrs);
    }


    private static void transformUniformResourceNames(final Map<String, Object> attributes) {
        final Set<Pair<String, Object>> attrs = attributes.keySet().stream()
                .filter(s -> s.toLowerCase().startsWith("urn_"))
                .map(s -> Pair.of(s.replace('_', ':'), attributes.get(s)))
                .collect(Collectors.toSet());
        transformFilteredAttributes(attributes, attrs);
    }

    private static void transformFilteredAttributes(final Map<String, Object> attributes, final Set<Pair<String, Object>> attrs) {
        if (!attrs.isEmpty()) {
            LOGGER.debug("Found [{}] attribute(s) that will be transformed.", attrs);
            attributes.entrySet().removeIf(s -> s.getKey().startsWith("urn_"));
            attrs.forEach(p -> {
                LOGGER.debug("Transformed attribute name to be [{}]", p.getKey());
                attributes.put(p.getKey(), p.getValue());
            });
        } else {
            LOGGER.debug("No attributes require special transformation.", attrs);
        }
    }

    @Override
    public Map<String, Object> encodeAttributes(final Map<String, Object> attributes, final RegisteredService service) {
        final Map<String, Object> finalAttributes = new HashMap<>(attributes);
        transformUniformResourceNames(finalAttributes);
        transformMicrosoftSchemaAttributes(finalAttributes);
        return finalAttributes;
    }
}
