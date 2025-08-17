package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This is {@link EmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
public class EmailProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = 7367120636536230761L;

    /**
     * Principal attribute names that indicates the destination email address
     * for this message. The attributes must already be resolved and available
     * to the CAS principal. When multiple attributes are specified, each attribute
     * is then examined against the available CAS principal to locate the email address
     * value, which may result in multiple emails being sent.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private List<String> attributeName = Stream.of("mail", "email").toList();

    /**
     * Email message body.
     * Could be plain text or a reference
     * to an external file that would serve as a template.
     * <p>
     * If specified as a path to an external file with an extension {@code .gtemplate},
     * then the email message body would be processed using the Groovy template engine.
     * The template engine uses JSP style &lt;% %&gt; script and &lt;%= %&gt; expression syntax or
     * GString style expressions. The variable {@code out} is bound to
     * the writer that the template is being written to.
     * <p>
     * If using plain text, the contents are processed for string substitution candidates using named variables.
     * For example, you may refer to an expected url variable in the email text via {@code ${url}},
     * or use {@code ${token}} to locate the token variable. In certain cases, additional parameters
     * are passed to the email body processor that might include authentication and/or principal attributes,
     * the available locale, client http information, etc.
     */
    private String text;

    /**
     * Email from address.
     */
    @RequiredProperty
    private String from;

    /**
     * Email subject line.
     * <p>
     * The subject can either be defined verbatim, or it
     * may point to a message key in the language bundle
     * using the syntax {@code #{subject-language-key}}.
     * This key should point to a valid message
     * defined in the appropriate language bundle that is
     * then picked up via the active locale. In case where
     * the language code cannot resolve the real subject,
     * a default subject value would be used.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String subject;

    /**
     * Email CC address, if any.
     */
    private List<String> cc = new ArrayList<>();

    /**
     * Email BCC address, if any.
     */
    private List<String> bcc = new ArrayList<>();

    /**
     * Email Reply-To address, if any.
     */
    private String replyTo;

    /**
     * Indicate whether the message body
     * should be evaluated as HTML text.
     * The application of this setting depends on the email provider implementation
     * and may not be fully supported everywhere. This is typically relevant for the default
     * {@link org.springframework.mail.javamail.JavaMailSender}.
     */
    private boolean html;

    /**
     * Set whether to validate all addresses which get passed to this helper.
     * The application of this setting depends on the email provider implementation
     * and may not be fully supported everywhere. This is typically relevant for the default
     * {@link org.springframework.mail.javamail.JavaMailSender}.
     */
    private boolean validateAddresses;

    /**
     * Set the priority ({@code X-Priority} header) of the message.
     * Values: {@code 1 (Highest)}, {@code 2 (High)}, {@code 3 (Normal)}, {@code 4 (Low)}, {@code 5 (Lowest)}.
     * The application of this setting depends on the email provider implementation
     * and may not be fully supported everywhere. This is typically relevant for the default
     * {@link org.springframework.mail.javamail.JavaMailSender}.
     */
    private int priority = 1;
}
