const assert = require("assert");
const cas = require("../../cas.js");

(async () => {
    const params = new URLSearchParams();
    params.append("username", "user1+casuser");
    params.append("password", "Mellon");
    await cas.doPost("https://localhost:8443/cas/v1/users",
        params, {
            "Accept": "application/json",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            cas.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser !== undefined);
            assert(res.data.authentication.attributes.surrogateEnabled !== undefined);
            assert(res.data.authentication.attributes.surrogatePrincipal !== undefined);
        },
        (error) => {
            throw error;
        });

    await cas.doPost("https://localhost:8443/cas/v1/users",
        "username=casuser&password=Mellon", {
            "Accept": "application/json",
            "X-Surrogate-Principal": "user1",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => {
            cas.log(res.data.authentication.attributes);
            assert(res.data.authentication.attributes.surrogateUser !== undefined);
            assert(res.data.authentication.attributes.surrogateEnabled !== undefined);
            assert(res.data.authentication.attributes.surrogatePrincipal !== undefined);
        },
        (error) => {
            throw error;
        });

    await cas.log("Getting ticket with surrogate principal");
    const tgt = await cas.doPost("https://localhost:8443/cas/v1/tickets",
        "username=casuser&password=Mellon", {
            "Accept": "application/json",
            "X-Surrogate-Principal": "user1",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => res.data,
        (error) => {
            throw error;
        });
    await cas.log(`Received ticket-granting ticket ${tgt}`);

    const service = "https://example.org";
    const st = await cas.doPost(`https://localhost:8443/cas/v1/tickets/${tgt}`,
        `service=${service}`, {
            "Accept": "application/json",
            "X-Surrogate-Principal": "user1",
            "Content-Type": "application/x-www-form-urlencoded"
        },
        (res) => res.data,
        (error) => {
            throw error;
        });
    await cas.log(`Received service ticket ${st}`);

    const json = await cas.validateTicket(service, st);
    const authenticationSuccess = json.serviceResponse.authenticationSuccess;
    assert(authenticationSuccess.attributes.employeeNumber !== undefined);
    assert(authenticationSuccess.attributes["fname"] === undefined);
    assert(authenticationSuccess.attributes["lname"] === undefined);

})();
