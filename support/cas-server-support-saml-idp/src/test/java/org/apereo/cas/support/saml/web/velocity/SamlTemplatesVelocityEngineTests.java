package org.apereo.cas.support.saml.web.velocity;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;

import lombok.val;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlTemplatesVelocityEngineTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlTemplatesVelocityEngineTests extends BaseSamlIdPConfigurationTests {
    @Autowired
    @Qualifier("shibboleth.VelocityEngine")
    private VelocityEngine velocityEngineFactoryBean;

    @Test
    public void verifySaml2PostBinding() {
        val template = velocityEngineFactoryBean.getTemplate("templates/saml2-post-binding.vm");
        assertNotNull(template);
        template.merge(new VelocityContext(), new StringWriter());
    }

    @Test
    public void verifySaml2SimpleSignBinding() {
        val template = velocityEngineFactoryBean.getTemplate("templates/saml2-post-simplesign-binding.vm");
        assertNotNull(template);
        template.merge(new VelocityContext(), new StringWriter());
    }
}
