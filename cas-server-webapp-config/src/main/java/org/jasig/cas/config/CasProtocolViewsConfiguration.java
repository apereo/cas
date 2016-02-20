package org.jasig.cas.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.JstlView;

/**
 * This is {@link CasProtocolViewsConfiguration} that attempts to create Spring-managed beans
 * backed by external configuration.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Configuration("casProtocolViewsConfiguration")
public class CasProtocolViewsConfiguration {

    @Value("${view.cas2.success:/WEB-INF/view/jsp/protocol/2.0/casServiceValidationSuccess.jsp}")
    private String cas2SuccessView;

    @Value("${view.cas2.failure:/WEB-INF/view/jsp/protocol/2.0/casServiceValidationFailure.jsp}")
    private String cas2FailureView;

    @Value("${view.cas2.proxy.success:/WEB-INF/view/jsp/protocol/2.0/casProxySuccessView.jsp}")
    private String cas2ProxySuccessView;

    @Value("${view.cas2.proxy.failure:/WEB-INF/view/jsp/protocol/2.0/casProxyFailureView.jsp}")
    private String cas2ProxyFailureView;

    @Value("${view.cas3.success:/WEB-INF/view/jsp/protocol/3.0/casServiceValidationSuccess.jsp}")
    private String cas3SuccessView;

    @Value("${view.cas3.failure:/WEB-INF/view/jsp/protocol/3.0/casServiceValidationFailure.jsp}")
    private String cas3FailureView;

    @Value("${view.cas.post.response:/WEB-INF/view/jsp/protocol/casPostResponseView.jsp}")
    private String postResponseView;

    @Bean
    public JstlView cas2JstlSuccessView() {
        return new JstlView(this.cas2SuccessView);
    }

    @Bean
    public JstlView cas2ServiceFailureView() {
        return new JstlView(this.cas2FailureView);
    }

    @Bean
    public JstlView cas2ProxyFailureView() {
        return new JstlView(this.cas2ProxyFailureView);
    }

    @Bean
    public JstlView cas2ProxySuccessView() {
        return new JstlView(this.cas2ProxySuccessView);
    }

    @Bean
    public JstlView cas3JstlSuccessView() {
        return new JstlView(this.cas3SuccessView);
    }

    @Bean
    public JstlView cas3ServiceFailureView() {
        return new JstlView(this.cas3FailureView);
    }

    @Bean
    public JstlView postResponseView() {
        return new JstlView(this.postResponseView);
    }
}
