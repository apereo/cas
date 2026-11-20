class CasActuatorEndpoints {
    static endpoints() {
        return PalantirDashboardConfiguration.actuatorEndpoints();
    }

    static health() {
        return this.endpoints().health;
    }

    static discoveryProfile() {
        return this.endpoints().discoveryprofile || this.endpoints().discoveryProfile;
    }

    static casConfig() {
        return this.endpoints().casconfig || this.endpoints().casConfig;
    }

    static env() {
        return this.endpoints().env;
    }

    static info() {
        return this.endpoints().info;
    }

    static throttles() {
        return this.endpoints().throttles;
    }

    static statistics() {
        return this.endpoints().statistics;
    }

    static metrics() {
        return this.endpoints().metrics;
    }

    static prometheus() {
        return this.endpoints().prometheus;
    }

    static auditEvents() {
        return this.endpoints().auditevents || this.endpoints().auditEvents;
    }

    static personDirectory() {
        return this.endpoints().persondirectory || this.endpoints().personDirectory;
    }

    static configProps() {
        return this.endpoints().configprops || this.endpoints().configProps;
    }

    static beans() {
        return this.endpoints().beans || this.endpoints().springBeans;
    }

    static conditions() {
        return this.endpoints().conditions || this.endpoints().springConditions;
    }

    static mfaDevices() {
        return this.endpoints().mfadevices || this.endpoints().mfaDevices;
    }

    static duoPing() {
        return this.endpoints().duoping || this.endpoints().duoPing;
    }

    static duoAdmin() {
        return this.endpoints().duoadmin || this.endpoints().duoAdmin;
    }

    /**
     * The Duo Security account status endpoint. It only reads one username, so CAS may list it by its templated link,
     * such as <code>duoAccountStatus-username</code>; the link without the username template is returned.
     *
     * @returns {string|undefined} the endpoint's address
     */
    static duoAccountStatus() {
        const links = this.endpoints();
        const key = Object.keys(links).find(name => name.toLowerCase() === "duoaccountstatus"
            || name.toLowerCase().startsWith("duoaccountstatus-"));
        return key === undefined ? undefined : String(links[key]).replace(/\/\{[^}]+\}$/, "");
    }

    static gauthCredentialRepository() {
        return this.endpoints().gauthcredentialrepository || this.endpoints().gauthCredentialRepository;
    }

    static webAuthnDevices() {
        return this.endpoints().webauthndevices || this.endpoints().webAuthnDevices;
    }

    static yubikeyAccountRepository() {
        return this.endpoints().yubikeyaccountrepository || this.endpoints().yubikeyAccountRepository;
    }

    static refresh() {
        return this.endpoints().busrefresh || this.endpoints().refresh;
    }

    static shutdown() {
        return this.endpoints().shutdown;
    }

    static restart() {
        return this.endpoints().restart;
    }

    static configurationMetadata() {
        return this.endpoints().configurationmetadata || this.endpoints().configurationMetadata;
    }

    static attributeConsent() {
        return this.endpoints().attributeconsent || this.endpoints().attributeConsent;
    }

    static httpExchanges() {
        return this.endpoints().httpexchanges || this.endpoints().httpExchanges;
    }

    static httpTraces() {
        return this.endpoints().httptraces || this.endpoints().httpTraces;
    }

    static mappings() {
        return this.endpoints().mappings;
    }

    static serviceAccess() {
        return this.endpoints().serviceaccess || this.endpoints().serviceAccess;
    }

    static springWebflow() {
        return this.endpoints().springwebflow || this.endpoints().springWebflow;
    }

    static events() {
        return this.endpoints().events;
    }

    static auditLog() {
        return this.endpoints().auditlog || this.endpoints().auditLog;
    }

    static casFeatures() {
        return this.endpoints().casfeatures || this.endpoints().casFeatures;
    }

    static casModules() {
        return this.endpoints().casmodules || this.endpoints().casModules;
    }

    static loggers() {
        return this.endpoints().loggers;
    }

    static loggingConfig() {
        return this.endpoints().loggingconfig || this.endpoints().loggingConfig;
    }

    static ticketRegistry() {
        return this.endpoints().ticketregistry || this.endpoints().ticketRegistry;
    }

    static heapDump() {
        return this.endpoints().heapdump || this.endpoints().heapDump;
    }

    static heapDumpAnalysis() {
        return this.endpoints().heapdumpanalysis || this.endpoints().heapDumpAnalysis;
    }

    static sessions() {
        return this.endpoints().sessions;
    }

    static ssoSessions() {
        return this.endpoints().ssosessions || this.endpoints().ssoSessions;
    }

    static releaseAttributes() {
        return this.endpoints().releaseattributes || this.endpoints().releaseAttributes;
    }

    static oauthTokens() {
        return this.endpoints().oauthtokens || this.endpoints().oauthTokens;
    }

    static registeredServices() {
        return this.endpoints().registeredservices || this.endpoints().registeredServices;
    }

    static oidcJwks() {
        return this.endpoints().oidcjwks || this.endpoints().oidcJwks;
    }

    static groovyCache() {
        return this.endpoints().groovycache || this.endpoints().groovyCache;
    }
    
    static oauthClientSecrets() {
        const endpoints = this.endpoints();
        return endpoints.oauthClientSecrets
            || endpoints.oauthclientSecrets
            || endpoints.oauthclientsecrets
            || endpoints["oauthClientSecrets-clientId"]
            || endpoints["oauthclientSecrets-clientId"]
            || endpoints["oauthclientsecrets-clientid"]
            || Object.entries(endpoints)
                .find(entry => entry[0].includes("oauthClientSecrets") || `${entry[1]}`.includes("/oauthClientSecrets/"))?.[1];
    }

    static heimdall() {
        return this.endpoints().heimdall;
    }

    static logFile() {
        return this.endpoints().logfile || this.endpoints().logFile;
    }

    static scheduledTasks() {
        return this.endpoints().scheduledtasks || this.endpoints().scheduledTasks;
    }

    static multifactorTrustedDevices() {
        return this.endpoints().multifactoreddevices || this.endpoints().multifactorTrustedDevices;
    }

    static multitenancy() {
        return this.endpoints().multitenancy;
    }

    static clusterTopology() {
        return this.endpoints().clustertopology || this.endpoints().clusterTopology;
    }

    static samlPostProfileResponse() {
        return this.endpoints().samlpostprofileresponse || this.endpoints().samlPostProfileResponse;
    }

    static casValidate() {
        return this.endpoints().casvalidate || this.endpoints().casValidate;
    }

    static samlValidate() {
        return this.endpoints().samlvalidate || this.endpoints().samlValidate;
    }

    static gcpLogs() {
        return this.endpoints().gcplogs || this.endpoints().gcpLogs;
    }

    static cloudWatchLogs() {
        return this.endpoints().cloudwatchlogs || this.endpoints().cloudWatchLogs;
    }

    static attributeDefinitions() {
        return this.endpoints().attributedefinitions || this.endpoints().attributeDefinitions;
    }

    static delegatedClients() {
        return this.endpoints().delegatedclients || this.endpoints().delegatedClients;
    }

    static authenticationHandlers() {
        return this.endpoints().authenticationhandlers || this.endpoints().authenticationHandlers;
    }

    static ticketExpirationPolicies() {
        return this.endpoints().ticketexpirationpolicies || this.endpoints().ticketExpirationPolicies;
    }

    static authenticationPolicies() {
        return this.endpoints().authenticationpolicies || this.endpoints().authenticationPolicies;
    }

    static entityHistory() {
        return this.endpoints().entityhistory || this.endpoints().entityHistory;
    }

    static samlIdpRegisteredServiceMetadata() {
        return this.endpoints().samlidpregisteredservicemetadata || this.endpoints().samlIdpRegisteredServiceMetadata
            || this.endpoints().samlIdPRegisteredServiceMetadata;
    }

    static samlIdpRegisteredServiceMetadataCache() {
        return this.endpoints().samlidpregisteredservicemetadatacache || this.endpoints().samlIdpRegisteredServiceMetadataCache
            || this.endpoints().samlIdPRegisteredServiceMetadataCache;
    }

    static threadDump() {
        return this.endpoints().threaddump || this.endpoints().threadDump;
    }

    static passwordManagement() {
        return this.endpoints().passwordmanagement || this.endpoints().passwordManagement;
    }

    static impersonation() {
        return this.endpoints().impersonation;
    }

    static passwordless() {
        return this.endpoints().passwordless;
    }

    static interrupts() {
        return this.endpoints().interrupts;
    }

    static startup() {
        return this.endpoints().startup;
    }

    static dependencies() {
        return this.endpoints().dependencies;
    }

    /**
     * Whether an endpoint accepts requests that change something. An endpoint whose access level is read-only
     * answers only GET requests; an endpoint Palantir knows nothing about is left to the server to decide.
     *
     * @param name the endpoint id
     * @returns {boolean} false when the endpoint is read-only
     */
    static writable(name) {
        const access = PalantirDashboardConfiguration.actuatorEndpointAccess();
        const key = Object.keys(access).find(id => id.toLowerCase() === String(name ?? "").toLowerCase());
        return key === undefined || access[key] !== "READ_ONLY";
    }

    /**
     * Whether any endpoint is read-only, so that the dashboard has controls to hold back.
     *
     * @returns {boolean} true when at least one endpoint is read-only
     */
    static anyReadOnly() {
        return Object.values(PalantirDashboardConfiguration.actuatorEndpointAccess()).includes("READ_ONLY");
    }

    /**
     * The endpoint a request URL goes to: the endpoint with the longest path that the URL's path starts with.
     *
     * @param url the request URL
     * @returns {string|null} the endpoint id, or null when the URL goes to no known endpoint
     */
    static endpointFor(url) {
        const access = PalantirDashboardConfiguration.actuatorEndpointAccess();
        const links = CasActuatorEndpoints.endpoints();
        let path;
        try {
            path = new URL(url, window.location.href).pathname;
        } catch (error) {
            return null;
        }
        const accessIds = new Set(Object.keys(access).map(id => id.toLowerCase()));
        let match = null;
        let matchLength = -1;
        for (const [name, link] of Object.entries(links)) {
            if (!accessIds.has(name.toLowerCase())) {
                continue;
            }
            let linkPath;
            try {
                linkPath = new URL(link, window.location.href).pathname.replace(/\/$/, "");
            } catch (error) {
                continue;
            }
            if ((path === linkPath || path.startsWith(`${linkPath}/`)) && linkPath.length > matchLength) {
                match = name;
                matchLength = linkPath.length;
            }
        }
        return match;
    }

    static all() {
        return Object.getOwnPropertyNames(CasActuatorEndpoints)
            .filter(name =>
                typeof CasActuatorEndpoints[name] === "function" &&
                !["length", "name", "prototype", "all", "endpoints", "writable", "anyReadOnly", "endpointFor"].includes(name)
            )
            .map(name => ({ name, value: CasActuatorEndpoints[name]() }));
    }
}
