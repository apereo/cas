package org.apereo.cas.configuration.model.support.mfa.simple;

import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link CasSimpleMultifactorAuthenticationBucket4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiresModule(name = "cas-server-support-simple-mfa")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CasSimpleMultifactorAuthenticationBucket4jProperties")
public class CasSimpleMultifactorAuthenticationBucket4jProperties extends BaseBucket4jProperties {
    private static final long serialVersionUID = -2432886337199727140L;

    public CasSimpleMultifactorAuthenticationBucket4jProperties() {
        setEnabled(false);
    }
}
