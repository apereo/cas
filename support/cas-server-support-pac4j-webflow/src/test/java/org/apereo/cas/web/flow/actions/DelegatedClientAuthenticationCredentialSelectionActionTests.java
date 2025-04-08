package org.apereo.cas.web.flow.actions;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.principal.DelegatedAuthenticationCandidateProfile;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BaseDelegatedAuthenticationTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientAuthenticationCredentialSelectionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Delegation")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseDelegatedAuthenticationTests.SharedTestConfiguration.class)
class DelegatedClientAuthenticationCredentialSelectionActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION)
    private Action action;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext).withUserAgent().setClientInfo();
        assertEquals(CasWebflowConstants.TRANSITION_ID_SELECT, action.execute(context).getId());
        val profile = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(CoreAuthenticationTestUtils.getAttributes())
            .id(UUID.randomUUID().toString())
            .key(UUID.randomUUID().toString())
            .linkedId("casuser")
            .build();
        DelegationWebflowUtils.putDelegatedClientAuthenticationResolvedCredentials(context, List.of(profile));
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, action.execute(context).getId());
    }
}
