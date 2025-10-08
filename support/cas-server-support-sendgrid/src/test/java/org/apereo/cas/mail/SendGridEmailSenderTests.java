package org.apereo.cas.mail;

import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasSendGridAutoConfiguration;
import org.apereo.cas.configuration.model.support.email.EmailProperties;
import org.apereo.cas.notifications.mail.EmailMessageRequest;
import org.apereo.cas.notifications.mail.EmailSender;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import lombok.val;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.sendgrid.autoconfigure.SendGridAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SendGridEmailSenderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    SendGridAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasSendGridAutoConfiguration.class
}, properties = "spring.sendgrid.api-key=12345667890")
@Tag("Mail")
@ExtendWith(CasTestExtension.class)
class SendGridEmailSenderTests {
    @Autowired
    @Qualifier(EmailSender.BEAN_NAME)
    private EmailSender emailSender;

    private static Stream<Arguments> emailContentTypes() {
        return Stream.of(
            Arguments.of(Named.of("HTML Email", Boolean.TRUE)),
            Arguments.of(Named.of("Plain Email", Boolean.FALSE))
        );
    }

    @ParameterizedTest
    @MethodSource("emailContentTypes")
    void verifyOperation(final boolean html) throws Exception {
        assertNotNull(emailSender);
        val principal = RegisteredServiceTestUtils.getPrincipal("casuser",
            Map.of("email", List.of("casuser@apereo.org")));
        val emailRequest = EmailMessageRequest.builder()
            .locale(Locale.FRANCE)
            .body("This is the email body")
            .emailProperties(new EmailProperties()
                .setHtml(html)
                .setSubject("This is the subject")
                .setFrom("from@apereo.org"))
            .attribute("email")
            .principal(principal)
            .build();
        assertFalse(emailSender.send(emailRequest).isSuccess());
    }
}
