package org.apereo.cas.impl.plans;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.RiskBasedAuthenticationProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link BaseAuthenticationRiskContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRiskContingencyPlan implements AuthenticationRiskContingencyPlan {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    /**
     * Only activated via boot if properties are defined.
     * May not always want to send email.
     */
    @Autowired(required = false)
    @Qualifier("mailSender")
    private JavaMailSender mailSender;

    @Override
    public final AuthenticationRiskContingencyResponse execute(final Authentication authentication,
                                                               final RegisteredService service,
                                                               final AuthenticationRiskScore score,
                                                               final HttpServletRequest request) {
        logger.debug("Executing {} to produce a risk response", getClass().getSimpleName());

        email(authentication.getPrincipal());
        sms(authentication.getPrincipal());

        return executeInternal(authentication, service, score, request);
    }

    /**
     * Execute authentication risk contingency plan.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     * @param request        the request
     * @return the authentication risk contingency response. May be null.
     */
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        return null;
    }

    private void sms(final Principal principal) {
        final RiskBasedAuthenticationProperties.Response.Sms sms =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getSms();

        if (StringUtils.isBlank(sms.getText())
                || StringUtils.isBlank(sms.getFrom())
                || StringUtils.isBlank(sms.getTwilio().getToken())
                || StringUtils.isBlank(sms.getTwilio().getAccountId())
                || !principal.getAttributes().containsKey(sms.getAttributeName())) {
            logger.debug("Could not send sms {} because either no phones could be found or sms settings are not configured.",
                    principal.getId());
            return;
        }

        try {
            Twilio.init(sms.getTwilio().getAccountId(), sms.getTwilio().getToken());
            Message.creator(
                    new PhoneNumber(principal.getAttributes().get(sms.getAttributeName()).toString()),
                    new PhoneNumber(sms.getFrom()),
                    sms.getText()).create();
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void email(final Principal principal) {
        final RiskBasedAuthenticationProperties.Response.Mail mail =
                casProperties.getAuthn().getAdaptive().getRisk().getResponse().getMail();

        if (this.mailSender == null
                || StringUtils.isBlank(mail.getText())
                || StringUtils.isBlank(mail.getFrom())
                || StringUtils.isBlank(mail.getSubject())
                || !principal.getAttributes().containsKey(mail.getAttributeName())) {
            logger.debug("Could not send email {} because either no addresses could be found or email settings are not configured.",
                    principal.getId());
            return;
        }

        try {
            final String to = principal.getAttributes().get(mail.getAttributeName()).toString();
            final MimeMessage message = this.mailSender.createMimeMessage();
            final MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setTo(to);
            helper.setText(mail.getText());
            helper.setSubject(mail.getSubject());
            helper.setFrom(mail.getFrom());
            helper.setPriority(1);

            if (StringUtils.isNotBlank(mail.getCc())) {
                helper.setCc(mail.getCc());
            }

            if (StringUtils.isNotBlank(mail.getBcc())) {
                helper.setBcc(mail.getBcc());
            }
            this.mailSender.send(message);
        } catch (final Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
