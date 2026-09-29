const assert = require("assert");
const https = require("https");
const cas = require("../../cas.js");

const GATEWAY = "https://localhost:4443";
const CASUSER = `Basic ${btoa("casuser:Mellon")}`;
const ALICE = `Basic ${btoa("alice:Mellon")}`;

async function request(path, method, authorization, status, body = undefined) {
    const headers = {"Accept": "application/json", "Content-Type": "application/json"};
    if (authorization !== undefined) {
        headers["Authorization"] = authorization;
    }
    const content = await cas.doRequest(`${GATEWAY}${path}`, method, headers, status, body);
    await cas.log(`${method} ${path}: ${status}`);
    return content;
}

async function assertForwarded(path, method, authorization, body = undefined) {
    const echo = JSON.parse(await request(path, method, authorization, 200, body));
    assert.equal(echo.method, method);
    assert(echo.url.includes(`/anything${path}`), `Unexpected upstream URL ${echo.url}`);
}

async function assertRefused(path, method, authorization, status, error) {
    const content = await request(path, method, authorization, status);
    assert.equal(JSON.parse(content).error, error);
}

function rawRequest(path, authorization) {
    return new Promise((resolve, reject) => {
        const req = https.request({
            host: "localhost", port: 4443, path: path, method: "GET",
            rejectUnauthorized: false, headers: {"Authorization": authorization}
        }, (res) => {
            res.resume();
            res.on("end", () => resolve(res.statusCode));
        });
        req.on("error", reject);
        req.end();
    });
}

(async () => {
    await cas.log("Requests authorized by Heimdall are forwarded upstream");
    await assertForwarded("/api/documents/1?view=full", "GET", CASUSER);
    await assertForwarded("/api/documents", "GET", ALICE);
    await assertForwarded("/api/documents", "POST", CASUSER, JSON.stringify({title: "Draft"}));

    await cas.log("Requests denied by Heimdall are refused by nginx");
    await assertRefused("/api/documents", "POST", ALICE, 403, "Access Denied");
    await assertRefused("/api/documents/1", "DELETE", ALICE, 403, "Access Denied");

    await cas.log("Callers that fail to authenticate are refused");
    await assertRefused("/api/documents", "GET", undefined, 401, "Unauthenticated Request");
    await assertRefused("/api/documents", "GET", `Basic ${btoa("alice:wrong")}`, 401, "Unauthenticated Request");

    await cas.log("A request without a matching Heimdall resource fails; auth_request turns 404 into 500");
    await request("/api/reports", "GET", CASUSER, 500);

    await cas.log("URIs that cannot be embedded in the Heimdall request body are rejected");
    assert.equal(await rawRequest("/api/documents?x=\",\"namespace\":\"OTHER", CASUSER), 400);
    assert.equal(await rawRequest("/api/documents\\1", CASUSER), 400);
})();
