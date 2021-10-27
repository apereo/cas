/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Service;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class TicketValidationExceptionTests extends TestCase {

    private static final String CODE = "INVALID_SERVICE";
    
    private Service service = TestUtils.getService();
    
    
    public void testThrowableConstructor() {
        final TicketValidationException t = new TicketValidationException(this.service);
        
        assertSame(CODE, t.getCode());
        assertEquals(this.service, t.getOriginalService());
    }
}
