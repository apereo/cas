package org.apereo.cas.config;

import org.apereo.cas.authentication.CasSoapEndpoint;

import lombok.val;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor;
import org.springframework.ws.soap.security.wss4j2.callback.SimplePasswordValidationCallbackHandler;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.List;
import java.util.Properties;

/**
 * This is {@link SoapAuthenticationServerTestConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestConfiguration("SoapAuthenticationServerTestConfiguration")
@EnableWs
@ComponentScan("org.apereo.cas")
@Lazy(false)
public class SoapAuthenticationServerTestConfiguration extends WsConfigurerAdapter {
    @Bean
    public SimplePasswordValidationCallbackHandler securityCallbackHandler() {
        val callbackHandler = new SimplePasswordValidationCallbackHandler();
        val users = new Properties();
        users.setProperty("casuser", "Mellon");
        callbackHandler.setUsers(users);
        return callbackHandler;
    }

    @Bean
    public Wss4jSecurityInterceptor securityInterceptor() {
        val securityInterceptor = new Wss4jSecurityInterceptor();
        securityInterceptor.setValidationActions("Timestamp UsernameToken");
        securityInterceptor.setValidationCallbackHandler(securityCallbackHandler());
        return securityInterceptor;
    }

    @Override
    public void addInterceptors(final List interceptors) {
        interceptors.add(securityInterceptor());
    }

    @Bean
    public ServletRegistrationBean messageDispatcherServlet(final ApplicationContext appContext) {
        val servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(appContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean(servlet, "/ws/*");
    }

    @Bean(name = "users")
    public DefaultWsdl11Definition defaultWsdl11Definition(final XsdSchema schema) {
        val wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("UsersPort");
        wsdl11Definition.setLocationUri("http://localhost:8080/ws");
        wsdl11Definition.setTargetNamespace(CasSoapEndpoint.NAMESPACE_URI);
        wsdl11Definition.setSchema(schema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema beersSchema() {
        return new SimpleXsdSchema(new ClassPathResource("xsd/users.xsd"));
    }
}
