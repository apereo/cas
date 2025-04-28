package org.apereo.cas.mfa.twilio;

import org.apereo.cas.authentication.MultifactorAuthenticationFailedException;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.model.support.mfa.twilio.CasTwilioMultifactorAuthenticationProperties;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.List;
import java.util.Locale;

/**
 * This is {@link CasDefaultTwilioMultifactorAuthenticationService}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Slf4j
public class CasDefaultTwilioMultifactorAuthenticationService implements CasTwilioMultifactorAuthenticationService {
    protected final CasTwilioMultifactorAuthenticationProperties properties;

    public CasDefaultTwilioMultifactorAuthenticationService(final CasTwilioMultifactorAuthenticationProperties properties) {
        this.properties = properties;
        val resolver = SpringExpressionLanguageValueResolver.getInstance();
        Twilio.init(resolver.resolve(properties.getCore().getAccountId()), resolver.resolve(properties.getCore().getToken()));
    }

    @Override
    public boolean generateToken(final Principal principal, final WebApplicationService service) {
        val channels = determineVerificationChannels(principal, service);
        val recipient = principal.getSingleValuedAttribute(properties.getCore().getRecipientAttributeName(), String.class);
        LOGGER.debug("Sending Twilio verification attempt to recipient [{}] via [{}]", recipient, channels);
        val serviceSid = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getCore().getServiceSid());
        return channels
            .stream()
            .anyMatch(channel -> FunctionUtils.doAndHandle(() -> {
                val verification = Verification.creator(serviceSid, recipient, channel.toString()).create();
                LOGGER.debug("Generated token for [{}] with SID: [{}]", principal.getId(), verification);
                return StringUtils.isNotBlank(verification.getSid());
            }, e -> false).get());
    }

    @Override
    public Principal validate(final Principal principal, final CasTwilioMultifactorTokenCredential credential) throws Throwable {
        val verificationCheck = verifyAttempt(principal, credential);
        LOGGER.debug("Verification check result for [{}] is [{}]", principal.getId(), verificationCheck);
        val valid = verificationCheck != null
            && verificationCheck.getValid()
            && StringUtils.isNotBlank(verificationCheck.getStatus())
            && Verification.Status.forValue(verificationCheck.getStatus()) == Verification.Status.APPROVED
            && StringUtils.isNotBlank(verificationCheck.getSid());
        FunctionUtils.throwIf(!valid,
            () -> new MultifactorAuthenticationFailedException("Twilio verification failed for %s".formatted(principal.getId())));
        return principal;
    }

    protected VerificationCheck verifyAttempt(final Principal principal, final CasTwilioMultifactorTokenCredential credential) {
        return FunctionUtils.doAndHandle(() -> {
            val serviceSid = SpringExpressionLanguageValueResolver.getInstance().resolve(properties.getCore().getServiceSid());
            val recipient = principal.getSingleValuedAttribute(properties.getCore().getRecipientAttributeName(), String.class);
            LOGGER.debug("Verifying token for [{}] with recipient [{}]", principal.getId(), recipient);
            return VerificationCheck.creator(serviceSid)
                .setTo(recipient)
                .setCode(credential.getToken().trim())
                .create();
        });
    }

    protected List<Verification.Channel> determineVerificationChannels(final Principal principal, final WebApplicationService service) {
        return properties.getCore().getVerificationChannels()
            .stream()
            .map(name -> name.trim().toUpperCase(Locale.ENGLISH))
            .map(Verification.Channel::valueOf)
            .toList();
    }

}
