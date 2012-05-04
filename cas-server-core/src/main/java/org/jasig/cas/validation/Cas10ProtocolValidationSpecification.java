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
package org.jasig.cas.validation;

/**
 * Validation specification for the CAS 1.0 protocol. This specification checks
 * for the presence of renew=true and if requested, succeeds only if ticket
 * validation is occurring from a new login. Additionally, validation will fail
 * if passed a proxy ticket.
 * 
 * @author Scott Battaglia
 * @author Drew Mazurek
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class Cas10ProtocolValidationSpecification extends
    AbstractCasProtocolValidationSpecification {

    public Cas10ProtocolValidationSpecification() {
        super();
    }

    public Cas10ProtocolValidationSpecification(final boolean renew) {
        super(renew);
    }

    protected boolean isSatisfiedByInternal(final Assertion assertion) {
        return (assertion.getChainedAuthentications().size() == 1);
    }
}
