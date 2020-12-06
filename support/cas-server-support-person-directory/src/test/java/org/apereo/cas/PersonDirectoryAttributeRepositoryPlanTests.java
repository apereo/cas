package org.apereo.cas;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlan;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
