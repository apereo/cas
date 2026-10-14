const cas = require("../../cas.js");
const assert = require("assert");

const MCP_ENDPOINT = "https://localhost:8443/cas/mcp";
const MCP_ADMIN = `Basic ${btoa("mcpadmin:Mellon")}`;
const SERVICE = "https://localhost:9859/anything/mcp";

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
    const tools = (await send(sessionId, "tools/list", {})).tools;
    const interruptTool = tools.find((tool) => tool.name === "previewInterrupt");
    assert(interruptTool !== undefined, "Interrupt tools must be listed when the interrupt module is included");
    assert(interruptTool.annotations.openWorldHint === true);
    assert(tools.some((tool) => tool.name === "evaluateAuthenticationRisk"),
        "Risk tools must be listed when the electrofence module is included");

    await cas.separator();
    const interrupted = await callTool(sessionId, "previewInterrupt", {username: "casuser", service: SERVICE});
    assert(interrupted.registeredService === "MCP");
    assert(interrupted.interrupt.message === "Accept the updated terms of use");
    assert(interrupted.interrupt.block === true);
    assert(interrupted.interrupt.links["Terms of use"] === "https://localhost:9859/anything/terms");
    assert(interrupted.inquirers.some((inquirer) => inquirer.outcome === "INTERRUPT"));
    const quiet = await callTool(sessionId, "previewInterrupt", {username: "someone-else", service: SERVICE});
    assert(quiet.interrupt === null);

    await cas.separator();
    const unknown = await callTool(sessionId, "evaluateAuthenticationRisk",
        {username: "casuser", service: SERVICE, clientIpAddress: "198.51.100.7", userAgent: "curl/8.0"});
    await cas.log("Without recorded sign-ins every attempt is regarded as risky");
    assert(unknown.risky === true);
    assert(unknown.calculators.length === 2);

    const credentials = new URLSearchParams({username: "casuser", password: "Mellon"}).toString();
    const ticketGrantingTicketId = await cas.doRequest("https://localhost:8443/cas/v1/tickets", "POST", {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded",
        "Content-Length": Buffer.byteLength(credentials)
    }, 201, credentials);
    assert(ticketGrantingTicketId.startsWith("TGT-"));
    await cas.sleep(1000);

    const events = await callTool(sessionId, "findAuthenticationEvents", {username: "casuser"});
    const created = events.events.find((event) => event.type === "CasTicketGrantingTicketCreatedEvent");
    assert(created !== undefined);

    const familiar = await callTool(sessionId, "evaluateAuthenticationRisk",
        {username: "casuser", service: SERVICE, clientIpAddress: created.clientIpAddress, userAgent: created.userAgent});
    assert(familiar.risky === false);
    assert(familiar.score === 0);
    const unfamiliar = await callTool(sessionId, "evaluateAuthenticationRisk",
        {username: "casuser", service: SERVICE, clientIpAddress: "198.51.100.7", userAgent: "curl/8.0"});
    assert(unfamiliar.risky === true);
    assert(unfamiliar.score > unfamiliar.threshold);
})();
