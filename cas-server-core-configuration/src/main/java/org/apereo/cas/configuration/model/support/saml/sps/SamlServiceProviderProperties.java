package org.apereo.cas.configuration.model.support.saml.sps;

/**
 * This is {@link SamlServiceProviderProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlServiceProviderProperties {
    private Dropbox dropbox = new Dropbox();

    public Dropbox getDropbox() {
        return dropbox;
    }

    public void setDropbox(final Dropbox dropbox) {
        this.dropbox = dropbox;
    }

    public static class Dropbox {
        private String metadata;
        private String name = "Dropbox";
        private String description = "Dropbox Integration";
        private String nameIdAttribute = "mail";

        public String getNameIdAttribute() {
            return nameIdAttribute;
        }

        public void setNameIdAttribute(final String nameIdAttribute) {
            this.nameIdAttribute = nameIdAttribute;
        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }

        public String getMetadata() {
            return metadata;
        }

        public void setMetadata(final String metadata) {
            this.metadata = metadata;
        }
    }
}
