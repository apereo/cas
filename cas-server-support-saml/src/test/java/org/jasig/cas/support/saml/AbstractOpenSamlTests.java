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

package org.jasig.cas.support.saml;

import net.shibboleth.utilities.java.support.xml.ParserPool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.*;

/**
 * OpenSaml context loading tests.
 * @author Misagh Moayyed
 * @since 4.1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/META-INF/spring/opensaml-config.xml"})
@WebAppConfiguration()
public abstract class AbstractOpenSamlTests {
    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected OpenSamlConfigBean configBean;

    @Autowired
    protected ParserPool parserPool;

    @Autowired
    @Qualifier("shibboleth.BuilderFactory")
    protected XMLObjectBuilderFactory builderFactory;

    @Autowired
    @Qualifier("shibboleth.MarshallerFactory")
    protected MarshallerFactory marshallerFactory;

    @Autowired
    @Qualifier("shibboleth.UnmarshallerFactory")
    protected UnmarshallerFactory unmarshallerFactory;

    @Test
    public void autowireApplicationContext() {
        assertNotNull(this.applicationContext);
        assertNotNull(this.configBean);
        assertNotNull(this.parserPool);
        assertNotNull(this.builderFactory);
        assertNotNull(this.unmarshallerFactory);
        assertNotNull(this.marshallerFactory);
        assertNotNull(this.configBean.getParserPool());
    }

    @Test
    public void loadStaticContextFactories() {
        assertNotNull(XMLObjectProviderRegistrySupport.getParserPool());
        assertNotNull(XMLObjectProviderRegistrySupport.getBuilderFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getMarshallerFactory());
        assertNotNull(XMLObjectProviderRegistrySupport.getUnmarshallerFactory());
    }


    @Test
    public void ensureParserIsInitialized() throws Exception {
        assertNotNull(this.parserPool);
        assertNotNull(this.parserPool.getBuilder());
    }
}
