package org.apereo.cas.authentication.metadata;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.springframework.core.Ordered;
import lombok.ToString;

/**
 * This is {@link BaseAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
@AllArgsConstructor
public abstract class BaseAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    public BaseAuthenticationMetaDataPopulator() {
        this(Ordered.HIGHEST_PRECEDENCE);
    }

}
