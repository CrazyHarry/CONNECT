/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.saml;

import gov.hhs.fha.nhinc.properties.PropertyAccessorFileUtilities;
import gov.hhs.fha.nhinc.util.PasswordUtil;

import java.util.Properties;

/**
 * @author akong
 *
 */
public class SAMLConfigFactory {

    private static final String SAML_PROPERTIES_FILENAME = "saml";

    private static SAMLConfigFactory INSTANCE = null;
    private Properties configuration = null;

    SAMLConfigFactory() {
        this(new PropertyAccessorFileUtilities());
    }

    SAMLConfigFactory(PropertyAccessorFileUtilities propFileUtilities) {
        configuration = propFileUtilities.loadPropertyFile(SAML_PROPERTIES_FILENAME);

        // If the key password is in the configuration, replace it with the decoded version
        if (configuration != null && configuration.containsKey("org.apache.ws.security.saml.issuer.key.password")) {
            configuration.setProperty("org.apache.ws.security.saml.issuer.key.password",
                    PasswordUtil.decode(configuration.getProperty("org.apache.ws.security.saml.issuer.key.password")));
        }
    }

    /**
     * Returns a singleton instance of this factory.
     *
     * @return a singleton instance of this factory.
     */
    public static SAMLConfigFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SAMLConfigFactory();
        }

        return INSTANCE;
    }

    /**
     * Returns a cloned copy of the Properties read from saml.properties.
     *
     * @return a cloned copy of the Properties read from saml.properties.
     */
    public Properties getConfiguration() {
        if (configuration == null) {
            return null;
        } else {
            return (Properties) configuration.clone();
        }
    }
}
