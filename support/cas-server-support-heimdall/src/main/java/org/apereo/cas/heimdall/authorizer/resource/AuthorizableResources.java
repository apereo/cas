package org.apereo.cas.heimdall.authorizer.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link AuthorizableResources}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class AuthorizableResources implements Serializable {
    @Serial
    private static final long serialVersionUID = -2037726972241437497L;
    
    private final List<AuthorizableResource> resources = new ArrayList<>();

    private String namespace;
}
