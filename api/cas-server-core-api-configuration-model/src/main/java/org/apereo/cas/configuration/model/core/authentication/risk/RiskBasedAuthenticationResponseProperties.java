package org.apereo.cas.configuration.model.core.authentication.risk;

import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.configuration.model.support.sms.SmsProperties;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link RiskBasedAuthenticationResponseProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-electrofence")
public class RiskBasedAuthenticationResponseProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 8254082561120701582L;

    /**
     * If an authentication attempt is deemed risky, block the response
     * and do not allow further attempts.
     */
    private boolean blockAttempt;

    /**
     * If an authentication attempt is deemed risky, force
     * a multi-factor authentication event noted by the provider id here.
     */
    private String mfaProvider;

    /**
     * If an authentication attempt is deemed risky, communicate the nature of
     * this attempt back to the application via a special attribute
     * in the final CAS response indicated here.
     */
    private String riskyAuthenticationAttribute = "triggeredRiskBasedAuthentication";

    /**
     * Control the expiration window of the verification token
     * that can be used to verify and confirm risky authentication
     * attempts.
     */
    @DurationCapable
    private String riskVerificationTokenExpiration = "PT5M";

    /**
     * Risk confirmation attempts are only evaluated up to a point in history, controlled by this setting. That is to say,
     * authentication attempts that are detected as risky are evaluated against previous confirmations in history using this time window.
     * Once we move beyond this point in the history of authentication attempts, the confirmations no longer hold
     * and the user will be asked to verify their attempt again.
     */
    @DurationCapable
    private String getRiskVerificationHistory = "P7D";

    /**
     * Email settings for notifications,
     * If an authentication attempt is deemed risky.
     */
    @NestedConfigurationProperty
    private EmailProperties mail = new EmailProperties();

    /**
     * SMS settings for notifications,
     * If an authentication attempt is deemed risky.
     */
    @NestedConfigurationProperty
    private SmsProperties sms = new SmsProperties();
}
