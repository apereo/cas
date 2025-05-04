package org.apereo.cas.notifications.mail;

import org.apereo.cas.configuration.support.ConfigurationPropertiesBindingContext;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This is {@link DefaultEmailSender}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
@Getter
public class DefaultEmailSender implements EmailSender {
    private final MessageSource messageSource;

    private final ApplicationContext applicationContext;

    private final List<EmailSenderCustomizer> emailSenderCustomizers;

    private final MailProperties mailProperties;

    private final ObjectProvider<SslBundles> sslBundles;

    private final TenantExtractor tenantExtractor;

    @Override
    public EmailCommunicationResult send(final EmailMessageRequest emailRequest) throws Exception {
        val mailSender = createMailSender(emailRequest);
        val connectionAvailable = mailSender != null && FunctionUtils.doAndHandle(() -> {
            mailSender.testConnection();
            return true;
        }, throwable -> false).get();

        val recipients = emailRequest.getRecipients();
        if (connectionAvailable) {
            val message = createEmailMessage(emailRequest, mailSender);
            emailSenderCustomizers.forEach(customizer -> customizer.customize(mailSender, emailRequest));
            mailSender.send(message);
        }

        return EmailCommunicationResult.builder()
            .success(connectionAvailable)
            .to(recipients)
            .body(emailRequest.getBody())
            .build();
    }

    protected MimeMessage createEmailMessage(final EmailMessageRequest emailRequest,
                                             final JavaMailSender mailSender) throws Exception {
        val recipients = emailRequest.getRecipients();
        val message = mailSender.createMimeMessage();
        val messageHelper = new MimeMessageHelper(message);
        messageHelper.setTo(recipients.toArray(ArrayUtils.EMPTY_STRING_ARRAY));

        val emailProperties = emailRequest.getEmailProperties();
        messageHelper.setText(emailRequest.getBody(), emailProperties.isHtml());

        val subject = determineEmailSubject(emailRequest, messageSource);
        messageHelper.setSubject(subject);
        messageHelper.setFrom(emailProperties.getFrom());
        FunctionUtils.doIfNotBlank(emailProperties.getReplyTo(), messageHelper::setReplyTo);
        messageHelper.setValidateAddresses(emailProperties.isValidateAddresses());
        messageHelper.setPriority(emailProperties.getPriority());
        messageHelper.setCc(emailProperties.getCc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        messageHelper.setBcc(emailProperties.getBcc().toArray(ArrayUtils.EMPTY_STRING_ARRAY));

        return message;
    }

    protected JavaMailSenderImpl createMailSender(final EmailMessageRequest emailRequest) {
        val sender = applyProperties(new JavaMailSenderImpl(), emailRequest);
        return StringUtils.isNotBlank(sender.getHost()) ? sender : null;
    }

    protected JavaMailSenderImpl applyProperties(final JavaMailSenderImpl sender,
                                                 final EmailMessageRequest emailRequest) {
        val effectiveProperties = findTenantEmailProperties(emailRequest);

        sender.setHost(effectiveProperties.getHost());
        if (effectiveProperties.getPort() != null) {
            sender.setPort(effectiveProperties.getPort());
        }
        sender.setUsername(effectiveProperties.getUsername());
        sender.setPassword(effectiveProperties.getPassword());

        sender.setProtocol(effectiveProperties.getProtocol());
        sender.setDefaultEncoding(effectiveProperties.getDefaultEncoding().name());

        val javaMailProperties = asProperties(effectiveProperties.getProperties());
        val protocol = StringUtils.defaultIfBlank(effectiveProperties.getProtocol(), "smtp");

        val ssl = effectiveProperties.getSsl();
        if (ssl.isEnabled()) {
            javaMailProperties.setProperty("mail." + protocol + ".ssl.enable", "true");
        }
        if (StringUtils.isNotBlank(ssl.getBundle())) {
            val sslBundle = sslBundles.getObject().getBundle(ssl.getBundle());
            val socketFactory = sslBundle.createSslContext().getSocketFactory();
            javaMailProperties.put("mail." + protocol + ".ssl.socketFactory", socketFactory);
        }
        if (!javaMailProperties.isEmpty()) {
            sender.setJavaMailProperties(javaMailProperties);
        }
        return sender;
    }

    protected MailProperties findTenantEmailProperties(
        final EmailMessageRequest emailMessageRequest) {
        return tenantExtractor.getTenantsManager()
            .findTenant(emailMessageRequest.getTenant())
            .map(tenantDefinition -> tenantDefinition.bindPropertiesTo(MailProperties.class))
            .filter(ConfigurationPropertiesBindingContext::isBound)
            .filter(bindingContext -> bindingContext.containsBindingFor(MailProperties.class))
            .map(ConfigurationPropertiesBindingContext::value)
            .orElse(mailProperties);
    }
    
    private static Properties asProperties(final Map<String, String> source) {
        val properties = new Properties();
        properties.putAll(source);
        return properties;
    }
}
