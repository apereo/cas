/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import java.io.Serializable;

/**
 * Generic concept of an authenticated thing. Examples include a person or a
 * service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface Principal extends Serializable {

    String getId();
}
