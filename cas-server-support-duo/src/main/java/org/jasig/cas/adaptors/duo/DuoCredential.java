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

package org.jasig.cas.adaptors.duo;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.Credential;

import java.io.Serializable;

/**
 * Represents the duo credential.
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class DuoCredential implements Credential, Serializable {

    private static final long serialVersionUID = -7570600733132111037L;

    private String username;
    private String signedDuoResponse;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("username", this.username)
                .append("signedDuoResponse", this.signedDuoResponse)
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof DuoCredential)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        final DuoCredential other = (DuoCredential) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(this.username, other.username);
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(97, 31);
        builder.append(this.username);
        return builder.toHashCode();
    }

    @Override
    public String getId() {
        return this.username;
    }

    public String getSignedDuoResponse() {
        return signedDuoResponse;
    }

    public String getUsername() {
        return username;
    }


    public void setUsername(final String username) {
        this.username = username;
    }

    public void setSignedDuoResponse(final String signedDuoResponse) {
        this.signedDuoResponse = signedDuoResponse;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(this.username) && StringUtils.isNotBlank(this.signedDuoResponse);
    }
}
