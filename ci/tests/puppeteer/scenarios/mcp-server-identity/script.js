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
    for (const tool of ["findPasswordlessAccount", "listIdentityProviders", "findAuthorizableResources"]) {
        assert(tools.includes(tool), `Tool ${tool} must be listed when its module is included`);
    }

    await cas.separator();
    const passwordless = await callTool(sessionId, "findPasswordlessAccount", {username: "casuser"});
    assert(passwordless.account.name === "CAS User");
    assert(passwordless.account.email === "c******@example.org");
    assert(passwordless.account.phone === "********23");
    assert(passwordless.account.requestPassword === true);
    assert(!JSON.stringify(passwordless).includes("casuser@example.org"), "Email addresses must be masked");
    const noAccount = await callTool(sessionId, "findPasswordlessAccount", {username: "nobody"});
    assert(noAccount.reason !== undefined);

    await cas.separator();
    const providers = await callTool(sessionId, "listIdentityProviders", {});
    const client = providers.identityProviders.find((provider) => provider.name === "CASServerOne");
    assert(client !== undefined);
    assert(client.displayName === "MyCASOne");
    assert(client.callbackUrl.startsWith("https://localhost:8443/cas/login"));

    await cas.separator();
    const namespaces = await callTool(sessionId, "findAuthorizableResources", {});
    assert(namespaces.knownNamespaces.includes("API"));
    const resources = await callTool(sessionId, "findAuthorizableResources", {namespace: "API"});
    assert(resources.count === 1);
    assert(resources.resources[0].pattern === "/api/reports/.*");
    assert(resources.resources[0].policies[0] === "RequiredAttributesAuthorizationPolicy");
    assert(!JSON.stringify(resources).includes("analysts"), "Policy definitions must not be returned");

    await cas.separator();
    await cas.log("Puppeteer builds skip the CAS settings metadata, so only Spring settings are in the catalog here");
    const search = await callTool(sessionId, "searchSettings", {query: "mcp server name"});
    const found = search.settings.find((entry) => entry.name === "spring.ai.mcp.server.name");
    assert(found !== undefined && found.uri === "cas://settings/spring.ai.mcp.server.name");
    assert(!JSON.stringify(search).includes("Apereo CAS MCP Server"), "Setting values must not be returned");

    const templates = await send(sessionId, "resources/templates/list", {});
    assert(templates.resourceTemplates.some((template) => template.uriTemplate === "cas://settings/{name}"));
    const read = await send(sessionId, "resources/read", {uri: "cas://settings/spring.ai.mcp.server.name"});
    const setting = JSON.parse(read.contents[0].text);
    await cas.log(`Setting: ${JSON.stringify(setting)}`);
    assert(setting.name === "spring.ai.mcp.server.name");
    assert(setting.defined === true);
    assert(setting.setting !== undefined);
    assert(!read.contents[0].text.includes("Apereo CAS MCP Server"), "Setting values must not be returned");

    const completion = await send(sessionId, "completion/complete",
        {ref: {type: "ref/resource", uri: "cas://settings/{name}"}, argument: {name: "name", value: "mcp.server.na"}});
    assert(completion.completion.values.includes("spring.ai.mcp.server.name"));
})();
