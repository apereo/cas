package org.apereo.cas.heimdall.authorizer.resource;

import module java.base;
import org.apereo.cas.heimdall.authorizer.resource.policy.ResourceAuthorizationPolicy;
import org.apereo.cas.util.serialization.PatternJsonDeserializer;
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
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * This is {@link AuthorizableResource}.
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
public class AuthorizableResource implements Serializable {
    @Serial
    private static final long serialVersionUID = -1222481042826672523L;

    private long id;
    
    @JsonDeserialize(using = PatternJsonDeserializer.class)
    private Pattern pattern;

    private String method;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<ResourceAuthorizationPolicy> policies = new ArrayList<>();

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Map<String, Object> properties = new HashMap<>();

    private boolean enforceAllPolicies;
}
