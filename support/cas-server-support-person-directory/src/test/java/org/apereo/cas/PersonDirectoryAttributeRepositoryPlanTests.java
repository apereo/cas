package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.function.Executable;

import static org.apereo.cas.util.junit.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PersonDirectoryAttributeRepositoryPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Attributes")
public class PersonDirectoryAttributeRepositoryPlanTests {

    @Test
    public void verifyOperation() {
        val input = mock(PersonDirectoryAttributeRepositoryPlan.class);
        doCallRealMethod().when(input).registerAttributeRepositories();
        doReturn(new ArrayList<>()).when(input).getAttributeRepositories();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                input.registerAttributeRepositories(CoreAuthenticationTestUtils.getAttributeRepository());
            }
        });
    }

}
