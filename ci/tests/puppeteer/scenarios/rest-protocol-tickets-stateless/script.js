const assert = require("assert");
const cas = require("../../cas.js");
const querystring = require("querystring");

const service = "https://localhost:9859/anything/rest";

async function executeRequest(url, method, statusCode, requestBody = undefined) {
    return cas.doRequest(url, method,
        {
            "Accept": "application/json",
            "Content-Length": requestBody === undefined ? 0 : Buffer.byteLength(requestBody),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        statusCode, requestBody);
}

async function validate(ticket) {
    const json = await cas.validateTicket(service, ticket);
    const success = json.serviceResponse.authenticationSuccess;
    assert(success !== undefined, JSON.stringify(json));
    assert(success.user === "casuser");
    assert(success.attributes.department[0] === "engineering");
}

function tamper(ticket) {
    const index = Math.floor(ticket.length * 2 / 3);
    return `${ticket.substring(0, index)}${ticket[index] === "A" ? "B" : "A"}${ticket.substring(index + 1)}`;
}

(async () => {
    const tgt = await executeRequest("https://localhost:8443/cas/v1/tickets", "POST", 201,
        querystring.stringify({username: "casuser", password: "Mellon"}));
    await cas.log(`Ticket-granting ticket is ${tgt.length} characters`);
    assert(tgt.startsWith("TGT-"), tgt);

    assert(await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "GET", 200) === tgt);

    const st = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "POST", 200, `service=${service}`);
    await cas.log(`Service ticket is ${st.length} characters`);
    assert(st.startsWith("ST-"), st);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${st}`, "GET", 200);
    await validate(st);

    await cas.log("Service tickets are not single-use with the stateless ticket registry");
    await validate(st);

    await cas.log("The ticket-granting ticket issues more service tickets");
    const another = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "POST", 200, `service=${service}`);
    assert(another !== st);
    await validate(another);

    await cas.log("Tampered tickets are rejected");
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tamper(tgt)}`, "GET", 404);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tamper(tgt)}`, "POST", 404, `service=${service}`);
    const json = await cas.validateTicket(service, tamper(st));
    assert(json.serviceResponse.authenticationFailure !== undefined, JSON.stringify(json));

    await cas.log("Issued tickets cannot be revoked with the stateless ticket registry");
    await cas.doDelete(`https://localhost:8443/cas/v1/tickets/${tgt}`);
    await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "GET", 200);
    const afterDelete = await executeRequest(`https://localhost:8443/cas/v1/tickets/${tgt}`, "POST", 200, `service=${service}`);
    await validate(afterDelete);

    await cas.log("Bad credentials do not produce a ticket-granting ticket");
    await executeRequest("https://localhost:8443/cas/v1/tickets", "POST", 401,
        querystring.stringify({username: "casuser", password: "bad"}));
})();
