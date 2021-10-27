/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.util.Assert;

/**
 * An immutable, serializable ticket validation assertion.
 *
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
public final class ImmutableAssertion implements Assertion, Serializable {

    /** Unique Id for Serialization. */
    private static final long serialVersionUID = -3348826049921010423L;

    /** Primary authentication. */
    private final Authentication primaryAuthentication;

    /** Chained authentications. */
    private final List<Authentication> chainedAuthentications;

    /** Was this the result of a new login. */
    private final boolean fromNewLogin;

    /** The service we are asserting this ticket for. */
    private final Service service;

    /**
     * Creates a new instance with required parameters.
     *
     * @param primary Primary authentication.
     * @param chained Chained authentitications.
     * @param service The service we are asserting this ticket for.
     * @param fromNewLogin True if the ticket was issued as a result of authentication, false otherwise.
     *
     * @throws IllegalArgumentException If any of the given arguments do not meet requirements.
     */
    public ImmutableAssertion(
            final Authentication primary,
            final List<Authentication> chained,
            final Service service,
            final boolean fromNewLogin) {

        Assert.notNull(primary, "primary authentication cannot be null");
        Assert.notNull(chained, "chained authentications cannot be null");
        Assert.notNull(service, "service cannot be null");
        Assert.notEmpty(chained, "chained authentications cannot be empty");

        this.primaryAuthentication = primary;
        this.chainedAuthentications = chained;
        this.service = service;
        this.fromNewLogin = fromNewLogin;
    }

    public Authentication getPrimaryAuthentication() {
        return this.primaryAuthentication;
    }

    public List<Authentication> getChainedAuthentications() {
        return Collections.unmodifiableList(this.chainedAuthentications);
    }

    public boolean isFromNewLogin() {
        return this.fromNewLogin;
    }

    public Service getService() {
        return this.service;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof Assertion)) {
            return false;
        }

        final Assertion a = (Assertion) o;
        return this.primaryAuthentication.equals(a.getPrimaryAuthentication())
                && this.chainedAuthentications.equals(a.getChainedAuthentications())
                && this.service.equals(a.getService())
                && this.fromNewLogin == a.isFromNewLogin();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(15, 11);
        builder.append(this.primaryAuthentication);
        builder.append(this.chainedAuthentications);
        builder.append(this.service);
        builder.append(this.fromNewLogin);
        return builder.toHashCode();
    }

    @Override
    public String toString() {
        return this.primaryAuthentication.toString() + ':' + this.service.toString();
    }
}
