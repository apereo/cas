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

package org.apereo.cas;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A repository that should be monitored.
 *
 * @author Andy Wilkinson
 */
@RequiredArgsConstructor
@Getter
public class MonitoredRepository {

    /**
     * The name of the organization that owns the repository.
     */
    private final String organization;

    /**
     * The name of the repository.
     */
    private final String name;

}
