/*
 * Copyright 2002-2004 the original author or authors. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.springframework.ldap.support;

import javax.naming.directory.DirContext;

/**
 * Interface defining a DateSource equivalent in the LDAP world. It defined an
 * implementation able to return DirContext when asked for.
 * 
 * @author Olivier Jolly
 * @see javax.naming.directory.DirContext
 */
public interface ContextSource {

    /**
     * Factory returning a DirContext
     * 
     * @return a DirContext
     */
    public DirContext getDirContext();

    public DirContext getDirContext(String principal, String password);

}