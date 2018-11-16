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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * A {@code LinkParser} that uses a regular expression to parse the header.
 *
 * @author Andy Wilkinson
 */
public class RegexLinkParser implements LinkParser {

	private static final Pattern LINK_PATTERN = Pattern.compile("<(.+)>; rel=\"(.+)\"");

	@Override
	public Map<String, String> parse(String input) {
		Map<String, String> links = new HashMap<>();
		for (String link : StringUtils.commaDelimitedListToStringArray(input)) {
			Matcher matcher = LINK_PATTERN.matcher(link.trim());
			if (matcher.matches()) {
				links.put(matcher.group(2), matcher.group(1));
			}
		}
		return links;
	}

}
