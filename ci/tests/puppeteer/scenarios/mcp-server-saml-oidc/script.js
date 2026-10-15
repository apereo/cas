const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;

let requestId = 0;

function mcpHeaders(sessionId) {
    const headers = {
        "Content-Type": "application/json",
        "Accept": "application/json, text/event-stream",
        "Authorization": MCP_ADMIN
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

async function initialize() {
    let sessionId = undefined;
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(), 200,
        jsonRpc("initialize", {
            protocolVersion: "2025-11-25",
            capabilities: {},
            clientInfo: {name: "cas-puppeteer", version: "1.0.0"}
        }),
        async (res) => {
            sessionId = res.headers["mcp-session-id"];
        });
    assert(sessionId !== undefined, "MCP session id must be returned");
    await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(sessionId), 202,
        JSON.stringify({jsonrpc: "2.0", method: "notifications/initialized"}));
    return sessionId;
}

async function send(sessionId, method, params) {
    const body = await cas.doRequest(MCP_ENDPOINT, "POST", mcpHeaders(sessionId), 200, jsonRpc(method, params));
    const events = body.split(/\r?\n/)
        .filter((line) => line.startsWith("data:"))
        .map((line) => line.substring("data:".length).trim());
    assert(events.length > 0, `No server-sent event found in ${body}`);
    const response = JSON.parse(events[events.length - 1]);
    assert(response.error === undefined, `Unexpected error: ${JSON.stringify(response.error)}`);
    return response.result;
}

async function callTool(sessionId, name, args) {
    await cas.log(`Calling MCP tool ${name} with ${JSON.stringify(args)}`);
    const result = await send(sessionId, "tools/call", {name: name, arguments: args});
    assert(result.isError !== true, `Tool ${name} failed: ${JSON.stringify(result)}`);
    const text = result.content[0].text;
    await cas.log(`Tool ${name} returned: ${text}`);
    return JSON.parse(text);
}

(async () => {
    const sessionId = await initialize();
    const tools = (await send(sessionId, "tools/list", {})).tools.map((tool) => tool.name);
    await cas.log(`Available tools: ${tools}`);
    for (const tool of ["resolveSamlMetadata", "findOidcClient", "listJwksKeys"]) {
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
    assert(JSON.parse(definitionText).clientSecret === "<redacted>");
    const missingClient = await callTool(sessionId, "findOidcClient", {clientId: "unknown"});
    assert(missingClient.reason !== undefined);

    const keys = await callTool(sessionId, "listJwksKeys", {});
    assert(keys.issuer === "https://localhost:8443/cas/oidc");
    assert(keys.count > 0);
    assert(keys.keys.some((key) => key.state === "CURRENT"));
    const allowed = ["algorithm", "keyId", "keyType", "state", "use"];
    assert(keys.keys.every((key) => Object.keys(key).every((field) => allowed.includes(field))),
        "Only key metadata may be returned");
})();
