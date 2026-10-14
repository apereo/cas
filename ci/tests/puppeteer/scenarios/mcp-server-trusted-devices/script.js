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
    assert(tools.includes("findTrustedDevices"), "Trusted device tools must be listed when the trusted-mfa module is included");

    const before = await callTool(sessionId, "findTrustedDevices", {username: "casuser"});
    assert(before.count === 0);
    const triggers = await callTool(sessionId, "evaluateMultifactorTriggers",
        {username: "casuser", service: "https://localhost:9859/anything/mcp"});
    assert(triggers.provider.id === "mfa-gauth");

    await cas.separator();
    const scratch = await cas.fetchGoogleAuthenticatorScratchCode();
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.log(`Using scratch code ${scratch} to login...`);
    await cas.type(page, "#token", scratch);
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertCookie(page);
    await cas.closeBrowser(browser);

    await cas.separator();
    const after = await callTool(sessionId, "findTrustedDevices", {username: "casuser"});
    assert(after.count === 1);
    const device = after.devices[0];
    assert(device.name !== undefined && device.name.length > 0);
    assert(device.multifactorAuthenticationProvider === "mfa-gauth");
    assert(device.expired === false);
    assert(device.deviceFingerprint === undefined && device.recordKey === undefined,
        "Device fingerprints and record keys must not be returned");
})();
