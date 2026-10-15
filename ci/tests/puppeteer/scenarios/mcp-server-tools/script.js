const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;
const MCP_USER = `Basic ${btoa("mcpuser:Mellon")}`;
const CAS_TOOLS = ["evaluateAccess", "evaluateMultifactorTriggers", "findAuditRecords", "findPerson", "findService",
    "findSessions", "findSessionsWithAttribute", "findTenant", "findTicket", "getRegistryStatistics",
    "listMultifactorProviders", "matchService", "previewAttributeRelease", "previewLogout", "searchServices",
    "findAuthenticationEvents", "findSurrogateAccounts", "searchSettings", "describeDeployment", "findThrottledLogins",
    "findAttributeSources", "findAttributeDefinitions", "validateServiceDefinition", "explainLogin"];
const FREE_FORM_TOOLS = ["findService", "matchService"];

let requestId = 0;

function mcpHeaders(authorization, sessionId) {
    const headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream"
    };
    if (authorization !== undefined) {
        headers["Authorization"] = authorization;
    }
    if (sessionId !== undefined) {
        headers["Mcp-Session-Id"] = sessionId;
    }
    return headers;
}

function jsonRpc(method, params) {
    requestId++;
    return JSON.stringify({jsonrpc: "2.0", id: requestId, method: method, params: params});
}

async function initialize(authorization) {
    let sessionId = undefined;
    const body = await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(authorization), 200,
        jsonRpc("initialize", {
            protocolVersion: "2025-11-25",
            capabilities: {},
            clientInfo: {name: "cas-puppeteer", version: "1.0.0"}
        }),
        async (res) => {
            sessionId = res.headers["mcp-session-id"];
        });
    const response = JSON.parse(body);
    await cas.log(`MCP server: ${JSON.stringify(response.result.serverInfo)}`);
    assert(response.result.serverInfo.name === "Apereo CAS MCP Server");
    assert(response.result.capabilities.tools !== undefined);
    assert(sessionId !== undefined, "MCP session id must be returned");

    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(authorization, sessionId), 202,
        JSON.stringify({jsonrpc: "2.0", method: "notifications/initialized"}));
    return sessionId;
}

async function send(authorization, sessionId, method, params) {
    const body = await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(authorization, sessionId), 200,
        jsonRpc(method, params));
    const events = body.split(/\r?\n/)
        .filter((line) => line.startsWith("data:"))
        .map((line) => line.substring("data:".length).trim());
    assert(events.length > 0, `No server-sent event found in ${body}`);
    const response = JSON.parse(events[events.length - 1]);
    assert(response.error === undefined, `Unexpected error: ${JSON.stringify(response.error)}`);
    return response.result;
}

async function callTool(authorization, sessionId, name, args) {
    await cas.log(`Calling MCP tool ${name} with ${JSON.stringify(args)}`);
    const result = await send(authorization, sessionId, "tools/call", {name: name, arguments: args});
    const text = result.content[0].text;
    await cas.log(`Tool ${name} returned: ${text}`);
    return {error: result.isError === true, text: text, structured: result.structuredContent};
}

async function failLogin(username, expectedStatus) {
    const body = new URLSearchParams({username: username, password: "BadPassw0rd"}).toString();
    await cas.doRequest("https://localhost:8443/cas/v1/users", "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(body)
    }, expectedStatus, body);
}

(async () => {
    await cas.separator();
    await cas.log("Unauthenticated requests to the MCP endpoint must be rejected");
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(), 401, jsonRpc("initialize", {}));
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(`Basic ${btoa("mcpadmin:bad-password")}`), 401, jsonRpc("initialize", {}));

    await cas.separator();
    const sessionId = await initialize(MCP_ADMIN);
    const tools = await send(MCP_ADMIN, sessionId, "tools/list", {});
    const names = tools.tools.map((tool) => tool.name);
    await cas.log(`Available tools: ${names}`);
    for (const optional of ["findConsentDecisions", "resolveSamlMetadata", "findOidcClient", "listJwksKeys", "findTrustedDevices",
        "previewInterrupt", "evaluateAuthenticationRisk", "findOneTimeTokenAccounts", "findWebAuthnDevices",
        "findPasswordlessAccount", "listIdentityProviders", "findAuthorizableResources"]) {
        assert(!names.includes(optional), `Tool ${optional} must not be listed without its module`);
    }
    for (const tool of CAS_TOOLS) {
        assert(names.includes(tool), `Tool ${tool} must be listed`);
        const definition = tools.tools.find((entry) => entry.name === tool);
        assert(definition.annotations.readOnlyHint === true);
        assert(definition.annotations.destructiveHint === false);
        assert((definition.outputSchema !== undefined) === !FREE_FORM_TOOLS.includes(tool), `Unexpected output schema for ${tool}`);
    }

    await cas.separator();
    const service = await callTool(MCP_ADMIN, sessionId, "findService", {serviceId: 1001});
    assert(!service.error);
    const definition = JSON.parse(service.text);
    assert(definition.id === 1001);
    assert(definition.serviceId === "^https://localhost:9859/anything/mcp.*");
    const missingService = await callTool(MCP_ADMIN, sessionId, "findService", {serviceId: 987654});
    assert(missingService.text === "null");

    const matched = await callTool(MCP_ADMIN, sessionId, "matchService", {service: "https://localhost:9859/anything/mcp/login"});
    assert(!matched.error);
    assert(JSON.parse(matched.text).id === 1001);
    const unmatched = await callTool(MCP_ADMIN, sessionId, "matchService", {service: "https://unknown.example.net/app"});
    assert(unmatched.text === "null");

    const searchResult = await callTool(MCP_ADMIN, sessionId, "searchServices", {query: "anything"});
    assert(searchResult.structured.count === 2, "Typed tools must return structured content");
    const search = JSON.parse(searchResult.text);
    assert(search.count === 2);
    assert(search.services.map((entry) => entry.id).sort().join(",") === "1001,1002");
    const searchByName = JSON.parse((await callTool(MCP_ADMIN, sessionId, "searchServices", {query: "restricted"})).text);
    assert(searchByName.count === 1 && searchByName.services[0].id === 1002);

    await cas.separator();
    const allowed = JSON.parse((await callTool(MCP_ADMIN, sessionId, "evaluateAccess",
        {username: "casuser", service: "https://localhost:9859/anything/mcp"})).text);
    assert(allowed.accessAllowed === true);
    assert(allowed.registeredService.id === 1001);
    const denied = JSON.parse((await callTool(MCP_ADMIN, sessionId, "evaluateAccess",
        {username: "casuser", service: "https://localhost:9859/anything/restricted"})).text);
    assert(denied.accessAllowed === false);
    assert(denied.registeredService.id === 1002);
    assert(denied.reason !== undefined && denied.reason.length > 0);
    const unregistered = JSON.parse((await callTool(MCP_ADMIN, sessionId, "evaluateAccess",
        {username: "casuser", service: "https://unknown.example.net/app"})).text);
    assert(unregistered.accessAllowed === false);

    await cas.separator();
    const providers = JSON.parse((await callTool(MCP_ADMIN, sessionId, "listMultifactorProviders", {})).text);
    assert(providers.count === 0);
    const triggers = JSON.parse((await callTool(MCP_ADMIN, sessionId, "evaluateMultifactorTriggers",
        {username: "casuser", service: "https://localhost:9859/anything/mcp"})).text);
    assert(triggers.provider === undefined);
    assert(triggers.registeredService === "MCP");
    const tenant = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findTenant", {tenantId: "unknown"})).text);
    assert(tenant.reason !== undefined && tenant.knownTenants.length === 0);

    await cas.separator();
    const release = JSON.parse((await callTool(MCP_ADMIN, sessionId, "previewAttributeRelease",
        {username: "casuser", service: "https://localhost:9859/anything/mcp"})).text);
    assert(release.releasedUsername === "casuser");
    assert(JSON.stringify(release.releasedAttributes.mail).includes("casuser@example.org"));
    const restrictedRelease = JSON.parse((await callTool(MCP_ADMIN, sessionId, "previewAttributeRelease",
        {username: "casuser", service: "https://localhost:9859/anything/restricted"})).text);
    assert(restrictedRelease.accessAllowed === false);

    await cas.separator();
    const person = await callTool(MCP_ADMIN, sessionId, "findPerson", {username: "casuser"});
    assert(!person.error);
    const principal = JSON.parse(person.text);
    assert(principal.id === "casuser");
    assert(JSON.stringify(principal.attributes.mail).includes("casuser@example.org"));

    await cas.separator();
    const credentials = new URLSearchParams({username: "casuser", password: "Mellon"}).toString();
    const ticketGrantingTicketId = await cas.doRequest("https://localhost:8443/cas/v1/tickets", "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(credentials)
    }, 201, credentials);
    await cas.log(`Created ticket-granting ticket ${ticketGrantingTicketId}`);
    assert(ticketGrantingTicketId.startsWith("TGT-"));

    const ticketGrantingTicket = await callTool(MCP_ADMIN, sessionId, "findTicket", {ticketId: ticketGrantingTicketId});
    assert(!ticketGrantingTicket.error);
    const ticketSummary = JSON.parse(ticketGrantingTicket.text);
    assert(ticketSummary.ticketId === ticketGrantingTicketId);
    assert(ticketSummary.authentication.principal === "casuser");
    assert(ticketSummary.authentication.principalAttributeNames.includes("mail"));
    assert(!ticketGrantingTicket.text.includes("casuser@example.org"), "Attribute values must not be returned");
    const sessionReference = ticketSummary.sessionReference;
    assert(sessionReference.startsWith("session:"));

    const sessions = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSessions", {username: "casuser"})).text);
    assert(sessions.count >= 1);
    assert(sessions.sessions.some((session) => session.sessionReference === sessionReference));
    assert(!JSON.stringify(sessions).includes(ticketGrantingTicketId), "Session ids must not be returned");
    const byReference = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findTicket",
        {ticketId: sessionReference, username: "casuser"})).text);
    assert(byReference.sessionReference === sessionReference && byReference.authentication.principal === "casuser");
    const withoutUsername = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findTicket", {ticketId: sessionReference})).text);
    assert(withoutUsername.reason !== undefined);

    const byAttribute = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSessionsWithAttribute",
        {attributeName: "mail", attributeValue: "casuser@example.org"})).text);
    assert(byAttribute.sessions.some((session) => session.sessionReference === sessionReference && session.principal === "casuser"));
    const noneByAttribute = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSessionsWithAttribute",
        {attributeName: "mail", attributeValue: "nobody@example.org"})).text);
    assert(noneByAttribute.count === 0);

    const serviceTicketBody = new URLSearchParams({service: "https://localhost:9859/anything/mcp"}).toString();
    const serviceTicket = await cas.doRequest(`https://localhost:8443/cas/v1/tickets/${ticketGrantingTicketId}`, "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(serviceTicketBody)
    }, 200, serviceTicketBody);
    assert(serviceTicket.startsWith("ST-"));
    const serviceTicketSummary = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findTicket", {ticketId: serviceTicket})).text);
    assert(serviceTicketSummary.service === "https://localhost:9859/anything/mcp");
    assert(serviceTicketSummary.sessionReference === sessionReference);
    assert(!JSON.stringify(serviceTicketSummary).includes(ticketGrantingTicketId), "Session ids must not be returned");
    const logout = JSON.parse((await callTool(MCP_ADMIN, sessionId, "previewLogout",
        {ticketId: sessionReference, username: "casuser"})).text);
    assert(logout.singleLogoutDisabled === false);
    assert(logout.count === 1);
    assert(logout.applications[0].service === "https://localhost:9859/anything/mcp");
    assert(logout.applications[0].registeredService === "MCP");
    assert(logout.applications[0].notified === true);
    assert(logout.applications[0].logoutUrls[0].logoutType === "BACK_CHANNEL");

    await cas.sleep(1000);
    const events = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findAuthenticationEvents", {username: "casuser"})).text);
    assert(events.count >= 1);
    assert(events.events.some((event) => event.type === "CasTicketGrantingTicketCreatedEvent"));
    assert(events.events.every((event) => event.deviceFingerprint === undefined));

    const statistics = JSON.parse((await callTool(MCP_ADMIN, sessionId, "getRegistryStatistics", {})).text);
    assert(statistics.singleSignOnSessions >= 1);
    assert(statistics.registeredServices === 2);

    await cas.log("The default audit log cannot be queried, so no audit records are expected");
    const audit = await callTool(MCP_ADMIN, sessionId, "findAuditRecords", {username: "casuser"});
    assert(!audit.error);
    assert(JSON.parse(audit.text).count === 0);

    await cas.doDelete(`https://localhost:8443/cas/v1/tickets/${ticketGrantingTicketId}`, 200,
        () => cas.log(`Destroyed ticket-granting ticket ${ticketGrantingTicketId}`),
        (error) => {
            throw error;
        });
    const removedTicket = await callTool(MCP_ADMIN, sessionId, "findTicket", {ticketId: ticketGrantingTicketId});
    assert(JSON.parse(removedTicket.text).reason !== undefined);

    await cas.separator();
    await cas.log("Describe the deployment and explain logins");
    const deployment = JSON.parse((await callTool(MCP_ADMIN, sessionId, "describeDeployment", {})).text);
    assert(deployment.casVersion.length > 0);
    assert(deployment.serverName === "https://localhost:8443");
    assert(deployment.features.includes("AI.mcp"));
    assert(deployment.serviceRegistries.some((registry) => registry.startsWith("JsonServiceRegistry")));
    assert(deployment.attributeRepositories.length >= 1);
    assert(deployment.mcpToolGroups.some((group) => group.feature === "mcp-consent" && group.active === false));
    const explained = JSON.parse((await callTool(MCP_ADMIN, sessionId, "explainLogin",
        {username: "casuser", service: "https://localhost:9859/anything/mcp"})).text);
    assert(explained.outcome === "LOGIN_SUCCEEDS");
    assert(explained.steps.map((step) => step.step).join(",") === "access,risk,multifactor,interrupt,attributeRelease");
    assert(explained.steps.find((step) => step.step === "risk").status === "SKIPPED");
    const explainedDenied = JSON.parse((await callTool(MCP_ADMIN, sessionId, "explainLogin",
        {username: "casuser", service: "https://localhost:9859/anything/restricted"})).text);
    assert(explainedDenied.outcome === "ACCESS_DENIED" && explainedDenied.steps.length === 1);

    const candidate = JSON.stringify({
        "@class": "org.apereo.cas.services.CasRegisteredService",
        serviceId: "^https://localhost:9859/anything/mcp/new.*",
        name: "MCP-New",
        id: 1003,
        evaluationOrder: -1
    });
    const validation = JSON.parse((await callTool(MCP_ADMIN, sessionId, "validateServiceDefinition",
        {definition: candidate, urls: ["https://localhost:9859/anything/mcp/new/login", "https://localhost:9859/anything/mcp"]})).text);
    assert(validation.valid === true);
    assert(validation.urls[0].matches === true && validation.urls[0].currentMatch.id === 1001 && validation.urls[0].winner === "CANDIDATE");
    assert(validation.urls[1].matches === false && validation.urls[1].winner === "EXISTING");
    const invalid = JSON.parse((await callTool(MCP_ADMIN, sessionId, "validateServiceDefinition", {definition: "{\"serviceId\":"})).text);
    assert(invalid.valid === false && invalid.errors[0].startsWith("CAS cannot read"));

    const sources = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findAttributeSources", {username: "casuser"})).text);
    assert(sources.repositories.some((source) => source.found && source.attributeNames.includes("mail")));
    assert(!JSON.stringify(sources).includes("casuser@example.org"), "Attribute values must not be returned");
    const definitions = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findAttributeDefinitions", {})).text);
    assert(definitions.count === 0);

    await cas.log("Failed logins are throttled and visible through MCP");
    await failLogin("throttled-user", 401);
    await failLogin("throttled-user", 423);
    const throttled = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findThrottledLogins", {username: "throttled-user"})).text);
    assert(throttled.settings.failureThreshold === 2);
    assert(throttled.count === 1 && throttled.logins[0].username === "throttled-user");
    assert(throttled.logins[0].lastFailure !== undefined && throttled.auditBased === false);

    await cas.log("Complete registered service ids and read a service as a resource");
    const completion = await send(MCP_ADMIN, sessionId, "completion/complete",
        {ref: {type: "ref/resource", uri: "cas://services/{id}"}, argument: {name: "id", value: "100"}});
    assert(completion.completion.values.includes("1001") && completion.completion.values.includes("1002"));
    const resource = await send(MCP_ADMIN, sessionId, "resources/read", {uri: "cas://services/1001"});
    assert(JSON.parse(resource.contents[0].text).id === 1001);

    await cas.separator();
    const surrogates = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSurrogateAccounts", {username: "casuser"})).text);
    assert(surrogates.anyAccount === false);
    assert(surrogates.accounts.join(",") === "banderson,jsmith");
    const noSurrogates = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSurrogateAccounts", {username: "jsmith"})).text);
    assert(noSurrogates.count === 0);

    await cas.separator();
    await cas.log("Users without the MCP_ADMIN role may list tools, but must not be able to call them");
    const userSessionId = await initialize(MCP_USER);
    const userTools = await send(MCP_USER, userSessionId, "tools/list", {});
    assert(userTools.tools.length >= CAS_TOOLS.length);
    for (const call of [
        {name: "findService", args: {serviceId: 1001}},
        {name: "findPerson", args: {username: "casuser"}},
        {name: "findTicket", args: {ticketId: "TGT-1-unknown"}},
        {name: "matchService", args: {service: "https://localhost:9859/anything/mcp"}},
        {name: "findSessions", args: {username: "casuser"}},
        {name: "evaluateAccess", args: {username: "casuser", service: "https://localhost:9859/anything/mcp"}},
        {name: "previewAttributeRelease", args: {username: "casuser", service: "https://localhost:9859/anything/mcp"}},
        {name: "searchServices", args: {query: "mcp"}},
        {name: "findSessionsWithAttribute", args: {attributeName: "mail", attributeValue: "casuser@example.org"}},
        {name: "findAuditRecords", args: {username: "casuser"}},
        {name: "listMultifactorProviders", args: {}},
        {name: "evaluateMultifactorTriggers", args: {username: "casuser", service: "https://localhost:9859/anything/mcp"}},
        {name: "previewLogout", args: {ticketId: "TGT-1-unknown"}},
        {name: "getRegistryStatistics", args: {}},
        {name: "findTenant", args: {tenantId: "unknown"}},
        {name: "findAuthenticationEvents", args: {username: "casuser"}},
        {name: "findSurrogateAccounts", args: {username: "casuser"}},
        {name: "describeDeployment", args: {}},
        {name: "findThrottledLogins", args: {}},
        {name: "findAttributeSources", args: {username: "casuser"}},
        {name: "findAttributeDefinitions", args: {}},
        {name: "validateServiceDefinition", args: {definition: "{}"}},
        {name: "explainLogin", args: {username: "casuser", service: "https://localhost:9859/anything/mcp"}}
    ]) {
        const denied = await callTool(MCP_USER, userSessionId, call.name, call.args);
        assert(denied.error, `Tool ${call.name} must be denied for users without the MCP_ADMIN role`);
    }
})();
