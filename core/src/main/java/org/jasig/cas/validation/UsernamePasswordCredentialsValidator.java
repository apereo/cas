/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.validation;

import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * A validator to check if UserNamePasswordCredentials is valid.
 * 
 * @author Scott Battaglia
 * @version $Id: UsernamePasswordCredentialsValidator.java,v 1.2 2005/02/27
 * 05:49:26 sbattaglia Exp $
 */
public class UsernamePasswordCredentialsValidator implements Validator {

    public boolean supports(Class clazz) {
        return UsernamePasswordCredentials.class.isAssignableFrom(clazz);
    }

    public void validate(Object o, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName",
            "required.username", null);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password",
            "required.password", null);
    }
}