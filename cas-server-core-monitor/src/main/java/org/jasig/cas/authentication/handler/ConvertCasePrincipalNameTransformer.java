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
package org.jasig.cas.authentication.handler;

import javax.validation.constraints.NotNull;


/**
 * A transformer that converts the form uid to either lowercase or
 * uppercase. The result is also trimmed. The transformer is also able
 * to accept and work on the result of a previous transformer that might
 * have modified the uid, such that the two can be chained.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class ConvertCasePrincipalNameTransformer implements PrincipalNameTransformer {
    private boolean toUpperCase;
    
    @NotNull
    private final PrincipalNameTransformer delegateTransformer;
    
    /**
     * Instantiates a new transformer, while initializing the
     * inner delegate to {@link NoOpPrincipalNameTransformer}.
     */
    public ConvertCasePrincipalNameTransformer() {
        this.delegateTransformer = new NoOpPrincipalNameTransformer();
    }
    
    /**
     * Instantiates a new transformer, accepting an inner delegate.
     *
     * @param delegate the delegate
     */
    public ConvertCasePrincipalNameTransformer(final PrincipalNameTransformer delegate) {
        this.delegateTransformer = delegate;
    }
    
    
    @Override
    public String transform(final String formUserId) {
        final String result = this.delegateTransformer.transform(formUserId.trim()).trim();
        return this.toUpperCase ? result.toUpperCase(): result.toLowerCase();
    }

    public final void setToUpperCase(final boolean toUpperCase) {
        this.toUpperCase = toUpperCase;
    }

}
