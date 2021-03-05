package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.core.Ordered;

/**
 * This is {@link BaseAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@ToString
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Setter
public abstract class BaseAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private int order = Ordered.HIGHEST_PRECEDENCE;

    protected BaseAuthenticationMetaDataPopulator() {
        this(Ordered.HIGHEST_PRECEDENCE);
    }

}
