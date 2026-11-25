/* The suggestions the wizard offers as fields are typed. */

/**
 * Suggestions for the wizard's text fields whose values usually come from a standard or from what CAS knows, such as
 * SAML2 NameID formats, JOSE algorithms, OAuth token types or the attributes CAS can resolve. A suggestion only helps:
 * the field still takes any value. Fields that hold a comma-separated list are completed one entry at a time.
 */
class PalantirWizardSuggestions {
    static choices(...entries) {
        return entries.map(([value, description = ""]) => ({value, description}));
    }

    static DURATIONS = PalantirWizardSuggestions.choices(
        ["PT30S", "30 seconds"], ["PT1M", "1 minute"], ["PT5M", "5 minutes"], ["PT15M", "15 minutes"],
        ["PT30M", "30 minutes"], ["PT1H", "1 hour"], ["PT8H", "8 hours"], ["P1D", "1 day"], ["P7D", "7 days"],
        ["P30D", "30 days"]);

    static TIME_UNITS = PalantirWizardSuggestions.choices(
        ["SECONDS"], ["MINUTES"], ["HOURS"], ["DAYS"], ["MILLISECONDS"]);

    static HTTP_METHODS = PalantirWizardSuggestions.choices(
        ["GET"], ["POST"], ["PUT"], ["PATCH"], ["DELETE"]);

    static JWS_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["RS256", "RSASSA-PKCS1-v1_5 using SHA-256, the OpenID Connect default"],
        ["RS384", "RSASSA-PKCS1-v1_5 using SHA-384"], ["RS512", "RSASSA-PKCS1-v1_5 using SHA-512"],
        ["PS256", "RSASSA-PSS using SHA-256"], ["PS384", "RSASSA-PSS using SHA-384"], ["PS512", "RSASSA-PSS using SHA-512"],
        ["ES256", "ECDSA using P-256 and SHA-256"], ["ES384", "ECDSA using P-384 and SHA-384"],
        ["ES512", "ECDSA using P-521 and SHA-512"], ["EdDSA", "Edwards-curve signatures, such as Ed25519"],
        ["HS256", "HMAC using SHA-256, keyed with the client secret"], ["HS384", "HMAC using SHA-384"],
        ["HS512", "HMAC using SHA-512"]);

    static JWE_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["RSA-OAEP-256", "RSAES OAEP using SHA-256"], ["RSA-OAEP", "RSAES OAEP using SHA-1"],
        ["ECDH-ES", "Elliptic curve Diffie-Hellman, direct key agreement"],
        ["ECDH-ES+A128KW", "ECDH-ES with AES key wrap, 128 bits"], ["ECDH-ES+A192KW", "ECDH-ES with AES key wrap, 192 bits"],
        ["ECDH-ES+A256KW", "ECDH-ES with AES key wrap, 256 bits"], ["A128KW", "AES key wrap, 128 bits"],
        ["A192KW", "AES key wrap, 192 bits"], ["A256KW", "AES key wrap, 256 bits"],
        ["A128GCMKW", "AES GCM key wrap, 128 bits"], ["A256GCMKW", "AES GCM key wrap, 256 bits"],
        ["dir", "A shared symmetric key used directly"]);

    static JWE_ENCODINGS = PalantirWizardSuggestions.choices(
        ["A128CBC-HS256", "AES-128-CBC with HMAC SHA-256"], ["A192CBC-HS384", "AES-192-CBC with HMAC SHA-384"],
        ["A256CBC-HS512", "AES-256-CBC with HMAC SHA-512"], ["A128GCM", "AES GCM, 128 bits"],
        ["A192GCM", "AES GCM, 192 bits"], ["A256GCM", "AES GCM, 256 bits"]);

    static TOKEN_TYPES = PalantirWizardSuggestions.choices(
        ["urn:ietf:params:oauth:token-type:access_token", "OAuth access token"],
        ["urn:ietf:params:oauth:token-type:refresh_token", "OAuth refresh token"],
        ["urn:ietf:params:oauth:token-type:id_token", "OpenID Connect ID token"],
        ["urn:ietf:params:oauth:token-type:jwt", "A JWT"],
        ["urn:ietf:params:oauth:token-type:saml2", "A SAML 2.0 assertion"],
        ["urn:ietf:params:oauth:token-type:saml1", "A SAML 1.1 assertion"]);

    static XML_SIGNATURE_ALGORITHMS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", "RSA with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha384", "RSA with SHA-384"],
        ["http://www.w3.org/2001/04/xmldsig-more#rsa-sha512", "RSA with SHA-512"],
        ["http://www.w3.org/2007/05/xmldsig-more#sha256-rsa-MGF1", "RSA-PSS with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha256", "ECDSA with SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha384", "ECDSA with SHA-384"],
        ["http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512", "ECDSA with SHA-512"],
        ["http://www.w3.org/2000/09/xmldsig#rsa-sha1", "RSA with SHA-1, for older service providers"]);

    static XML_DIGEST_METHODS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256"],
        ["http://www.w3.org/2001/04/xmldsig-more#sha384", "SHA-384"],
        ["http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512"],
        ["http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1, for older service providers"]);

    static XML_CANONICALIZATIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2001/10/xml-exc-c14n#", "Exclusive canonicalization"],
        ["http://www.w3.org/2001/10/xml-exc-c14n#WithComments", "Exclusive canonicalization, keeping comments"],
        ["http://www.w3.org/TR/2001/REC-xml-c14n-20010315", "Canonical XML 1.0"],
        ["http://www.w3.org/2006/12/xml-c14n11", "Canonical XML 1.1"]);

    static XML_DATA_ENCRYPTIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2009/xmlenc11#aes128-gcm", "AES-128 GCM"],
        ["http://www.w3.org/2009/xmlenc11#aes192-gcm", "AES-192 GCM"],
        ["http://www.w3.org/2009/xmlenc11#aes256-gcm", "AES-256 GCM"],
        ["http://www.w3.org/2001/04/xmlenc#aes128-cbc", "AES-128 CBC, for older service providers"],
        ["http://www.w3.org/2001/04/xmlenc#aes256-cbc", "AES-256 CBC, for older service providers"]);

    static XML_KEY_ENCRYPTIONS = PalantirWizardSuggestions.choices(
        ["http://www.w3.org/2009/xmlenc11#rsa-oaep", "RSA-OAEP"],
        ["http://www.w3.org/2001/04/xmlenc#rsa-oaep-mgf1p", "RSA-OAEP with MGF1 and SHA-1"],
        ["http://www.w3.org/2001/04/xmlenc#kw-aes128", "AES-128 key wrap"],
        ["http://www.w3.org/2001/04/xmlenc#kw-aes256", "AES-256 key wrap"],
        ["http://www.w3.org/2001/04/xmlenc#rsa-1_5", "RSA PKCS#1 v1.5, for older service providers"]);

    static NAME_ID_FORMATS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:persistent", "A stable, opaque identifier per service provider"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:transient", "A one-time identifier"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", "An email address"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "Any identifier the IdP chooses"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName", "An X.509 subject name"],
        ["urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName", "A Windows domain qualified name"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos", "A Kerberos principal name"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:entity", "An entity id"],
        ["urn:oasis:names:tc:SAML:2.0:nameid-format:encrypted", "An encrypted identifier"]);

    static AUTHENTICATION_CONTEXTS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport", "A password over a protected session"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Password", "A password"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:X509", "An X.509 certificate"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:TLSClient", "A TLS client certificate"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Kerberos", "Kerberos"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:Smartcard", "A smart card"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwoFactorContract", "A mobile second factor"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:TimeSyncToken", "A time-based one-time password"],
        ["urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified", "Any authentication"],
        ["https://refeds.org/profile/mfa", "REFEDS multifactor authentication profile"],
        ["https://refeds.org/profile/sfa", "REFEDS single-factor authentication profile"]);

    static SAML_BINDINGS = PalantirWizardSuggestions.choices(
        ["urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST", "HTTP POST"],
        ["urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect", "HTTP Redirect"],
        ["urn:oasis:names:tc:SAML:2.0:bindings:SOAP", "SOAP back channel"]);

    static ATTRIBUTE_NAME_FORMATS = PalantirWizardSuggestions.choices(
        ["uri", "urn:oasis:names:tc:SAML:2.0:attrname-format:uri"],
        ["basic", "urn:oasis:names:tc:SAML:2.0:attrname-format:basic"],
        ["unspecified", "urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified"]);

    static ENTITY_CATEGORIES = PalantirWizardSuggestions.choices(
        ["http://refeds.org/category/research-and-scholarship", "REFEDS Research and Scholarship"],
        ["https://refeds.org/category/code-of-conduct/v2", "REFEDS Data Protection Code of Conduct"],
        ["http://www.geant.net/uri/dataprotection-code-of-conduct/v1", "GÉANT Data Protection Code of Conduct"],
        ["https://refeds.org/category/personalized", "REFEDS Personalized Access"],
        ["https://refeds.org/category/pseudonymous", "REFEDS Pseudonymous Access"],
        ["https://refeds.org/category/anonymous", "REFEDS Anonymous Access"],
        ["https://refeds.org/sirtfi", "Sirtfi security incident response"]);

    static ENTITY_ATTRIBUTE_NAMES = PalantirWizardSuggestions.choices(
        ["http://macedir.org/entity-category", "Entity categories the service provider belongs to"],
        ["http://macedir.org/entity-category-support", "Entity categories the service provider supports"],
        ["urn:oasis:names:tc:SAML:attribute:assurance-certification", "Assurance certifications, such as Sirtfi"],
        ["urn:oasis:names:tc:SAML:profiles:subject-id:req", "The subject identifier the service provider requires"]);

    static ENTITY_ATTRIBUTE_VALUES = [
        ...PalantirWizardSuggestions.ENTITY_CATEGORIES,
        ...PalantirWizardSuggestions.choices(
            ["http://id.incommon.org/category/research-and-scholarship", "InCommon Research and Scholarship"],
            ["http://refeds.org/category/hide-from-discovery", "Hidden from discovery services"],
            ["https://refeds.org/sirtfi2", "Sirtfi version 2"],
            ["subject-id", "Requires a subject identifier"],
            ["pairwise-id", "Requires a pairwise identifier"],
            ["any", "Accepts a subject or pairwise identifier"],
            ["none", "Requires no subject identifier"])
    ];

    static LOCALES = PalantirWizardSuggestions.choices(
        ["en", "English"], ["ar", "Arabic"], ["ca", "Catalan"], ["cs", "Czech"], ["de", "German"], ["es", "Spanish"],
        ["fa", "Persian"], ["fr", "French"], ["hr", "Croatian"], ["it", "Italian"], ["ja", "Japanese"],
        ["mk", "Macedonian"], ["nl", "Dutch"], ["pl", "Polish"], ["pt_BR", "Portuguese (Brazil)"],
        ["pt_PT", "Portuguese (Portugal)"], ["ru", "Russian"], ["sk", "Slovak"], ["sl", "Slovenian"],
        ["sv", "Swedish"], ["tr", "Turkish"], ["uk", "Ukrainian"], ["ur", "Urdu"], ["vi", "Vietnamese"],
        ["zh_CN", "Chinese (Simplified)"], ["zh_TW", "Chinese (Traditional)"]);

    static SECONDS = PalantirWizardSuggestions.choices(
        ["300", "5 minutes"], ["1800", "30 minutes"], ["3600", "1 hour"], ["28800", "8 hours"], ["86400", "1 day"],
        ["604800", "7 days"]);

    static HEADERS = PalantirWizardSuggestions.choices(
        ["Authorization", "Credentials, such as a bearer token or basic authentication"], ["Accept", "The media types accepted"],
        ["Accept-Language", "The languages accepted"], ["Content-Type", "The media type of the body"],
        ["Cache-Control", "Caching directives"], ["User-Agent", "The client software"],
        ["X-Request-ID", "An identifier to trace the request"], ["X-Forwarded-For", "The client address behind a proxy"]);

    static ONE_USE = PalantirWizardSuggestions.choices(["1", "Used once, as the protocol expects"]);

    static OIDC_CLAIMS = PalantirWizardSuggestions.choices(
        ["sub", "Subject"], ["name", "Full name"], ["given_name", "Given name"], ["family_name", "Family name"],
        ["middle_name", "Middle name"], ["nickname", "Casual name"], ["preferred_username", "Preferred username"],
        ["profile", "Profile page"], ["picture", "Profile picture"], ["website", "Web page or blog"],
        ["gender", "Gender"], ["birthdate", "Birthday"], ["zoneinfo", "Time zone"], ["locale", "Locale"],
        ["updated_at", "Last profile update"], ["email", "Email address"], ["email_verified", "Whether the email is verified"],
        ["address", "Postal address"], ["phone_number", "Phone number"],
        ["phone_number_verified", "Whether the phone number is verified"]);

    /**
     * The client ids of the OAuth and OpenID Connect applications CAS has, as audiences a token may be exchanged for.
     *
     * @returns {object[]} the client ids, described by the application's name
     */
    static clientIds() {
        return [...registeredServicesById.values()]
            .filter(service => service?.clientId)
            .map(service => ({value: String(service.clientId), description: String(service.name ?? "")}));
    }

    static attributes() {
        return CasDiscoveryProfile.availableAttributes().map(name => ({value: name, description: "An attribute CAS can resolve"}));
    }

    static timeZones() {
        const zones = typeof Intl.supportedValuesOf === "function" ? Intl.supportedValuesOf("timeZone") : [];
        return [{value: "UTC", description: "Coordinated Universal Time"}, ...zones.map(zone => ({value: zone, description: ""}))];
    }

    static authenticationHandlers() {
        return CasDiscoveryProfile.availableAuthenticationHandlers().map(name => ({value: name, description: "An authentication handler of this server"}));
    }

    /**
     * The external identity providers CAS delegates to, read once from the delegated clients endpoint.
     *
     * @returns {object[]|Promise<object[]>} the providers, or a promise of them while they are read
     */
    static async identityProviders() {
        if (!CasActuatorEndpoints.delegatedClients()) {
            return [];
        }
        const response = await $.get(CasActuatorEndpoints.delegatedClients());
        const names = response && typeof response === "object" && !Array.isArray(response) ? Object.keys(response).sort() : [];
        return names.map(name => ({value: name, description: "An external identity provider of this server"}));
    }

    static serviceProperties() {
        return [].concat(PalantirDashboardConfiguration.serviceProperties() ?? [])
            .map(property => typeof property === "string" ? {value: property, description: ""}
                : {value: String(property?.name ?? ""), description: String(property?.description ?? "")})
            .filter(property => property.value !== "");
    }

    /**
     * Where each SAML2 metadata manager CAS can have stores service provider metadata, by the prefix of the metadata
     * locations it handles.
     */
    static SAML_METADATA_MANAGERS = {
        "mongodb://": "Metadata stored in MongoDB",
        "jdbc://": "Metadata stored in a relational database",
        "redis://": "Metadata stored in Redis",
        "dynamodb://": "Metadata stored in Amazon DynamoDB",
        "awss3://": "Metadata stored in Amazon S3",
        "git://": "Metadata stored in a Git repository",
        "rest://": "Metadata stored behind a REST API",
        "json://": "Metadata stored in a JSON file",
        "memory://": "Metadata stored in memory until CAS restarts"
    };

    /**
     * The kinds of metadata location a SAML2 service provider can have, in the order an operator would weigh them: a
     * URL, a metadata query (MDQ) service, the metadata stores of the managers CAS has, or of every manager CAS can
     * have when it cannot be asked which it has, then a file or a Groovy script. Any other location can still be typed.
     *
     * @returns {Promise<object[]>} the choices
     */
    static async samlMetadataLocations() {
        const readable = typeof PalantirSamlMetadataUpload !== "undefined" && Boolean(CasActuatorEndpoints.samlIdpRegisteredServiceMetadata());
        const managers = readable ? await PalantirSamlMetadataUpload.loadManagers() : [];
        const known = PalantirWizardSuggestions.SAML_METADATA_MANAGERS;
        const stores = readable && PalantirSamlMetadataUpload.managers !== null
            ? [...new Map(managers.filter(manager => manager?.sourceId).map(manager => {
                const prefix = String(manager.sourceId);
                return [prefix.toLowerCase(), {value: prefix, description: known[prefix.toLowerCase()] ?? `Metadata stored by ${manager.name ?? prefix}`}];
            })).values()]
            : Object.entries(known).map(([value, description]) => ({value, description: `${description}, if CAS includes its support`}));
        return [
            ...PalantirWizardSuggestions.choices(
                ["https://", "Metadata downloaded from a URL"],
                ["https://mdq.incommon.org/entities/{0}", "Metadata queried by entity ID from the InCommon MDQ service"],
                ["http://mdq.ukfederation.org.uk/entities/{0}", "Metadata queried by entity ID from the UK federation MDQ service"]),
            ...stores,
            ...PalantirWizardSuggestions.choices(
                ["file:/etc/cas/saml/sp-metadata.xml", "Metadata in a file on the CAS server"],
                ["classpath:sp-metadata.xml", "Metadata in a file packaged with CAS"],
                ["file:/etc/cas/saml/metadata.groovy", "Metadata resolved by a Groovy script, if CAS includes Groovy support"])
        ];
    }

    static multifactorProviders() {
        const providers = Object.entries(CasDiscoveryProfile.multifactorAuthenticationProviders())
            .map(([id, name]) => ({value: id, description: String(name)}));
        return providers.length > 0 ? providers : PalantirWizardSuggestions.choices(
            ["mfa-duo", "Duo Security"], ["mfa-gauth", "Google Authenticator"], ["mfa-webauthn", "WebAuthn"],
            ["mfa-yubikey", "YubiKey"], ["mfa-simple", "One-time codes by email or text message"],
            ["mfa-radius", "RADIUS"], ["mfa-inwebo", "inWebo"], ["mfa-twilio", "Twilio"]);
    }

    /**
     * The suggestions of each field, by the dotted path it writes to, with <code>:key</code> or <code>:value</code>
     * for the two sides of a field that edits a map. A list field is completed one comma-separated entry at a time.
     */
    static FIELDS = {
        "idTokenSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "userInfoSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "jwtAccessTokenSigningAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "introspectionSignedResponseAlg": {source: () => PalantirWizardSuggestions.JWS_ALGORITHMS},
        "backchannelAuthenticationRequestSigningAlg": {
            source: () => PalantirWizardSuggestions.JWS_ALGORITHMS.filter(choice => !choice.value.startsWith("HS"))
        },
        "idTokenEncryptionAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "userInfoEncryptedResponseAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "introspectionEncryptedResponseAlg": {source: () => PalantirWizardSuggestions.JWE_ALGORITHMS},
        "idTokenEncryptionEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "userInfoEncryptedResponseEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "introspectionEncryptedResponseEncoding": {source: () => PalantirWizardSuggestions.JWE_ENCODINGS},
        "tokenEndpointAuthenticationMethod": {source: () => PalantirWizardSuggestions.choices(
            ["client_secret_basic", "Client secret in the HTTP Basic header"],
            ["client_secret_post", "Client secret in the request body"],
            ["client_secret_jwt", "A JWT signed with the client secret"],
            ["private_key_jwt", "A JWT signed with the client's private key"],
            ["tls_client_auth", "A TLS client certificate issued by a trusted authority"],
            ["self_signed_tls_client_auth", "A self-signed TLS client certificate"],
            ["none", "A public client that does not authenticate"])},
        "applicationType": {source: () => PalantirWizardSuggestions.choices(["web", "A web application"], ["native", "A native or mobile application"])},
        "subjectType": {source: () => PalantirWizardSuggestions.choices(
            ["public", "The same subject for every client"], ["pairwise", "A different subject for each sector"])},
        "jwksCacheDuration": {source: () => PalantirWizardSuggestions.DURATIONS},
        "idTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "verifiableCredentialsPolicy.credentialSigningAlgValuesSupported": {
            list: true,
            source: () => PalantirWizardSuggestions.JWS_ALGORITHMS.filter(choice => !choice.value.startsWith("HS"))
        },
        "tokenExchangePolicy.allowedTokenTypes": {list: true, source: () => PalantirWizardSuggestions.TOKEN_TYPES},
        "tokenExchangePolicy.allowedActorTokenTypes": {list: true, source: () => PalantirWizardSuggestions.TOKEN_TYPES},
        "accessTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "accessTokenExpirationPolicy.maxTimeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "refreshTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "deviceTokenExpirationPolicy.timeToKill": {source: () => PalantirWizardSuggestions.DURATIONS},
        "codeExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "serviceTicketExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "serviceTicketExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "proxyTicketExpirationPolicy.timeToLive": {source: () => PalantirWizardSuggestions.DURATIONS},
        "proxyTicketExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "proxyGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds": {source: () => PalantirWizardSuggestions.SECONDS},
        "ticketGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds": {source: () => PalantirWizardSuggestions.SECONDS},
        "proxyPolicy.pattern": {source: () => PalantirWizardSuggestions.choices(
            ["^https://.+", "Any HTTPS proxy callback"], ["^https://([a-z0-9-]+\\.)*example\\.org/.*", "HTTPS callbacks on one domain"])},
        "proxyPolicy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "codeExpirationPolicy.numberOfUses": {source: () => PalantirWizardSuggestions.ONE_USE},
        "tokenExchangePolicy.allowedAudience": {list: true, source: () => PalantirWizardSuggestions.clientIds()},
        "tokenExchangePolicy.requiredActorTokenAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "audience": {list: true, source: () => PalantirWizardSuggestions.clientIds()},
        "idTokenIssuer": {source: () => PalantirWizardSuggestions.choices(
            [`${PalantirDashboardConfiguration.casServerPrefix() ?? ""}/oidc`, "The issuer CAS uses by default"])},
        "realm": {source: () => PalantirWizardSuggestions.choices(["urn:org:apereo:cas:ws:idp:realm-CAS", "The realm CAS uses by default"])},
        "accessStrategy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "accessStrategy.requiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.rejectedAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "properties:key": {source: () => PalantirWizardSuggestions.serviceProperties()},
        "attributeReleasePolicy.headers:key": {source: () => PalantirWizardSuggestions.HEADERS},
        "accessStrategy.rolesAttribute": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.emailAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.firstNameAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.lastNameAttributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "accessStrategy.surrogateRequiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.claimMappings:key": {source: () => PalantirWizardSuggestions.OIDC_CLAIMS},
        "attributeReleasePolicy.claimMappings:value": {source: () => PalantirWizardSuggestions.attributes()},

        "metadataLocation": {source: () => PalantirWizardSuggestions.samlMetadataLocations()},
        "requiredNameIdFormat": {source: () => PalantirWizardSuggestions.NAME_ID_FORMATS},
        "requiredAuthenticationContextClass": {source: () => PalantirWizardSuggestions.AUTHENTICATION_CONTEXTS},
        "logoutResponseBinding": {source: () => PalantirWizardSuggestions.SAML_BINDINGS},
        "signingSignatureAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureBlackListedAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureWhiteListedAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_SIGNATURE_ALGORITHMS},
        "signingSignatureReferenceDigestMethods": {list: true, source: () => PalantirWizardSuggestions.XML_DIGEST_METHODS},
        "signingSignatureCanonicalizationAlgorithm": {source: () => PalantirWizardSuggestions.XML_CANONICALIZATIONS},
        "encryptionDataAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS},
        "encryptionKeyAlgorithms": {list: true, source: () => PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS},
        "encryptionBlackListedAlgorithms": {list: true, source: () => [...PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS, ...PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS]},
        "encryptionWhiteListedAlgorithms": {list: true, source: () => [...PalantirWizardSuggestions.XML_DATA_ENCRYPTIONS, ...PalantirWizardSuggestions.XML_KEY_ENCRYPTIONS]},
        "whiteListBlackListPrecedence": {source: () => PalantirWizardSuggestions.choices(
            ["INCLUDE", "The allowed algorithms win over the blocked ones"], ["EXCLUDE", "The blocked algorithms win over the allowed ones"])},
        "signingCredentialType": {source: () => PalantirWizardSuggestions.choices(
            ["X509", "Sign with an X.509 certificate"], ["BASIC", "Sign with a bare key pair"])},
        "signingKeyAlgorithm": {source: () => PalantirWizardSuggestions.choices(["RSA"], ["EC", "Elliptic curve"])},
        "metadataCriteriaDirection": {source: () => PalantirWizardSuggestions.choices(
            ["INCLUDE", "Keep the entities that match"], ["EXCLUDE", "Remove the entities that match"])},
        "metadataCriteriaRoles": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["SPSSODescriptor", "Service provider role"], ["IDPSSODescriptor", "Identity provider role"])},
        "metadataExpirationDuration": {source: () => PalantirWizardSuggestions.DURATIONS},
        "skewAllowance": {source: () => PalantirWizardSuggestions.DURATIONS.slice(0, 5)},
        "encryptableAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeNameFormats:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeNameFormats:value": {source: () => PalantirWizardSuggestions.ATTRIBUTE_NAME_FORMATS},
        "attributeValueTypes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeValueTypes:value": {source: () => PalantirWizardSuggestions.choices(
            ["XSString", "Text"], ["XSURI", "A URI"], ["XSBoolean", "true or false"], ["XSInteger", "A whole number"],
            ["XSDateTime", "A date and time"], ["XSBase64Binary", "Base64 binary data"],
            ["XSObject", "Values released as they are"], ["NameIDType", "A NameID"])},
        "attributeFriendlyNames:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.entityAttribute": {source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_NAMES},
        "attributeReleasePolicy.entityAttributeValues": {list: true, source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_VALUES},
        "metadataCriteriaEntityAttributes:key": {source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_NAMES},
        "metadataCriteriaEntityAttributes:value": {list: true, source: () => PalantirWizardSuggestions.ENTITY_ATTRIBUTE_VALUES},
        "attributeReleasePolicy.entityAttributeFormat": {source: () => PalantirWizardSuggestions.choices(
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:uri", "URI"],
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:basic", "Basic"],
            ["urn:oasis:names:tc:SAML:2.0:attrname-format:unspecified", "Unspecified"])},

        "protocol": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/ws-sx/ws-trust/200512", "WS-Trust 1.3"],
            ["http://schemas.xmlsoap.org/ws/2005/02/trust", "WS-Trust, February 2005"])},
        "tokenType": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0", "SAML 2.0 token"],
            ["http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV1.1", "SAML 1.1 token"])},
        "namespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://docs.oasis-open.org/ws-sx/ws-trust/200512/", "WS-Trust 1.3"])},
        "addressingNamespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://www.w3.org/2005/08/addressing", "WS-Addressing 1.0"])},
        "policyNamespace": {source: () => PalantirWizardSuggestions.choices(
            ["http://www.w3.org/ns/ws-policy", "WS-Policy 1.5"], ["http://schemas.xmlsoap.org/ws/2004/09/policy", "WS-Policy 1.2"])},
        "wsdlService": {source: () => PalantirWizardSuggestions.choices(["SecurityTokenService", "The CAS security token service"])},
        "wsdlEndpoint": {source: () => PalantirWizardSuggestions.choices(["TransportUT_Port", "The CAS security token service endpoint"])},

        "locale": {source: () => PalantirWizardSuggestions.LOCALES},
        "supportedProtocols": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["CAS10", "CAS protocol 1.0"], ["CAS20", "CAS protocol 2.0"], ["CAS30", "CAS protocol 3.0"], ["SAML1", "SAML 1.1"])},
        "accessStrategy.zoneId": {source: () => PalantirWizardSuggestions.timeZones()},
        "accessStrategy.method": {source: () => PalantirWizardSuggestions.HTTP_METHODS},
        "accessStrategy.acceptableResponseCodes": {list: true, source: () => PalantirWizardSuggestions.choices(
            ["200", "OK"], ["201", "Created"], ["202", "Accepted"], ["204", "No content"])},
        "attributeReleasePolicy.method": {source: () => PalantirWizardSuggestions.HTTP_METHODS},
        "attributeReleasePolicy.allowedAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.allowedAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.attribute": {source: () => PalantirWizardSuggestions.choices(
            ["eduPersonTargetedID", "The attribute name eduPerson defines"],
            ["urn:oid:1.3.6.1.4.1.5923.1.1.1.10", "The attribute's OID, as SAML2 service providers expect it"])},
        "attributeReleasePolicy.consentPolicy.includeOnlyAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.activationCriteria.operator": {source: () => PalantirWizardSuggestions.choices(
            ["AND", "Every required attribute must match"], ["OR", "Any required attribute may match"])},
        "attributeReleasePolicy.activationCriteria.requiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.principalAttributesRepository.timeUnit": {source: () => PalantirWizardSuggestions.TIME_UNITS},
        "singleSignOnParticipationPolicy.timeUnit": {source: () => PalantirWizardSuggestions.TIME_UNITS},
        "singleSignOnParticipationPolicy.attributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "multifactorPolicy.multifactorAuthenticationProviders": {list: true, source: () => PalantirWizardSuggestions.multifactorProviders()},
        "multifactorPolicy.multifactorProviderSelectionMappings:key": {source: () => PalantirWizardSuggestions.multifactorProviders()},
        "authenticationPolicy.requiredAuthenticationHandlers": {list: true, source: () => PalantirWizardSuggestions.authenticationHandlers()},
        "authenticationPolicy.excludedAuthenticationHandlers": {list: true, source: () => PalantirWizardSuggestions.authenticationHandlers()},
        "accessStrategy.delegatedAuthenticationPolicy.allowedProviders": {list: true, source: () => PalantirWizardSuggestions.identityProviders()},
        "accessStrategy.activationCriteria.requiredAttributes:key": {source: () => PalantirWizardSuggestions.attributes()},
        "attributeReleasePolicy.consentPolicy.excludedAttributes": {list: true, source: () => PalantirWizardSuggestions.attributes()},
        "webflowInterruptPolicy.attributeName": {source: () => PalantirWizardSuggestions.attributes()},
        "publicKey.algorithm": {source: () => PalantirWizardSuggestions.choices(["RSA", "The algorithm CAS encrypts with by default"])}
    };

    /**
     * The suggestions of a field, if it has any.
     *
     * @param input the field
     * @returns {object|null} the field's suggestions
     */
    static rule(input) {
        const path = String($(input).data("param-name") ?? "");
        const role = input.dataset.mappedFieldRole;
        const key = role === "key" || role === "value" ? `${path}:${role}` : path;
        return PalantirWizardSuggestions.FIELDS[key] ?? null;
    }

    static cache = new Map();

    /**
     * Forget the suggestions read so far, as when the wizard opens, so a field reads what the server knows then.
     */
    static reset() {
        PalantirWizardSuggestions.cache.clear();
    }

    /**
     * The suggestions of a field, read once while the wizard is open rather than with every key typed: a list the
     * server is asked for is asked for once, and one that cannot be read suggests nothing. Each value is listed once,
     * with its text in lower case ready to be matched. A list known without asking the server is ready at once.
     *
     * @param rule the field's suggestions
     * @returns {object[]|Promise<object[]>} the choices, or a promise of them while they are read
     */
    static choicesOf(rule) {
        if (PalantirWizardSuggestions.cache.has(rule)) {
            return PalantirWizardSuggestions.cache.get(rule);
        }
        const prepare = choices => [...new Map((Array.isArray(choices) ? choices : [])
            .filter(choice => choice?.value !== undefined && choice.value !== null && String(choice.value) !== "")
            .map(choice => {
                const value = String(choice.value);
                const description = String(choice.description ?? "");
                return [value, {value, description, search: `${value}\n${description}`.toLowerCase()}];
            })).values()];
        const failed = error => {
            console.debug("The wizard could not read the suggestions of a field", error);
            PalantirWizardSuggestions.cache.set(rule, []);
            return [];
        };
        try {
            const choices = rule.source();
            if (typeof choices?.then !== "function") {
                PalantirWizardSuggestions.cache.set(rule, prepare(choices));
            } else {
                PalantirWizardSuggestions.cache.set(rule, choices.then(list => {
                    const ready = prepare(list);
                    PalantirWizardSuggestions.cache.set(rule, ready);
                    return ready;
                }, failed));
            }
        } catch (error) {
            failed(error);
        }
        return PalantirWizardSuggestions.cache.get(rule);
    }

    static filter(choices, term) {
        const wanted = term.trim().toLowerCase();
        return choices
            .filter(choice => wanted === "" || choice.search.includes(wanted))
            .slice(0, 50)
            .map(choice => ({label: choice.value, value: choice.value, description: choice.description}));
    }

    /**
     * Offer suggestions on every field of the wizard that has them, including fields added later, such as another row
     * of a map, as each field gets focus.
     *
     * @param form the wizard's form
     */
    static attach(form) {
        if (!form || typeof $.fn.autocomplete !== "function") {
            return;
        }
        $(form).off("focusin.palantirSuggestions").on("focusin.palantirSuggestions", "input[type=text][data-param-name]", event => {
            const input = event.currentTarget;
            const rule = PalantirWizardSuggestions.rule(input);
            if (rule === null) {
                return;
            }
            if (!$(input).autocomplete("instance")) {
                PalantirWizardSuggestions.enable(input, rule);
            }
            $(input).autocomplete("search", PalantirWizardSuggestions.term(input, rule));
        });
    }

    static term(input, rule) {
        const value = String(input.value ?? "");
        return rule.list ? value.split(",").pop().trimStart() : value;
    }

    static enable(input, rule) {
        $(input).attr("autocomplete", "off").autocomplete({
            delay: 0,
            minLength: 0,
            source: (request, respond) => {
                const term = PalantirWizardSuggestions.term(input, rule);
                const choices = PalantirWizardSuggestions.choicesOf(rule);
                if (Array.isArray(choices)) {
                    respond(PalantirWizardSuggestions.filter(choices, term));
                } else {
                    choices.then(list => respond(PalantirWizardSuggestions.filter(list, term)));
                }
            },
            focus: () => false,
            open: () => attachPalantirTextFields($(input).closest(".mdc-text-field")),
            select: (event, ui) => {
                if (rule.list) {
                    const entries = String(input.value ?? "").split(",").map(entry => entry.trim());
                    entries[entries.length - 1] = ui.item.value;
                    input.value = entries.filter(Boolean).join(",");
                } else {
                    input.value = ui.item.value;
                }
                $(input).trigger("input").trigger("change");
                attachPalantirTextFields($(input).closest(".mdc-text-field"));
                return false;
            }
        });
        $(input).autocomplete("instance")._renderItem = (list, item) => $("<li>")
            .addClass("palantir-suggestion")
            .append($("<div>")
                .append($("<code>").text(item.value))
                .append(item.description ? $("<span>").text(item.description) : ""))
            .appendTo(list);
    }
}
