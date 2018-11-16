/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apereo.cas.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * A GitHub user.
 *
 * @author Andy Wilkinson
 */
@Getter
public class User {

    private final String login;

    /**
     * Creates a new {@code User} with the given login.
     *
     * @param login the login
     */
    @JsonCreator
    public User(@JsonProperty("login") final String login) {
        this.login = login;
    }

    @Override
    public String toString() {
        return this.login;
    }

}
