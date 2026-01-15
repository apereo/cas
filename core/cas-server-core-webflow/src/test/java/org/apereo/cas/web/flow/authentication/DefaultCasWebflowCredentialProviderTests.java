package org.apereo.cas.web.flow.authentication;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasWebflowCredentialProviderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowConfig")
class DefaultCasWebflowCredentialProviderTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier(CasWebflowCredentialProvider.BEAN_NAME)
    protected CasWebflowCredentialProvider casWebflowCredentialProvider;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val credentials = RegisteredServiceTestUtils.getCredentialsWithSameUsernameAndPassword(UUID.randomUUID().toString());
        WebUtils.putCredential(context, credentials);
        assertFalse(casWebflowCredentialProvider.extract(context).isEmpty());
    }
}
