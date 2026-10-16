const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;
const SERVICE = "https://localhost:9859/anything/consent";

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
    assert(tools.includes("findConsentDecisions"), "Consent tools must be listed when the consent module is included");

    const before = await callTool(sessionId, "findConsentDecisions", {username: "casuser"});
    assert(before.count === 0);

    await cas.separator();
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page, SERVICE);
    await cas.loginWith(page);
    await cas.sleep(1000);
    await cas.assertTextContent(page, "#content h2", "Attribute Consent");
    await cas.click(page, "#confirm");
    await cas.waitForNavigation(page);
    await cas.assertTicketParameter(page);
    await cas.closeBrowser(browser);

    await cas.separator();
    const after = await callTool(sessionId, "findConsentDecisions", {username: "casuser"});
    assert(after.count === 1);
    const decision = after.decisions[0];
    assert(decision.service.startsWith(SERVICE));
    assert(decision.attributes.includes("mail"));
    assert(decision.attributes.includes("cn"));
    assert(!JSON.stringify(after).includes("casuser@example.org"), "Consented attribute values must not be returned");

    await cas.separator();
    const audit = await callTool(sessionId, "findAuditRecords", {username: "casuser"});
    const actions = audit.records.map((record) => record.action);
    await cas.log(`Audited actions: ${actions}`);
    assert(audit.count > 0);
    assert(actions.some((action) => action.startsWith("AUTHENTICATION_SUCCESS")));
    const filtered = await callTool(sessionId, "findAuditRecords", {username: "casuser", action: "AUTHENTICATION_SUCCESS"});
    assert(filtered.count > 0 && filtered.records.every((record) => record.action.includes("AUTHENTICATION_SUCCESS")));

    await cas.separator();
    await cas.log("Every MCP call is recorded in the audit trail under the MCP user who made it");
    const mcpCalls = await callTool(sessionId, "findAuditRecords", {username: "mcpadmin", action: "MCP_TOOL_CALL"});
    assert(mcpCalls.count > 0);
    assert(mcpCalls.records.some((record) => record.action === "MCP_TOOL_CALL_SUCCESS"
        && String(record.resource).includes("operation=findConsentDecisions")));
})();
