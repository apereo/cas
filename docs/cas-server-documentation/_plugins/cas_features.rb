require "set"

# Groups the feature toggles catalog by area for the Configuration Feature Toggles page.
module Jekyll
  module CasFeaturesFilter
    GROUPS = [
      ["Authentication", "right-to-bracket", %w[Authentication DelegatedAuthentication LDAP PasswordlessAuthn SPNEGO X509 Radius SurrogateAuthentication]],
      ["Multifactor", "key", %w[MultifactorAuthentication MultifactorAuthenticationTrustedDevices GoogleAuthenticator YubiKey RadiusMFA SimpleMFA WebAuthn]],
      ["Protocols", "diagram-project", %w[ACME OAuth OpenIDConnect SAML SAMLIdentityProvider SAMLIdentityProviderMetadata SAMLServiceProviderMetadata WsFederation WsFederationIdentityProvider RestProtocol UMA Validation Tokens]],
      ["Registries & storage", "database", %w[TicketRegistry TicketRegistryLocking ServiceRegistry ServiceRegistryStreaming SessionManagement JDBC CasConfiguration]],
      ["Accounts & consent", "user-gear", %w[AccountManagement AccountRegistration ForgotUsername PasswordManagement PasswordManagementHistory PersonDirectory Provisioning SCIM Consent AcceptableUsagePolicy InterruptNotifications]],
      ["Operations", "chart-line", %w[Audit Events Monitoring Metrics Tracing Logging Reports SpringBootAdmin Palantir Discovery Notifications GeoLocation Electrofence Throttling]],
      ["Platform", "cube", %w[Core WebApplication Webflow Thymeleaf ApacheTomcat Jetty Scripting Multitenancy Logout Authorization CAPTCHA]]
    ].freeze

    TITLES = {
      "YubiKey" => "YubiKey", "WebAuthn" => "WebAuthn", "PasswordlessAuthn" => "Passwordless Authentication",
      "OpenIDConnect" => "OpenID Connect", "OAuth" => "OAuth", "CasConfiguration" => "CAS Configuration",
      "GeoLocation" => "Geolocation", "ApacheTomcat" => "Apache Tomcat", "SpringBootAdmin" => "Spring Boot Admin",
      "MultifactorAuthentication" => "Multifactor Authentication", "MultifactorAuthenticationTrustedDevices" => "MFA Trusted Devices",
      "RadiusMFA" => "RADIUS MFA", "SimpleMFA" => "Simple MFA", "Radius" => "RADIUS", "WsFederation" => "WS-Federation",
      "WsFederationIdentityProvider" => "WS-Federation Identity Provider", "X509" => "X.509", "UMA" => "UMA"
    }.freeze

    DOCS = {
      "ACME" => "integration/ACME-Integration", "AcceptableUsagePolicy" => "webflow/Webflow-Customization-AUP",
      "AccountManagement" => "registration/Account-Management-Overview", "AccountRegistration" => "registration/Account-Registration-Overview",
      "Audit" => "audits/Audits", "Authorization" => "authorization/Authorization-Overview",
      "CasConfiguration" => "configuration/Configuration-Server-Management", "Consent" => "integration/Attribute-Release-Consent",
      "DelegatedAuthentication" => "integration/Delegate-Authentication", "Discovery" => "installation/Service-Discovery-Guide",
      "Events" => "authentication/Configuring-Authentication-Events", "GeoLocation" => "authentication/GeoTracking-Authentication-Requests",
      "GoogleAuthenticator" => "mfa/GoogleAuthenticator-Authentication", "InterruptNotifications" => "webflow/Webflow-Customization-Interrupt",
      "JDBC" => "installation/JDBC-Drivers", "LDAP" => "authentication/LDAP-Authentication", "Logout" => "installation/Logout-Single-Signout",
      "Metrics" => "monitoring/Configuring-Metrics", "Monitoring" => "monitoring/Configuring-Monitoring",
      "MultifactorAuthentication" => "mfa/Configuring-Multifactor-Authentication",
      "MultifactorAuthenticationTrustedDevices" => "mfa/Multifactor-TrustedDevice-Authentication",
      "Multitenancy" => "multitenancy/Multitenancy-Overview", "OAuth" => "protocol/OAuth-Protocol", "OpenIDConnect" => "protocol/OIDC-Protocol",
      "PasswordManagement" => "password_management/Password-Management",
      "PasswordManagementHistory" => "password_management/Password-Management-History",
      "PasswordlessAuthn" => "authentication/Passwordless-Authentication", "PersonDirectory" => "integration/Attribute-Resolution",
      "RadiusMFA" => "mfa/RADIUS-Authentication", "RestProtocol" => "protocol/REST-Protocol",
      "SAMLIdentityProvider" => "authentication/Configuring-SAML2-Authentication", "SCIM" => "integration/SCIM-Provisioning",
      "SPNEGO" => "authentication/SPNEGO-Authentication", "Scripting" => "integration/Apache-Groovy-Scripting",
      "ServiceRegistry" => "services/Service-Management", "SimpleMFA" => "mfa/Simple-Multifactor-Authentication",
      "SurrogateAuthentication" => "authentication/Surrogate-Authentication",
      "Throttling" => "authentication/Configuring-Authentication-Throttling", "Thymeleaf" => "ux/User-Interface-Thymeleaf",
      "TicketRegistry" => "ticketing/Configuring-Ticketing-Components", "TicketRegistryLocking" => "ticketing/Ticket-Registry-Locking",
      "Tracing" => "monitoring/Configuring-Tracing", "UMA" => "protocol/OAuth-UMA-Protocol", "WebAuthn" => "mfa/FIDO2-WebAuthn-Authentication",
      "WsFederation" => "protocol/WS-Federation-Protocol", "X509" => "authentication/X509-Authentication",
      "YubiKey" => "mfa/YubiKey-Authentication", "Jetty" => "installation/Configuring-Servlet-Container-Embedded-Jetty",
      "ApacheTomcat" => "installation/Configuring-Servlet-Container-Embedded"
    }.freeze

    @catalogs = {}

    class << self
      def catalog(data, version, site)
        return [] unless data.is_a?(Hash) && data["features"].is_a?(Hash)
        @catalogs[[data.object_id, version]] ||= build(data["features"], version, site)
      end

      def title(feature)
        TITLES[feature] || feature.gsub(/([a-z\d])([A-Z])/, '\1 \2').gsub(/([A-Z]+)([A-Z][a-z])/, '\1 \2')
      end

      private

      def build(files, version, site)
        features = Hash.new { |hash, key| hash[key] = { "toggles" => {}, "classes" => Set.new } }
        files.values.select { |value| value.is_a?(Array) }.flatten.each do |entry|
          next unless entry.is_a?(Hash) && entry["property"].is_a?(String)
          segments = entry["property"].split(".")
          next unless segments.size >= 3 && segments.first == "CasFeatureModule"
          feature = segments[1]
          record = features[feature]
          record["classes"] << entry["type"].to_s if entry["type"]
          record["toggles"][entry["property"]] ||= {
            "property" => entry["property"],
            "module" => segments[2..-2].join("."),
            "enabledByDefault" => entry["enabledByDefault"] != false,
            "type" => entry["type"].to_s
          }
        end
        pages = documentation_pages(site, version)
        assigned = GROUPS.flat_map { |group| group[2] }
        groups = GROUPS.map { |name, icon, members| [name, icon, members.select { |member| features.key?(member) }] }
        others = features.keys.reject { |feature| assigned.include?(feature) }.sort
        groups << ["Other", "shapes", others] unless others.empty?
        groups.reject { |group| group[2].empty? }.map do |name, icon, members|
          {
            "name" => name,
            "icon" => icon,
            "features" => members.sort_by { |member| title(member).downcase }.map { |member| feature(member, features[member], pages, version) }
          }
        end
      end

      def feature(name, record, pages, version)
        toggles = record["toggles"].values.sort_by { |toggle| [toggle["module"].empty? ? 0 : 1, toggle["module"]] }
        classes = record["classes"].to_a.sort
        docs = DOCS[name]
        {
          "id" => name,
          "title" => title(name),
          "anchor" => "feature-#{name.downcase}",
          "toggles" => toggles,
          "modules" => toggles.map { |toggle| toggle["module"] }.reject(&:empty?),
          "offByDefault" => toggles.count { |toggle| !toggle["enabledByDefault"] },
          "classes" => classes.map { |type| type.split(".").last },
          "classesTitle" => classes.join("\n"),
          "docs" => docs && pages.include?(docs) ? "../#{docs}.html" : nil,
          "searchText" => ([name, title(name)] + toggles.map { |toggle| toggle["property"] }).join(" ").downcase
        }
      end

      def documentation_pages(site, version)
        return Set.new unless site.respond_to?(:pages)
        prefix = "/#{version}/"
        site.pages.map(&:url).select { |url| url.start_with?(prefix) }.map { |url| url.delete_prefix(prefix).delete_suffix(".html") }.to_set
      end
    end

    def cas_feature_catalog(data, version)
      CasFeaturesFilter.catalog(data, version.to_s, @context.registers[:site])
    end
  end
end

Liquid::Template.register_filter(Jekyll::CasFeaturesFilter)
