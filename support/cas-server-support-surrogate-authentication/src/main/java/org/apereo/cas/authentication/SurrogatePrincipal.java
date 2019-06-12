package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * This is {@link SurrogatePrincipal}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class SurrogatePrincipal implements Principal {
    private static final long serialVersionUID = 5672386093026290631L;

    private final Principal primary;
    private final Principal surrogate;

    @Override
    public String getId() {
        return surrogate.getId();
    }

    @Override
    public Map<String, List<Object>> getAttributes() {
        return surrogate.getAttributes();
    }
}
