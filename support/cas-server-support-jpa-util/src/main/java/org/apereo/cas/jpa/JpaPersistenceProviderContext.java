package org.apereo.cas.jpa;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link JpaPersistenceProviderContext}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@Accessors(chain = true)
public class JpaPersistenceProviderContext {
    private Set<String> includeEntityClasses = new LinkedHashSet<>();
}
