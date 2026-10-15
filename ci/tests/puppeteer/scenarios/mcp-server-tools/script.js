const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;
const MCP_USER = `Basic ${btoa("mcpuser:Mellon")}`;
const CAS_TOOLS = ["evaluateAccess", "evaluateMultifactorTriggers", "findAuditRecords", "findPerson", "findService",
    "findSessions", "findSessionsWithAttribute", "findTenant", "findTicket", "getRegistryStatistics",
    "listMultifactorProviders", "matchService", "previewAttributeRelease", "previewLogout", "searchServices",
    "findAuthenticationEvents", "findSurrogateAccounts"];

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
    return {error: result.isError === true, text: text};
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

    const search = JSON.parse((await callTool(MCP_ADMIN, sessionId, "searchServices", {query: "anything"})).text);
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
    assert(triggers.provider === null);
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
    assert(ticketGrantingTicket.text.includes(ticketGrantingTicketId));
    assert(ticketGrantingTicket.text.includes("casuser"));
    assert(ticketGrantingTicket.text.includes("casuser@example.org"));

    const sessions = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSessions", {username: "casuser"})).text);
    assert(sessions.count >= 1);
    assert(sessions.sessions.some((session) => session.id === ticketGrantingTicketId));

    const byAttribute = JSON.parse((await callTool(MCP_ADMIN, sessionId, "findSessionsWithAttribute",
        {attributeName: "mail", attributeValue: "casuser@example.org"})).text);
    assert(byAttribute.sessions.some((session) => session.id === ticketGrantingTicketId && session.principal === "casuser"));
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
    const logout = JSON.parse((await callTool(MCP_ADMIN, sessionId, "previewLogout", {ticketId: ticketGrantingTicketId})).text);
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
    assert(removedTicket.text === "null");

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
        {name: "findSurrogateAccounts", args: {username: "casuser"}}
    ]) {
        const denied = await callTool(MCP_USER, userSessionId, call.name, call.args);
        assert(denied.error, `Tool ${call.name} must be denied for users without the MCP_ADMIN role`);
    }
})();
