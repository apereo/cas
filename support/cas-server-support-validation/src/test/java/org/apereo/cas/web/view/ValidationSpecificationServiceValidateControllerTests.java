package org.apereo.cas.web.view;

import org.apereo.cas.mock.MockValidationSpecification;
import org.apereo.cas.validation.ValidationSpecification;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;

/**
 * This is {@link ValidationSpecificationServiceValidateControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ValidationSpecificationServiceValidateControllerTests extends Cas20ResponseViewTests {

    @Override
    protected ValidationSpecification getValidationSpecification() {
        return new MockValidationSpecification(false);
    }

    @Test
    public void verifyValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        assertFalse(this.serviceValidateController.handleRequestInternal(getHttpServletRequest(),
                new MockHttpServletResponse()).getView().toString().contains(SUCCESS));
    }
}
