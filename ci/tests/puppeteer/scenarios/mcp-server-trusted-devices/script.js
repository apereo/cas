const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;
const MCP_OPERATOR = `Basic ${btoa("mcpoperator:Mellon")}`;

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

    await cas.separator();
    const otp = await callTool(sessionId, "findOneTimeTokenAccounts", {username: "casuser"});
    assert(otp.count === 1);
    assert(otp.accounts[0].name === "RecordName");
    assert(otp.accounts[0].scratchCodesLeft > 0);
    const rendered = JSON.stringify(otp);
    assert(!rendered.includes("S3cr3t198$") && !rendered.includes(scratch) && !rendered.includes("999666"),
        "Secret keys, validation codes and scratch codes must not be returned");

    await cas.separator();
    const denied = await send(sessionId, "tools/call", {name: "removeTrustedDevices", arguments: {username: "casuser"}});
    assert(denied.isError === true, "Tools that change CAS must require the MCP_OPERATOR role");

    const operator = await initialize(MCP_OPERATOR);
    const operatorTools = (await send(operator, "tools/list", {}, MCP_OPERATOR)).tools.map((tool) => tool.name);
    assert(operatorTools.includes("removeTrustedDevices") && operatorTools.includes("revokeSessions"),
        "Tools that change CAS must be listed when the mcp-write feature is turned on");

    const sessions = await callTool(operator, "findSessions", {username: "casuser"}, MCP_OPERATOR);
    assert(sessions.count >= 1);
    const revoked = await callTool(operator, "revokeSessions", {username: "casuser"}, MCP_OPERATOR);
    assert(revoked.count === sessions.count);
    assert((await callTool(operator, "findSessions", {username: "casuser"}, MCP_OPERATOR)).count === 0);

    const removed = await callTool(operator, "removeTrustedDevices", {username: "casuser", deviceId: device.id}, MCP_OPERATOR);
    assert(removed.removed === 1);
    assert((await callTool(operator, "findTrustedDevices", {username: "casuser"}, MCP_OPERATOR)).count === 0);

    const debug = await callTool(operator, "setLogLevel", {loggerName: "org.apereo.cas.trusted", level: "DEBUG"}, MCP_OPERATOR);
    assert(debug.level === "DEBUG");
    const reset = await callTool(operator, "setLogLevel", {loggerName: "org.apereo.cas.trusted"}, MCP_OPERATOR);
    assert(reset.previousLevel === "DEBUG" && reset.level !== undefined);
})();
