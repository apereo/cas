/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.validation;

import org.jasig.cas.authentication.UsernamePasswordAuthenticationRequest;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public class BasicAuthenticationRequestValidator implements Validator {

    /**
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(Class clazz) {
        return UsernamePasswordAuthenticationRequest.class.isAssignableFrom(clazz);
    }

    /**
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "required.username", null);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "required.password", null);
    }

}
