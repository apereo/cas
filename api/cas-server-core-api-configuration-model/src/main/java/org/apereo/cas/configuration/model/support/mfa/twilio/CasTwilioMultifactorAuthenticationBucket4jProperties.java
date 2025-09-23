package org.apereo.cas.configuration.model.support.mfa.twilio;

import org.apereo.cas.configuration.model.support.bucket4j.BaseBucket4jProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link CasTwilioMultifactorAuthenticationBucket4jProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-twilio-mfa")
@Getter
@Setter
@Accessors(chain = true)
public class CasTwilioMultifactorAuthenticationBucket4jProperties extends BaseBucket4jProperties {
    @Serial
    private static final long serialVersionUID = -2432886337199727140L;

    public CasTwilioMultifactorAuthenticationBucket4jProperties() {
        setEnabled(false);
    }
}
