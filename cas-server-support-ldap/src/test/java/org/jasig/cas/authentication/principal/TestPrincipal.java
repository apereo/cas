/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.authentication.principal;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * Test CAS principal for use with resolver unit tests. Supports loading test cases from JSON.
 *
 * @author Marvin S. Addison
 */
public class TestPrincipal implements Principal {

    private static final Logger LOG = LoggerFactory.getLogger(TestPrincipal.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String userName;

    private String id;

    private Map<String, Object> attributes;

    public String getUserName() {
        return this.userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(final Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public static List<TestPrincipal> loadFromResource(final Resource resource) throws IOException {
        final List<TestPrincipal> principals;
        try {
            LOG.info("Loading test principals from {}", resource);
            principals = MAPPER.readValue(resource.getInputStream(), new TypeReference<List<TestPrincipal>>() {});
        } finally {
            resource.getInputStream().close();
        }
        return principals;
    }
}
