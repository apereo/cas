const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;

let requestId = 0;

function mcpHeaders(sessionId, authorization = MCP_ADMIN) {
    const headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
        "Authorization": authorization
    };
    if (sessionId !== undefined) {
        headers["Mcp-Session-Id"] = sessionId;
    }
    return headers;
}

function jsonRpc(method, params) {
    requestId++;
    return JSON.stringify({jsonrpc: "2.0", id: requestId, method: method, params: params});
}

async function initialize(authorization = MCP_ADMIN) {
    let sessionId = undefined;
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(undefined, authorization), 200,
        jsonRpc("initialize", {
            protocolVersion: "2025-11-25",
            capabilities: {},
            clientInfo: {name: "cas-puppeteer", version: "1.0.0"}
        }),
        async (res) => {
            sessionId = res.headers["mcp-session-id"];
        });
    assert(sessionId !== undefined, "MCP session id must be returned");
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(sessionId, authorization), 202,
        JSON.stringify({jsonrpc: "2.0", method: "notifications/initialized"}));
    return sessionId;
}

async function send(sessionId, method, params, authorization = MCP_ADMIN) {
    const body = await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(sessionId, authorization), 200, jsonRpc(method, params));
    const events = body.split(/\r?\n/)
        .filter((line) => line.startsWith("data:"))
        .map((line) => line.substring("data:".length).trim());
    assert(events.length > 0, `No server-sent event found in ${body}`);
    const response = JSON.parse(events[events.length - 1]);
    assert(response.error === undefined, `Unexpected error: ${JSON.stringify(response.error)}`);
    return response.result;
}

async function callTool(sessionId, name, args, authorization = MCP_ADMIN) {
    await cas.log(`Calling MCP tool ${name} with ${JSON.stringify(args)}`);
    const result = await send(sessionId, "tools/call", {name: name, arguments: args}, authorization);
    assert(result.isError !== true, `Tool ${name} failed: ${JSON.stringify(result)}`);
    const text = result.content[0].text;
    await cas.log(`Tool ${name} returned: ${text}`);
    return JSON.parse(text);
}

async function requestAccessToken(scope) {
    const params = new URLSearchParams({
        grant_type: "password",
        client_id: "mcp-agent",
        client_secret: "mcp-agent-secret-value",
        username: "casuser",
        password: "Mellon",
        scope: scope,
        resource: MCP_ENDPOINT
    });
    return cas.doPost("https://localhost:8443/cas/oidc/token", params, {
        "Content-Type": "application/x-www-form-urlencoded"
    }, (res) => res.data.access_token, (error) => {
        throw `Unable to obtain an access token: ${error}`;
    });
}

async function verifyAccessTokens() {
    await cas.separator();
    const resourceMetadataUrl = "https://localhost:8443/cas/.well-known/oauth-protected-resource/mcp";
    const unauthenticated = {"Content-Type": "application/json", "Accept": "application/json, text/event-stream"};
    await cas.doRequest(MCP_ENDPOINT, "POST", unauthenticated, 401, jsonRpc("initialize", {}), async (res) => {
        const challenge = String(res.headers["www-authenticate"]);
        await cas.log(`Challenge: ${challenge}`);
        assert(challenge.includes(`resource_metadata="${resourceMetadataUrl}"`), challenge);
        assert(challenge.includes("scope=\"mcp:read\""), challenge);
        assert(challenge.includes("Basic realm="), challenge);
    });
    const protectedResource = JSON.parse(await cas.doRequest(resourceMetadataUrl, "GET", {"Accept": "application/json"}, 200));
    await cas.log(protectedResource);
    assert(protectedResource.resource === MCP_ENDPOINT);
    assert.deepStrictEqual(protectedResource.authorization_servers, ["https://localhost:8443/cas/oidc"]);
    assert.deepStrictEqual(protectedResource.scopes_supported, ["mcp:read"]);
    assert.deepStrictEqual(protectedResource.bearer_methods_supported, ["header"]);

    const agent = `Bearer ${await requestAccessToken("openid mcp:read")}`;
    const agentSession = await initialize(agent);
    const agentClient = await callTool(agentSession, "findOidcClient", {clientId: "mcp-agent"}, agent);
    assert(agentClient.id === 2004);

    const reader = `Bearer ${await requestAccessToken("openid")}`;
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(undefined, reader), 403, jsonRpc("initialize", {}), async (res) => {
        const challenge = String(res.headers["www-authenticate"]);
        assert(challenge.startsWith("Bearer error=\"insufficient_scope\""), challenge);
    });
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(undefined, "Bearer AT-1-unknown"), 401, jsonRpc("initialize", {}), async (res) => {
        assert(String(res.headers["www-authenticate"]).startsWith("Bearer error=\"invalid_token\""));
    });
}

async function verifyClientIdMetadataDocuments(sessionId) {
    await cas.separator();
    const configuration = JSON.parse(await cas.doRequest("https://localhost:8443/cas/oidc/.well-known/openid-configuration",
        "GET", {"Accept": "application/json"}, 200));
    assert(configuration.client_id_metadata_document_supported === true);
    const clientId = "https://localhost:8443/cas/actuator/info";
    const authorize = `https://localhost:8443/cas/oidc/authorize?response_type=code&client_id=${encodeURIComponent(clientId)}`
        + `&redirect_uri=${encodeURIComponent("http://127.0.0.1:3000/callback")}&scope=openid`
        + "&code_challenge=E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM&code_challenge_method=S256";
    await cas.doRequest(authorize, "GET", {}, 0);
    const client = await callTool(sessionId, "findOidcClient", {clientId: clientId});
    assert(client.reason !== undefined, "A client id on a loopback address must not be registered");
}

(async () => {
    const sessionId = await initialize();
    const tools = (await send(sessionId, "tools/list", {})).tools.map((tool) => tool.name);
    await cas.log(`Available tools: ${tools}`);
    for (const tool of ["resolveSamlMetadata", "findOidcClient", "checkOidcClientResource", "listJwksKeys"]) {
        assert(tools.includes(tool), `Tool ${tool} must be listed when SAML2 and OpenID Connect are included`);
    }
    assert(!tools.includes("findConsentDecisions"));

    await cas.separator();
    const metadata = await callTool(sessionId, "resolveSamlMetadata", {entityId: "https://sp.example.org/mcp"});
    assert(metadata.resolved === true);
    assert(metadata.registeredService.id === 2001);
    const sp = metadata.serviceProvider;
    assert(sp.wantAssertionsSigned === true);
    assert(sp.authnRequestsSigned === false);
    assert(sp.nameIdFormats.includes("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent"));
    assert(sp.assertionConsumerServices.length === 1);
    assert(sp.assertionConsumerServices[0].location === "https://localhost:9859/anything/saml/acs");
    assert(sp.assertionConsumerServices[0].default === true);
    assert(sp.singleLogoutServices[0].location === "https://localhost:9859/anything/saml/slo");
    const signing = sp.keys.find((key) => key.use === "SIGNING");
    assert(signing !== undefined);
    assert(signing.certificates[0].subject.includes("CN=sp.example.org"));
    assert(signing.certificates[0].expired === false);

    const broken = await callTool(sessionId, "resolveSamlMetadata", {entityId: "https://broken.example.org/mcp"});
    assert(broken.resolved === false);
    assert(broken.registeredService.id === 2002);
    assert(broken.error !== undefined || broken.reason !== undefined);

    const unknown = await callTool(sessionId, "resolveSamlMetadata", {entityId: "https://unknown.example.net/sp"});
    assert(unknown.resolved === false && unknown.reason.startsWith("No SAML2 service provider registration"));

    await cas.separator();
    const client = await callTool(sessionId, "findOidcClient", {clientId: "mcp-client"});
    assert(client.id === 2003);
    assert(client.issuer === "https://localhost:8443/cas/oidc");
    assert(client.clientSecretConfigured === true);
    assert(client.scopes.includes("openid"));
    assert(client.supportedGrantTypes.includes("authorization_code"));
    assert(client.idTokenSigningAlg === "RS256");
    assert(!JSON.stringify(client).includes("mcp-client-secret-value"), "Client secrets must not be returned");
    const definition = await send(sessionId, "tools/call", {name: "findService", arguments: {serviceId: 2003}});
    const definitionText = definition.content[0].text;
    assert(!definitionText.includes("mcp-client-secret-value"), "Client secrets must be redacted from service definitions");
    assert(JSON.parse(definitionText).clientSecrets === "<redacted>");
    const missingClient = await callTool(sessionId, "findOidcClient", {clientId: "unknown"});
    assert(missingClient.reason !== undefined);
    assert.deepStrictEqual(client.allowedResources, ["https://localhost:9859/anything/(api|reports)"]);

    const allowedResource = await callTool(sessionId, "checkOidcClientResource",
        {clientId: "mcp-client", resource: "https://localhost:9859/anything/api"});
    assert(allowedResource.allowed === true);
    assert(allowedResource.matchedPattern === "https://localhost:9859/anything/(api|reports)");
    assert.deepStrictEqual(allowedResource.audience, ["mcp-client", "https://localhost:9859/anything/api"]);
    const refusedResource = await callTool(sessionId, "checkOidcClientResource",
        {clientId: "mcp-client", resource: "https://localhost:9859/anything/admin"});
    assert(refusedResource.allowed === false);
    assert(refusedResource.reason !== undefined);
    const mcpResource = await callTool(sessionId, "checkOidcClientResource",
        {clientId: "mcp-client", resource: "https://localhost:8443/cas/mcp"});
    assert(mcpResource.allowed === true);
    assert(mcpResource.matchedBy === "cas.authn.oauth.core.allowed-resources");
    assert(allowedResource.matchedBy === "client");

    const keys = await callTool(sessionId, "listJwksKeys", {});
    assert(keys.issuer === "https://localhost:8443/cas/oidc");
    assert(keys.count > 0);
    assert(keys.keys.some((key) => key.state === "CURRENT"));
    const allowed = ["algorithm", "keyId", "keyType", "state", "use"];
    assert(keys.keys.every((key) => Object.keys(key).every((field) => allowed.includes(field))),
        "Only key metadata may be returned");

    await verifyAccessTokens();
    await verifyClientIdMetadataDocuments(sessionId);
})();
