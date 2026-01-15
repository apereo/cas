package org.apereo.cas.heimdall.authorizer.resource.policy;

import module java.base;
import org.apereo.cas.heimdall.AuthorizationRequest;
import org.apereo.cas.heimdall.authorizer.AuthorizationResult;
import org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.val;

/**
 * This is {@link RequiredAttributesAuthorizationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RequiredAttributesAuthorizationPolicy implements ResourceAuthorizationPolicy {
    @Serial
    private static final long serialVersionUID = -2444481042826672523L;

    /**
     * Collection of required attributes
     * for this service to proceed.
     */
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Set<String>> attributes = new HashMap<>();

    @Override
    public AuthorizationResult evaluate(final AuthorizableResource resource, final AuthorizationRequest request) {
        val principalAttributes = request.getPrincipal().getAttributes();
        return getAttributes()
            .entrySet()
            .parallelStream()
            .filter(entry -> principalAttributes.containsKey(entry.getKey()))
            .filter(entry -> {
                val attributeValues = CollectionUtils.toCollection(principalAttributes.get(entry.getKey()));
                return entry.getValue().parallelStream().anyMatch(value -> RegexUtils.findFirst(value, attributeValues).isPresent());
            })
            .findAny()
            .map(entry -> AuthorizationResult.granted("OK"))
            .orElseGet(() -> AuthorizationResult.denied("Denied"));
    }
}
