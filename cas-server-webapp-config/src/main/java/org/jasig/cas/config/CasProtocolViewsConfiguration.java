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

    /**
     * The Cas 2 success view.
     */
    @Value("${view.cas2.success:/WEB-INF/view/jsp/protocol/2.0/casServiceValidationSuccess.jsp}")
    private String cas2SuccessView;

    /**
     * The Cas 2 failure view.
     */
    @Value("${view.cas2.failure:/WEB-INF/view/jsp/protocol/2.0/casServiceValidationFailure.jsp}")
    private String cas2FailureView;

    /**
     * The Cas 2 proxy success view.
     */
    @Value("${view.cas2.proxy.success:/WEB-INF/view/jsp/protocol/2.0/casProxySuccessView.jsp}")
    private String cas2ProxySuccessView;

    /**
     * The Cas 2 proxy failure view.
     */
    @Value("${view.cas2.proxy.failure:/WEB-INF/view/jsp/protocol/2.0/casProxyFailureView.jsp}")
    private String cas2ProxyFailureView;

    /**
     * The Cas 3 success view.
     */
    @Value("${view.cas3.success:/WEB-INF/view/jsp/protocol/3.0/casServiceValidationSuccess.jsp}")
    private String cas3SuccessView;

    /**
     * The Cas 3 failure view.
     */
    @Value("${view.cas3.failure:/WEB-INF/view/jsp/protocol/3.0/casServiceValidationFailure.jsp}")
    private String cas3FailureView;

    /**
     * The Post response view.
     */
    @Value("${view.cas.post.response:/WEB-INF/view/jsp/protocol/casPostResponseView.jsp}")
    private String postResponseView;

    /**
     * Cas 2 jstl success view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas2JstlSuccessView")
    public JstlView cas2JstlSuccessView() {
        return new JstlView(this.cas2SuccessView);
    }

    /**
     * Cas 2 service failure view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas2ServiceFailureView")
    public JstlView cas2ServiceFailureView() {
        return new JstlView(this.cas2FailureView);
    }

    /**
     * Cas 2 proxy failure view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas2ProxyFailureView")
    public JstlView cas2ProxyFailureView() {
        return new JstlView(this.cas2ProxyFailureView);
    }

    /**
     * Cas 2 proxy success view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas2ProxySuccessView")
    public JstlView cas2ProxySuccessView() {
        return new JstlView(this.cas2ProxySuccessView);
    }

    /**
     * Cas 3 jstl success view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas3JstlSuccessView")
    public JstlView cas3JstlSuccessView() {
        return new JstlView(this.cas3SuccessView);
    }

    /**
     * Cas 3 service failure view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "cas3ServiceFailureView")
    public JstlView cas3ServiceFailureView() {
        return new JstlView(this.cas3FailureView);
    }

    /**
     * Post response view jstl view.
     *
     * @return the jstl view
     */
    @Bean(name = "postResponseView")
    public JstlView postResponseView() {
        return new JstlView(this.postResponseView);
    }
}
