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

import java.util.Map;

/**
 * A {@code LinkParser} can be used to parse the
 * <a href="https://developer.github.com/v3/#link-header">{@code Link} header</a> that is
 * returned by the GitHub API.
 *
 * @author Andy Wilkinson
 */
interface LinkParser {

    /**
     * Parse the given {@code header} into a map of rel:url pairs.
     *
     * @param header the header to parse
     * @return the map of links
     */
    Map<String, String> parse(String header);

}
