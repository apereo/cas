const assert = require("assert");
const cas = require("../../cas.js");

const BASE_URL = "https://localhost:8443/cas";
const AUTHZEN_URL = `${BASE_URL}/heimdall/authzen`;
const AUTHORIZE_URL = `${BASE_URL}/heimdall/authorize`;
const EVALUATIONS_URL = `${BASE_URL}/heimdall/authzen/evaluations`;
const PEP_CREDENTIALS = `Basic ${btoa("heimdall-pep:pep:s3cret")}`;
const THROTTLE_WINDOW = 4000;

function evaluation(subjectId, resourceType, resourceId, action) {
    return {
        subject: {type: "user", id: subjectId},
        resource: {type: resourceType, id: resourceId},
        action: {name: action}
    };
}

async function post(url, body, headers, status) {
    let response = undefined;
    const content = await cas.doRequest(url, "POST", {
        "Content-Type": "application/json",
        "Accept": "application/json",
        ...headers
    }, status, typeof body === "string" ? body : JSON.stringify(body), (res) => {
        response = res;
    });
    return {response: response, content: content};
}

async function assertDecision(authorization, request, expected, label) {
    const result = await post(AUTHZEN_URL, request, {"Authorization": authorization}, 200);
    assert(result.response.headers["content-type"].startsWith("application/json"));
    const decision = JSON.parse(result.content);
    assert.equal(typeof decision.decision, "boolean", `${label}: decision must be a boolean`);
    assert.equal(decision.decision, expected, `${label}: expected decision ${expected}`);
    await cas.logg(`${label}: decision is ${decision.decision}`);
}

async function assertUnauthenticated(url, request, authorization, label) {
    const headers = authorization === undefined ? {} : {"Authorization": authorization};
    const result = await post(url, request, headers, 401);
    assert(result.response.headers["www-authenticate"] !== undefined, `${label}: missing WWW-Authenticate`);
    await cas.logg(`${label}: caller authentication is refused`);
    await cas.sleep(THROTTLE_WINDOW);
}

async function fetchPepAccessToken() {
    const params = "grant_type=client_credentials";
    return cas.doPost(`${BASE_URL}/oauth2.0/token?${params}`, params, {
        "Content-Type": "application/x-www-form-urlencoded",
        "Authorization": PEP_CREDENTIALS
    }, (res) => {
        assert(res.data.access_token !== undefined);
        return `Bearer ${res.data.access_token}`;
    }, (error) => {
        throw `Unable to obtain an access token for the PEP: ${error}`;
    });
}

async function verifyDecisions(bearer) {
    await cas.log("Evaluating access decisions across namespaces, types, actions and resource ids");
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "document", "doc-1", "can_read"), true, "Reader reads a document");
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "document", "doc-1", "can_write"), false, "Reader writes a document");
    await assertDecision(bearer, evaluation("bob", "document", "doc-1", "can_write"), true, "Editor writes a document with a bearer token");
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "document", "doc-x", "can_read"), false, "Resource id outside the pattern");
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "folder", "doc-1", "can_read"), false, "Unknown resource type");
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "document", "doc-1", "can_delete"), false, "Unknown action");
    await assertDecision(PEP_CREDENTIALS, evaluation("bob", "account", "acct-1", "can_read"), true, "Every matching namespace grants");
    await assertDecision(PEP_CREDENTIALS, evaluation("carol", "account", "acct-1", "can_read"), false, "One matching namespace denies");
    await assertDecision(PEP_CREDENTIALS, evaluation("mallory", "document", "doc-1", "can_read"), false, "Subject without attributes");
    await assertDecision(PEP_CREDENTIALS, evaluation("bob", "report", "r-1", "can_read"), false, "Resource without policies");
}

async function evaluations(request, status = 200) {
    const result = await post(EVALUATIONS_URL, request, {"Authorization": PEP_CREDENTIALS}, status);
    return status === 200 ? JSON.parse(result.content) : undefined;
}

async function verifyEvaluations() {
    await cas.log("Evaluating a batch of access requests with shared defaults");
    const batch = {
        subject: {type: "user", id: "alice"},
        action: {name: "can_read"},
        evaluations: [
            {resource: {type: "document", id: "doc-1"}},
            {resource: {type: "document", id: "doc-2"}, action: {name: "can_write"}},
            {resource: {type: "document", id: "doc-3"}, subject: {type: "user", id: "bob"}, action: {name: "can_write"}},
            {resource: {type: "document"}}
        ]
    };
    const all = await evaluations(batch);
    assert.deepEqual(all.evaluations.map((entry) => entry.decision), [true, false, true, false]);
    assert.equal(all.evaluations[0].context, undefined);
    assert.equal(all.evaluations[1].context.reason, "policy_denied");
    assert.equal(all.evaluations[3].context.error.status, 400);

    const reasons = await evaluations({
        subject: {type: "user", id: "bob"},
        action: {name: "can_read"},
        evaluations: [
            {resource: {type: "folder", id: "f-1"}},
            {resource: {type: "report", id: "r-1"}}
        ]
    });
    assert.deepEqual(reasons.evaluations.map((entry) => entry.context.reason), ["no_matching_resource", "no_policies"]);

    const denyOnFirstDeny = await evaluations({...batch, options: {evaluations_semantic: "deny_on_first_deny"}});
    assert.deepEqual(denyOnFirstDeny.evaluations.map((entry) => entry.decision), [true, false]);

    const permitOnFirstPermit = await evaluations({...batch, evaluations: batch.evaluations.slice(1),
        options: {evaluations_semantic: "permit_on_first_permit"}});
    assert.deepEqual(permitOnFirstPermit.evaluations.map((entry) => entry.decision), [false, true]);

    const single = await evaluations({...evaluation("alice", "document", "doc-1", "can_read"), evaluations: []});
    assert.equal(single.decision, true);
    assert.equal(single.evaluations, undefined);

    await evaluations({...batch, options: {evaluations_semantic: "unknown"}}, 400);
}

async function verifySubjectsAndContext() {
    await cas.log("Verifying subject types, qualified attribute names and request context");
    const invoice = (department) => ({
        subject: {type: "service", id: "billing-api", properties: {department: department}},
        resource: {type: "invoice", id: "inv-1"},
        action: {name: "can_read"}
    });
    await assertDecision(PEP_CREDENTIALS, invoice("Finance"), true, "Service subject by its properties");
    await assertDecision(PEP_CREDENTIALS, invoice("Sales"), false, "Service subject with other properties");

    const service = evaluation("bob", "document", "doc-1", "can_write");
    service.subject.type = "service";
    await assertDecision(PEP_CREDENTIALS, service, false, "Non-user subject is not resolved from the directory");

    const channel = evaluation("alice", "channel", "c-1", "can_read");
    channel.context = {channel: "web"};
    let result = await post(AUTHZEN_URL, channel, {"Authorization": PEP_CREDENTIALS, "channel": "api"}, 200);
    assert.equal(JSON.parse(result.content).decision, true, "HTTP headers must not override the request context");
    delete channel.context;
    result = await post(AUTHZEN_URL, channel, {"Authorization": PEP_CREDENTIALS, "channel": "web"}, 200);
    assert.equal(JSON.parse(result.content).decision, false, "HTTP headers must not become request context");
    await cas.logg("Request context comes only from the request body");
}

async function verifyMetadata() {
    await cas.log("Verifying policy decision point metadata");
    const policyDecisionPoint = `${BASE_URL}/heimdall`;
    const wellKnown = "https://localhost:8443/.well-known/authzen-configuration/cas/heimdall";
    const metadata = JSON.parse(await cas.doRequest(wellKnown, "GET", {"Accept": "application/json"}, 200));
    assert.equal(metadata.policy_decision_point, policyDecisionPoint, "PDP identifier must match the discovery URL");
    assert.equal(metadata.access_evaluation_endpoint, AUTHZEN_URL);
    assert.equal(metadata.access_evaluations_endpoint, EVALUATIONS_URL);
    assert.equal(metadata.search_subject_endpoint, undefined);

    const direct = JSON.parse(await cas.doRequest(`${policyDecisionPoint}/.well-known/authzen-configuration`,
        "GET", {"Accept": "application/json"}, 200));
    assert.deepEqual(direct, metadata);

    const result = await post(metadata.access_evaluation_endpoint, evaluation("alice", "document", "doc-1", "can_read"),
        {"Authorization": PEP_CREDENTIALS}, 200);
    assert.equal(JSON.parse(result.content).decision, true);
}

async function verifyProtocol() {
    await cas.log("Verifying request identifiers, forward compatibility and malformed requests");
    const requestId = crypto.randomUUID();
    const result = await post(AUTHZEN_URL, evaluation("alice", "document", "doc-2", "can_read"), {
        "Authorization": PEP_CREDENTIALS,
        "X-Request-ID": requestId
    }, 200);
    assert.equal(result.response.headers["x-request-id"], requestId);

    const extended = evaluation("alice", "document", "doc-3", "can_read");
    extended.subject.properties = {department: "Sales"};
    extended.resource.properties = {owner: "bob"};
    extended.action.properties = {method: "GET"};
    extended.context = {time: new Date().toISOString()};
    extended.unknownField = {name: "value"};
    await assertDecision(PEP_CREDENTIALS, extended, true, "Unknown fields and properties are ignored");

    const missingSubjectType = evaluation("alice", "document", "doc-1", "can_read");
    delete missingSubjectType.subject.type;
    await post(AUTHZEN_URL, missingSubjectType, {"Authorization": PEP_CREDENTIALS}, 400);
    const missingAction = evaluation("alice", "document", "doc-1", "can_read");
    delete missingAction.action;
    await post(AUTHZEN_URL, missingAction, {"Authorization": PEP_CREDENTIALS}, 400);
    const missingResourceId = evaluation("alice", "document", "doc-1", "can_read");
    delete missingResourceId.resource.id;
    await post(AUTHZEN_URL, missingResourceId, {"Authorization": PEP_CREDENTIALS}, 400);
    await post(AUTHZEN_URL, "{}", {"Authorization": PEP_CREDENTIALS}, 400);
}

async function verifyLegacyEndpoint(bearer) {
    await cas.log("Verifying the Heimdall authorization endpoint is unaffected by AuthZEN");
    const legacy = {method: "POST", uri: "/api/legacy", namespace: "LEGACY"};
    const user = `Basic ${btoa("casuser:Mellon")}`;
    await post(AUTHORIZE_URL, legacy, {"Authorization": user}, 200);
    await post(AUTHORIZE_URL, legacy, {"Authorization": bearer}, 403);
    await post(AUTHORIZE_URL, {...legacy, ...evaluation("alice", "document", "doc-1", "can_read")}, {"Authorization": user}, 400);
}

async function verifyCallerAuthentication() {
    await cas.log("Verifying caller authentication failures");
    const request = evaluation("alice", "document", "doc-1", "can_read");
    await assertUnauthenticated(AUTHZEN_URL, request, undefined, "Missing credentials");
    await assertUnauthenticated(AUTHZEN_URL, request, `Basic ${btoa("heimdall-pep:wrong")}`, "Wrong client secret");
    await assertUnauthenticated(AUTHZEN_URL, request, `Basic ${btoa("casuser:Mellon")}`, "CAS user credentials on AuthZEN");
    await assertUnauthenticated(AUTHZEN_URL, request, `Basic ${btoa("blocked-pep:blocked-secret")}`, "Client refused by the Heimdall access strategy");
    await assertUnauthenticated(AUTHZEN_URL, request, "Bearer AT-1-unknown", "Unknown access token");
    await assertUnauthenticated(AUTHORIZE_URL, {method: "POST", uri: "/api/legacy", namespace: "LEGACY"},
        `Basic ${btoa("casuser:wrong")}`, "Wrong CAS user password");
}

async function verifyThrottling() {
    await cas.log("Verifying that guessing client secrets is throttled while decisions are not");
    const request = evaluation("alice", "document", "doc-1", "can_write");
    await assertDecision(PEP_CREDENTIALS, request, false, "Denied decision before guessing");
    await assertDecision(PEP_CREDENTIALS, request, false, "Denied decision does not count as a failure");
    await post(AUTHZEN_URL, request, {"Authorization": `Basic ${btoa("heimdall-pep:guess-1")}`}, 401);
    await post(AUTHZEN_URL, request, {"Authorization": `Basic ${btoa("heimdall-pep:guess-2")}`}, 423);
    await cas.sleep(THROTTLE_WINDOW);
    await assertDecision(PEP_CREDENTIALS, evaluation("alice", "document", "doc-1", "can_read"), true, "Access resumes after the throttle window");
}

async function verifyResourcesEndpoint() {
    await cas.log("Verifying AuthZEN fields of authorizable resources");
    const content = await cas.doRequest(`${BASE_URL}/actuator/heimdall/resources`, "GET", {"Accept": "application/json"}, 200);
    const resources = JSON.parse(content);
    const documents = resources["DOCUMENTS"];
    assert(Array.isArray(documents) && documents.length === 2);
    const read = documents.find((resource) => resource.id === 1);
    assert.equal(read.resourceType, "document");
    assert.equal(read.resourceIdPattern, "doc-[0-9]+");
    const actions = Array.isArray(read.actions[1]) ? read.actions[1] : read.actions;
    assert(actions.includes("can_read"));
    assert(resources["LEGACY"] !== undefined);
}

(async () => {
    await verifyMetadata();
    const bearer = await fetchPepAccessToken();
    await verifyDecisions(bearer);
    await verifyEvaluations();
    await verifySubjectsAndContext();
    await verifyProtocol();
    await verifyLegacyEndpoint(bearer);
    await verifyResourcesEndpoint();
    await verifyCallerAuthentication();
    await verifyThrottling();
})();
