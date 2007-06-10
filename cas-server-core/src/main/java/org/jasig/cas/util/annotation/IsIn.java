/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.util.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * States that the field value must be in the array of values.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.2 $ $Date: 2007/04/10 00:48:49 $
 * @since 3.1
 */
@Target( {ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsIn {

    int[] value();
}
