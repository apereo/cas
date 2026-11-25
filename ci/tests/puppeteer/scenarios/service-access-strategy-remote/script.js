
const cas = require("../../cas.js");
const express = require("express");
const assert = require("assert");

const PDP_USERS = {
    pdpallow: "allow",
    pdpdeny: "deny",
    pdperror: "error",
    pdpgarbage: "garbage",
    pdpdrop: "drop"
};

const PDPS = {
    opa: {
        principal: (body) => body.input.principal,
        allow: () => ({result: true}),
        deny: () => ({result: false}),
        verify: (request, service) => {
            assert.equal(request.path, "/opa/v1/data/cas/authz/allow");
            assert.equal(request.headers.authorization, "Bearer opa-token");
            const input = request.body.input;
            assert.equal(input.principal, "pdpallow");
            assert.equal(input.service, service);
            assert(input.attributes.memberOf.includes("staff"));
            assert(input.attributes.mail.includes("pdp@example.org"));
            assert.deepEqual(input.context, {tier: "gold"});
        }
    },
    cerbos: {
        principal: (body) => body.principal.id,
        allow: (body) => ({
            requestId: body.requestId,
            results: [{resource: {id: body.resources[0].resource.id, kind: "application"}, actions: {read: "EFFECT_ALLOW"}}]
        }),
        deny: (body) => ({
            requestId: body.requestId,
            results: [{resource: {id: body.resources[0].resource.id, kind: "application"}, actions: {read: "EFFECT_DENY"}}]
        }),
        verify: (request, service) => {
            assert.equal(request.path, "/cerbos/api/check/resources");
            assert.equal(request.headers.authorization, "Bearer cerbos-token");
            const body = request.body;
            assert.equal(body.requestId, "cas-scenario-request");
            assert.equal(body.includeMeta, true);
            assert.deepEqual(body.auxData, {source: "cas"});
            assert.equal(body.principal.id, "pdpallow");
            assert.equal(body.principal.scope, "cas");
            assert(body.principal.roles.includes("staff"));
            assert(body.principal.attr.mail.includes("pdp@example.org"));
            assert.deepEqual(body.principal.attr.serviceUrl, [service]);
            assert.deepEqual(body.principal.attr.serviceName, ["Cerbos"]);
            assert.deepEqual(body.principal.attr.serviceId, [12]);
            assert.equal(body.resources.length, 1);
            assert.deepEqual(body.resources[0].actions, ["read"]);
            assert.equal(body.resources[0].resource.id, "12");
            assert.equal(body.resources[0].resource.kind, "application");
            assert.equal(body.resources[0].resource.scope, "cas");
            assert.deepEqual(body.resources[0].resource.attr.serviceUrl, [service]);
        }
    },
    permify: {
        principal: (body) => body.subject.id,
        allow: () => ({can: "CHECK_RESULT_ALLOWED"}),
        deny: () => ({can: "CHECK_RESULT_DENIED"}),
        verify: (request, service) => {
            assert.equal(request.path, "/permify/v1/tenants/t1/permissions/check");
            assert.equal(request.headers.authorization, "Bearer permify-token");
            const body = request.body;
            assert.deepEqual(body.entity, {type: "application", id: "13"});
            assert.equal(body.permission, "view");
            assert.deepEqual(body.subject, {type: "user", id: "pdpallow", relation: "owner"});
            assert.equal(body.context.data.service, service);
            assert(body.context.data.attributes.memberOf.includes("staff"));
            assert.deepEqual(body.metadata, {depth: 20});
        }
    },
    openfga: {
        principal: (body) => body.tuple_key.user.replace(/^user:/, ""),
        allow: () => ({allowed: true}),
        deny: () => ({allowed: false}),
        verify: (request, service) => {
            assert.equal(request.path, "/openfga/stores/s1/check");
            assert.equal(request.headers.authorization, "Bearer openfga-token");
            assert.deepEqual(request.body, {tuple_key: {user: "user:pdpallow", relation: "viewer", object: service}});
        }
    }
};

function startPolicyDecisionPoints(requests) {
    const app = express();
    app.use(express.json());
    const routes = {
        opa: "/opa/v1/data/cas/authz/allow",
        cerbos: "/cerbos/api/check/resources",
        permify: "/permify/v1/tenants/t1/permissions/check",
        openfga: "/openfga/stores/s1/check"
    };
    for (const [name, route] of Object.entries(routes)) {
        app.post(route, (req, res) => {
            const pdp = PDPS[name];
            const principal = pdp.principal(req.body);
            requests.push({pdp: name, principal: principal, path: req.path, headers: req.headers, body: req.body});
            const outcome = PDP_USERS[principal];
            cas.log(`${name} received a decision request for ${principal}, answering ${outcome}`);
            if (outcome === "allow") {
                res.status(200).json(pdp.allow(req.body));
            } else if (outcome === "deny") {
                res.status(200).json(pdp.deny(req.body));
            } else if (outcome === "error") {
                res.status(500).json(pdp.allow(req.body));
            } else if (outcome === "garbage") {
                res.status(200).type("text/plain").send("<html>not a decision</html>");
            } else {
                req.socket.destroy();
            }
        });
    }
    return app.listen(5611);
}

async function assertAccessDenied(page, service, user) {
    await cas.gotoLogin(page, service);
    const response = await cas.loginWith(page, user);
    await cas.sleep(1000);
    assert.equal(response.status(), 401);
    await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
    await cas.assertTicketParameter(page, false);
    await cas.assertCookie(page, false);
}

async function verifyPolicyDecisionPoints(page) {
    const requests = [];
    const pdpServer = startPolicyDecisionPoints(requests);
    for (const name of Object.keys(PDPS)) {
        const service = `https://localhost:9859/anything/pdp-${name}`;
        for (const user of ["pdpdeny", "pdperror", "pdpgarbage", "pdpdrop"]) {
            await cas.log(`${name}: decision ${PDP_USERS[user]} for ${user} must deny access`);
            await assertAccessDenied(page, service, user);
            assert(requests.some((r) => r.pdp === name && r.principal === user), `${name} was never asked about ${user}`);
        }

        await cas.log(`${name}: an allow decision issues a service ticket`);
        await cas.gotoLogin(page, service);
        await cas.loginWith(page, "pdpallow");
        await cas.sleep(1000);
        const ticket = await cas.assertTicketParameter(page);
        const json = await cas.validateTicket(service, ticket);
        assert.equal(json.serviceResponse.authenticationSuccess.user, "pdpallow");
        const received = requests.filter((r) => r.pdp === name && r.principal === "pdpallow");
        assert(received.length > 0);
        received.forEach((request) => PDPS[name].verify(request, service));
        await cas.gotoLogout(page);
        await cas.assertCookie(page, false);
    }

    await cas.log("Every PDP is unreachable: access is denied even for the allowed user");
    await new Promise((resolve) => {
        pdpServer.close(resolve);
        pdpServer.closeAllConnections();
    });
    const before = requests.length;
    for (const name of Object.keys(PDPS)) {
        await assertAccessDenied(page, `https://localhost:9859/anything/pdp-${name}`, "pdpallow");
    }
    assert.equal(requests.length, before);
}

(async () => {
    const app = express();
    let slowRequests = 0;
    app.get("/gateway", (req, res) => {
        const username = req.query.username;

        if (username === "casslow") {
            slowRequests++;
            setTimeout(() => res.status(200).send("Accepted too late"), 8000);
        } else if (username === "casblock") {
            res.status(403).send("Denied");
        } else {
            res.status(200).send("Accepted");
        }
    });

    const server = app.listen(5566, async () => {
        const browser = await cas.newBrowser(cas.browserOptions());
        const page = await cas.newPage(browser);
        await cas.gotoLogin(page, "https://localhost:9859/anything/deny");
        const response = await cas.loginWith(page, "casblock");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
        assert(response.status() === 401);

        await cas.log("Endpoint answering after the fixed timeout denies access, without retries");
        await cas.gotoLogin(page, "https://localhost:9859/anything/slow");
        const slowResponse = await cas.loginWith(page, "casslow");
        await cas.sleep(1000);
        await cas.assertInnerText(page, "#loginErrorsPanel p", "Service access denied due to missing privileges.");
        assert(slowResponse.status() === 401);
        assert(slowRequests === 1);

        await cas.gotoLogin(page, "https://localhost:9859/anything/OK");
        await cas.loginWith(page, "casuser");
        await cas.sleep(1000);
        await cas.assertTicketParameter(page);
        await cas.gotoLogout(page);

        await verifyPolicyDecisionPoints(page);

        server.close(() => {
            cas.log("Exiting server...");
            cas.closeBrowser(browser);
        });
    });
})();
