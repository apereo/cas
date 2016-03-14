package org.jasig.cas.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.JstlView;
import org.thymeleaf.Arguments;
import org.thymeleaf.dom.Text;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafView;
import org.thymeleaf.templatemode.TemplateModeHandler;
import org.thymeleaf.templateparser.xmlsax.XhtmlAndHtml5NonValidatingSAXTemplateParser;
import org.thymeleaf.templatewriter.AbstractGeneralTemplateWriter;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.Locale;

/**
 * This is {@link CasProtocolViewsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casProtocolViewsConfiguration")
@Lazy(true)
public class CasProtocolViewsConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SpringTemplateEngine springTemplateEngine;

    @Value("${view.cas2.success:protocol/2.0/casServiceValidationSuccess}")
    private String cas2SuccessView;

    @Autowired
    private ThymeleafProperties properties;

    /**
     * The Cas 2 failure view.
     */
    @Value("${view.cas2.failure:protocol/2.0/casServiceValidationFailure}")
    private String cas2FailureView;

    /**
     * The Cas 2 proxy success view.
     */
    @Value("${view.cas2.proxy.success:protocol/2.0/casProxySuccessView}")
    private String cas2ProxySuccessView;

    /**
     * The Cas 2 proxy failure view.
     */
    @Value("${view.cas2.proxy.failure:protocol/2.0/casProxyFailureView}")
    private String cas2ProxyFailureView;

    /**
     * The Cas 3 success view.
     */
    @Value("${view.cas3.success:protocol/3.0/casServiceValidationSuccess}")
    private String cas3SuccessView;

    /**
     * The Cas 3 failure view.
     */
    @Value("${view.cas3.failure:protocol/3.0/casServiceValidationFailure}")
    private String cas3FailureView;

    /**
     * Cas 2  success view.
     *
     * @return the  view
     */
    @Bean(name = "cas2SuccessView")
    public View cas2SuccessView() {
        return new CasProtocolView(this.cas2SuccessView, applicationContext, springTemplateEngine);
    }

    /**
     * Cas 2 service failure view.
     *
     * @return the  view
     */
    @Bean(name = "cas2ServiceFailureView")
    public View cas2ServiceFailureView() {
        return new CasProtocolView(this.cas2FailureView, applicationContext, springTemplateEngine);
    }

    /**
     * Cas 2 proxy failure view.
     *
     * @return the  view
     */
    @Bean(name = "cas2ProxyFailureView")
    public View cas2ProxyFailureView() {
        return new CasProtocolView(this.cas2ProxyFailureView, applicationContext, springTemplateEngine);
    }

    /**
     * Cas 2 proxy success view.
     *
     * @return the view
     */
    @Bean(name = "cas2ProxySuccessView")
    public View cas2ProxySuccessView() {
        return new CasProtocolView(this.cas2ProxySuccessView, applicationContext, springTemplateEngine);
    }

    /**
     * Cas 3 success view.
     *
     * @return the view
     */
    @Bean(name = "cas3SuccessView")
    public View cas3SuccessView() {
        return new CasProtocolView(this.cas3SuccessView, applicationContext, springTemplateEngine);
    }

    /**
     * Cas 3 service failure view.
     *
     * @return the view
     */
    @Bean(name = "cas3ServiceFailureView")
    public View cas3ServiceFailureView() {
        return new CasProtocolView(this.cas3FailureView, applicationContext, springTemplateEngine);
    }

    /**
     * Oauth confirm view.
     *
     * @return the view
     */
    @Bean(name = "oauthConfirmView")
    public View oauthConfirmView() {
        return new CasProtocolView("protocol/oauth/confirm.jsp", applicationContext, springTemplateEngine);
    }


    /**
     * Cas open id service failure view.
     *
     * @return the view
     */
    @Bean(name = "casOpenIdServiceFailureView")
    public View casOpenIdServiceFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceFailureView", applicationContext, springTemplateEngine);
    }

    /**
     * Cas open id service success view.
     *
     * @return the view
     */
    @Bean(name = "casOpenIdServiceSuccessView")
    public View casOpenIdServiceSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdServiceSuccessView", applicationContext, springTemplateEngine);
    }

    /**
     * Cas open id association failure view.
     *
     * @return the view
     */
    @Bean(name = "casOpenIdAssociationFailureView")
    public View casOpenIdAssociationFailureView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationFailureView", applicationContext, springTemplateEngine);
    }

    /**
     * Cas open id association success view .
     *
     * @return the view
     */
    @Bean(name = "casOpenIdAssociationSuccessView")
    public View casOpenIdAssociationSuccessView() {
        return new CasProtocolView("protocol/openid/casOpenIdAssociationSuccessView", applicationContext, springTemplateEngine);
    }

    /**
     * Open id provider view.
     *
     * @return the view
     */
    @Bean(name = "openIdProviderView")
    public JstlView openIdProviderView() {
        return new JstlView("/WEB-INF/view/jsp/protocol/openid/user.jsp");
    }


    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        this.springTemplateEngine.addTemplateModeHandler(
                new TemplateModeHandler(properties.getMode(),
                        new XhtmlAndHtml5NonValidatingSAXTemplateParser(2), new
                        WhiteSpaceNormalizedTemplateWriter()));
    }

    private static class WhiteSpaceNormalizedTemplateWriter extends AbstractGeneralTemplateWriter {
        @Override
        protected boolean shouldWriteXmlDeclaration() {
            return false;
        }

        @Override
        protected boolean useXhtmlTagMinimizationRules() {
            return true;
        }

        @Override
        protected void writeText(final Arguments arguments, final Writer writer, final Text text) throws IOException {
            final char[] textChars = text.getContent().toCharArray();
            if (StringUtils.isNotBlank(CharBuffer.wrap(textChars))) {
                writer.write(textChars);
            }
        }
    }

    private static class CasProtocolView extends ThymeleafView {
        /**
         * Instantiates a new Cas protocol view.
         *
         * @param templateName       the template name
         * @param applicationContext the application context
         * @param templateEngine     the template engine
         */
        CasProtocolView(final String templateName, final ApplicationContext applicationContext,
                        final SpringTemplateEngine templateEngine) {
            super(templateName);
            setApplicationContext(applicationContext);
            setTemplateEngine(templateEngine);
            setLocale(Locale.getDefault());
        }
    }
}
