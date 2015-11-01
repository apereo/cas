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

package org.jasig.cas.support.saml.web.idp.metadata;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * The {@link SamlMetadataController} will attempt
 * to produce saml metadata for CAS as an identity provider.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Controller
@RequestMapping("/idp/metadata")
public final class SamlMetadataController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${cas.samlidp.metadata.location}")
    private File metadataLocation;

    @Value("${cas.samlidp.entityid}")
    private String entityId;

    @Value("${cas.samlidp.hostname}")
    private String hostName;

    @Value("${cas.samlidp.scope}")
    private String scope;

    /**
     * Instantiates a new Saml metadata controller.
     * Required for bean initialization.
     */
    public SamlMetadataController() {
    }

    /**
     * Post constructor placeholder for additional
     * extensions. This method is called after
     * the object has completely initialized itself.
     */
    @PostConstruct
    public void postConstruct() {}

    /**
     * Displays the identity provider metadata.
     * Checks to make sure metadata exists, and if not, generates it first.
     * @param response servlet response
     * @throws IOException the iO exception
     */
    @RequestMapping(method = RequestMethod.GET)
    protected void generateMetadata(final HttpServletResponse response) throws IOException {
        logger.debug("Preparing to generate metadata for entityId [{}]", this.entityId);
        final GenerateSamlMetadata generator = new GenerateSamlMetadata(this.metadataLocation,
                this.hostName, this.entityId, this.scope);
        if (generator.isMetadataMissing()) {
            logger.debug("Metadata does not exist at [{}]. Creating...", this.metadataLocation);
            generator.generate();
        }
        logger.debug("Metadata is available at [{}]", generator.getMetadataFile());

        final String contents = FileUtils.readFileToString(generator.getMetadataFile());
        response.setContentType("text/xml;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        final PrintWriter writer = response.getWriter();
        logger.debug("Producing metadata for the response");
        writer.write(contents);
        writer.flush();
        writer.close();
    }
}
