const assert = require("assert");
const cas = require("../../cas.js");

const BASE_URL = "https://localhost:8443/cas/actuator/";
const ADMIN = `Basic ${btoa("casadmin:pa$$w0rd")}`;
const VIEWER = `Basic ${btoa("casviewer:Mellon")}`;
const SERVICE = encodeURIComponent("https://localhost:9859/anything/cas");
const CREDENTIALS = `username=casuser&password=Mellon&service=${SERVICE}`;
const FORM = "application/x-www-form-urlencoded";

/*
 * Each endpoint is called with valid credentials, without credentials and with credentials that lack the
 * required role. "ok" expects a 2xx answer from the administrator; "reachable" only expects the request to get
 * past security without a server error, for operations that need a payload this scenario does not build.
 * The Duo Security endpoints answer an empty result with no Duo provider configured, and the QR device endpoints
 * work against the default repository that authorizes every device.
 */
const ENDPOINTS = [
    {method: "GET", path: "info", expect: "ok"},
    {method: "GET", path: "health", expect: "ok"},
    {method: "GET", path: "beans", expect: "ok"},
    {method: "GET", path: "attributeDefinitions", expect: "ok"},
    {method: "POST", path: `casValidate/validate?${CREDENTIALS}`, expect: "ok", contentType: FORM},
    {method: "POST", path: `casValidate/serviceValidate?${CREDENTIALS}`, expect: "ok", contentType: FORM},
    {method: "POST", path: `casValidate/p3/serviceValidate?${CREDENTIALS}`, expect: "ok", contentType: FORM},
    {method: "POST", path: `samlValidate?${CREDENTIALS}`, expect: "ok"},
    {method: "GET", path: "impersonation/casuser", expect: "ok"},
    {method: "GET", path: "multitenancy/tenants", expect: "reachable"},
    {method: "GET", path: "oauthTokens", expect: "ok"},
    {method: "GET", path: "personDirectory/repositories", expect: "ok"},
    {method: "GET", path: "personDirectory/cache/casuser", expect: "ok"},
    {method: "GET", path: "groovyCache/keys", expect: "ok"},
    {method: "POST", path: "heapDumpAnalysis", expect: "reachable"},
    {method: "GET", path: "duoPing", expect: "ok"},
    {method: "GET", path: "duoAccountStatus/casuser", expect: "ok"},
    {method: "GET", path: "duoAdmin/casuser", expect: "ok"},
    {method: "POST", path: "duoAdmin/bypassCodes?username=casuser", expect: "ok"},
    {method: "GET", path: "qrDevices/casuser", expect: "ok"},
    {method: "POST", path: "qrDevices/casuser/device1", expect: "ok"}
];

async function call(endpoint, authorization) {
    const headers = {
        "Accept": "application/json",
        "Content-Type": endpoint.contentType ?? "application/json"
    };
    if (authorization !== undefined) {
        headers["Authorization"] = authorization;
    }
    let status = 0;
    await cas.doRequest(`${BASE_URL}${endpoint.path}`, endpoint.method, headers, 0,
        endpoint.method === "POST" ? "" : undefined,
        (res) => {
            status = res.statusCode;
        });
    return status;
}

(async () => {
    const failures = [];
    for (const endpoint of ENDPOINTS) {
        await cas.separator();
        await cas.log(`${endpoint.method} ${endpoint.path}`);

        const anonymous = await call(endpoint);
        if (anonymous !== 401) {
            failures.push(`${endpoint.path}: without credentials expected 401 but got ${anonymous}`);
        }
        const viewer = await call(endpoint, VIEWER);
        if (viewer !== 403) {
            failures.push(`${endpoint.path}: without the ADMIN role expected 403 but got ${viewer}`);
        }
        const admin = await call(endpoint, ADMIN);
        const passed = endpoint.expect === "ok"
            ? admin >= 200 && admin < 300
            : admin !== 401 && admin !== 403 && admin < 500;
        if (!passed) {
            failures.push(`${endpoint.path}: with the ADMIN role expected ${endpoint.expect} but got ${admin}`);
        }
        await cas.log(`anonymous=${anonymous}, viewer=${viewer}, admin=${admin}`);
    }
    await cas.separator();
    failures.forEach((failure) => cas.logr(failure));
    assert(failures.length === 0, `${failures.length} actuator endpoint check(s) failed`);
})();
