package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Return environment info and app profiles for the service.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Getter
@Setter
@NoArgsConstructor
public class ReturnEnvironmentAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 1239257723778012771L;

    @JsonProperty("environmentVariables")
    private Map<String, String> environmentVariables = new TreeMap<>();

    @JsonProperty("systemProperties")
    private Map<String, String> systemProperties = new TreeMap<>();
    
    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> resolvedAttributes) {
        val attributes = new LinkedHashMap<String, List<Object>>();
        attributes.putAll(fetchAttributes(environmentVariables,
            entry -> List.of(System.getenv(entry.getKey()))));
        attributes.putAll(fetchAttributes(systemProperties,
            entry -> List.of(System.getProperty(entry.getKey()))));
        val profiles = (List) CollectionUtils.wrapList(context.getApplicationContext().getEnvironment().getActiveProfiles());
        attributes.put("applicationProfiles", profiles);
        return attributes;
    }

    protected Map fetchAttributes(final Map<String, String> container,
                                  final Function<Map.Entry<String, ?>, List<?>> valueMapper) {
        return container.entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getValue(), valueMapper.apply(entry)))
            .collect(Collectors.toMap(Pair::getKey,
                entry -> CollectionUtils.toCollection(entry.getValue(), ArrayList.class)));
    }
}
