package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PersonDirectoryAttributeRepositoryPlanTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("AttributeRepository")
class PersonDirectoryAttributeRepositoryPlanTests {

    @Test
    void verifyOperation() {
        val input = mock(PersonDirectoryAttributeRepositoryPlan.class);
        doCallRealMethod().when(input).registerAttributeRepositories();
        doReturn(new ArrayList<>()).when(input).getAttributeRepositories();
        assertDoesNotThrow(() -> input.registerAttributeRepositories(CoreAuthenticationTestUtils.getAttributeRepository()));
    }

}
