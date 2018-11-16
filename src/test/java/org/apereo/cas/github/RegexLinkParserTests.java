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

import org.junit.Test;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for {@link RegexLinkParser}.
 *
 * @author Andy Wilkinson
 */
public class RegexLinkParserTests {

    private final LinkParser linkParser = new RegexLinkParser();

    @Test
    public void emptyInput() {
        assertThat(this.linkParser.parse("").size(), is(0));
    }

    @Test
    public void nullInput() {
        assertThat(this.linkParser.parse(null).size(), is(0));
    }

    @Test
    public void singleLink() {
        Map<String, String> links = this.linkParser.parse("<url>; rel=\"foo\"");
        assertThat(links.size(), is(1));
        assertThat(links, hasEntry("foo", "url"));
    }

    @Test
    public void notALink() {
        Map<String, String> links = this.linkParser.parse("<url>; foo bar");
        assertThat(links.size(), is(0));
    }

    @Test
    public void multipleLinks() {
        Map<String, String> links = this.linkParser
            .parse("<url-one>; rel=\"foo\", <url-two>; rel=\"bar\"");
        assertThat(links.size(), is(2));
        assertThat(links, hasEntry("foo", "url-one"));
        assertThat(links, hasEntry("bar", "url-two"));
    }

}
