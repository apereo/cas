package org.apereo.cas.jpa;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link JpaPersistenceProviderContext}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
public class JpaPersistenceProviderContext {
    private Set<String> includeEntityClasses = new LinkedHashSet<>();
}
