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

package io.spring.issuebot.triage;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * A repository that should be monitored.
 *
 * @author Andy Wilkinson
 */
@Getter
@Setter
public class MonitoredRepository {

	/**
	 * The name of the organization that owns the repository.
	 */
	private String organization;

	/**
	 * The name of the repository.
	 */
	private String name;

	/**
	 * The names of the project collaborators whose issues do not require triage.
	 */
	private List<String> collaborators;

	/**
	 * The name of the label that should be applied to issues that are waiting for triage.
	 */
	private String label;

}
