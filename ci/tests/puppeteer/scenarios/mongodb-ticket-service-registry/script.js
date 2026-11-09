const assert = require("assert");
const crypto = require("crypto");
const cas = require("../../cas.js");

const sha512 = (value) => crypto.createHash("sha512").update(value).digest("hex");

async function storedDocuments(collection) {
    const output = await cas.dockerExec("mongodb-server", ["mongosh", "--quiet",
        "mongodb://root:secret@localhost:27017/ticket-registry?authSource=admin", "--eval",
        `EJSON.stringify(db.${collection}.find().toArray())`]);
    return output.split("\n").pop();
}

/**
 * With the registry cipher on, a document carries the digest of the ticket id and of the principal, digested
 * attribute names and values, and the ticket itself encrypted; nothing that identifies the user or the ticket is
 * left in the clear. CAS still reads the tickets back for single sign-on and validation.
 */
async function verifyTicketsEncryptedAtRest(baseUrl) {
    const service = "https://localhost:9859/anything/mongo-crypto";
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const context = await browser.createBrowserContext();
        const page = await cas.newPage(context);
        await cas.gotoLogin(page, service);
        await cas.loginWith(page);
        await cas.sleep(1000);
        const serviceTicket = await cas.assertTicketParameter(page);

        const sessions = JSON.parse(await cas.doRequest(`${baseUrl}/ssoSessions?type=ALL`, "GET",
            {"Accept": "application/json"}, 200)).activeSsoSessions;
        const ticketGrantingTicket = sessions
            .find((session) => Object.keys(session.authenticated_services).includes(serviceTicket))?.ticket_granting_ticket;
        assert(ticketGrantingTicket !== undefined, `No SSO session tracks ${serviceTicket}`);

        const clearValues = ["casuser", "EN7731CAS", "employeeNumber", "AcceptUsersAuthenticationHandler"];
        const storedSessions = await storedDocuments("ticketGrantingTicketsCollection");
        const sessionDocument = JSON.parse(storedSessions).find((document) => document._id.toLowerCase() === sha512(ticketGrantingTicket));
        assert(sessionDocument !== undefined, "The ticket-granting ticket must be stored under the digest of its id");
        assert.equal(sessionDocument.principal.toLowerCase(), sha512("casuser"));
        assert(sessionDocument.type.endsWith("EncodedTicket"), `Unexpected stored type ${sessionDocument.type}`);
        for (const value of [ticketGrantingTicket, ...clearValues]) {
            assert(!storedSessions.includes(value), `Ticket-granting ticket documents must not hold ${value} in the clear`);
        }

        const storedServiceTickets = await storedDocuments("serviceTicketsCollection");
        const serviceTicketDocument = JSON.parse(storedServiceTickets).find((document) => document._id.toLowerCase() === sha512(serviceTicket));
        assert(serviceTicketDocument !== undefined, "The service ticket must be stored under the digest of its id");
        assert(serviceTicketDocument.type.endsWith("EncodedTicket"), `Unexpected stored type ${serviceTicketDocument.type}`);
        for (const value of [serviceTicket, ticketGrantingTicket, ...clearValues]) {
            assert(!storedServiceTickets.includes(value), `Service ticket documents must not hold ${value} in the clear`);
        }

        const headers = {"Accept": "application/json", "Content-Type": "application/x-www-form-urlencoded"};
        const encoded = await cas.doRequest(`${baseUrl}/ticketRegistry/query?type=TGT&count=100`, "GET", headers, 200);
        assert(encoded.includes(`${sha512(ticketGrantingTicket)}:${sha512("casuser")}`));
        assert(!encoded.includes("casuser"));
        const decoded = await cas.doRequest(`${baseUrl}/ticketRegistry/query?type=TGT&count=100&decode=true`, "GET", headers, 200);
        assert(decoded.includes(ticketGrantingTicket) && decoded.includes("casuser"), "Decoded tickets must be readable again");

        const validation = await cas.validateTicket(service, serviceTicket);
        assert.equal(validation.serviceResponse.authenticationSuccess.user, "casuser");

        await cas.gotoLogin(page, service);
        await cas.sleep(1000);
        const nextTicket = await cas.assertTicketParameter(page);
        const nextValidation = await cas.validateTicket(service, nextTicket);
        assert.equal(nextValidation.serviceResponse.authenticationSuccess.user, "casuser");

        await cas.gotoLogout(page);
        await context.close();
    } finally {
        await cas.closeBrowser(browser);
    }
}

(async () => {
    const leak = await cas.randomNumber(4, 20);
    await cas.log("Updating configuration and waiting for changes to reload...");
    await cas.updateYamlConfigurationSource(__dirname, {
        cas: {
            ticket: {
                registry: {
                    mongo: {
                        "timeout": `PT${leak}S`
                    }
                }
            }
        }
    });
    await cas.sleep(2000);
    await cas.refreshContext();
    await cas.sleep(5000);
    
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.logg("Removing all SSO Sessions");
    await cas.doDelete(`${baseUrl}/ssoSessions?type=ALL&from=0&count=100000`);

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);
    await cas.loginWith(page);
    await cas.goto(page, "https://localhost:8443/cas/actuator/health");
    await cas.sleep(1000);
    await cas.closeBrowser(browser);

    await cas.doGet("https://localhost:8443/cas/actuator/health",
        async (res) => {
            assert(res.data.components.mongo !== undefined);
            assert(res.data.components.memory !== undefined);
            assert(res.data.components.ping !== undefined);

            assert(res.data.components.mongo.status !== undefined);
            assert(res.data.components.mongo.details !== undefined);

            let details = res.data.components.mongo.details["MongoDbHealthIndicator-ticket-registry"];
            assert(details.name === "MongoDbHealthIndicator-ticket-registry");
            assert(details.proxyGrantingTicketsCollection !== undefined);
            assert(details.ticketGrantingTicketsCollection !== undefined);
            assert(details.proxyTicketsCollection !== undefined);
            assert(details.serviceTicketsCollection !== undefined);
            assert(details.transientSessionTicketsCollection !== undefined);

            details = res.data.components.mongo.details["MongoDbHealthIndicator-service-registry"];
            assert(details.name === "MongoDbHealthIndicator-service-registry");

        }, async (error) => {
            throw error;
        }, {"Content-Type": "application/json"});

    await cas.logg("Querying registry for ticket-granting tickets");
    await cas.doGet(`${baseUrl}/ticketRegistry/query?type=TGT&count=10`, async (res) => {
        assert(res.status === 200);
        assert(res.data.length === 1);
    }, async (err) => {
        throw err;
    }, {
        "Accept": "application/json",
        "Content-Type": "application/x-www-form-urlencoded"
    });

    await cas.doDelete(`${baseUrl}/ticketRegistry/clean`, 200,
        async (res) => {
            await cas.log(res.data);
            assert(res.status === 200);
            assert(res.data.removed === 0);
            assert(res.data.startTime !== undefined);
            assert(res.data.endTime !== undefined);
            assert(res.data.total !== undefined);
        }, async (err) => {
            throw err;
        }, {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        });

    await verifyTicketsEncryptedAtRest(baseUrl);
})();
