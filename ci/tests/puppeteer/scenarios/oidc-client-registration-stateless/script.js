const assert = require("assert");
const cas = require("../../cas.js");

async function fetchClientConfiguration(url, token) {
    let status = 0;
    let data = undefined;
    await cas.doGet(url,
        (res) => {
            status = res.status;
            data = res.data;
        },
        (error) => {
            status = error.response?.status ?? error.status;
        },
        {
            "Authorization": `Bearer ${token}`
        });
    return {status, data};
}

(async () => {
    let failed = false;
    try {
        await cas.doDelete("https://localhost:8443/cas/actuator/registeredServices", 200);
        const body = JSON.stringify({
            "application_type": "web",
            "grant_types": ["authorization_code", "client_credentials"],
            "redirect_uris": ["https://localhost:9859/anything/cas"],
            "client_name": "Stateless Client",
            "token_endpoint_auth_method": "client_secret_basic",
            "contacts": ["sample@example.org"]
        });
        const result = await cas.doRequest("https://localhost:8443/cas/oidc/register", "POST",
            {
                "Content-Length": body.length,
                "Content-Type": "application/json"
            }, 201, body);
        const entity = JSON.parse(result.toString());
        await cas.log(entity);
        assert(entity.client_id !== undefined);
        assert(entity.client_secret !== undefined);
        assert(entity.registration_access_token !== undefined);
        await cas.log(`Registration access token is ${entity.registration_access_token.length} characters`);

        await cas.log("Fetching client configuration with the registration access token");
        const configuration = await fetchClientConfiguration(entity.registration_client_uri, entity.registration_access_token);
        assert.equal(configuration.status, 200);
        assert.equal(configuration.data.client_id, entity.client_id);
        assert.equal(configuration.data.client_name, "Stateless Client");

        await cas.log("A tampered registration access token is rejected");
        const token = entity.registration_access_token;
        const index = Math.floor(token.length * 2 / 3);
        const tampered = `${token.substring(0, index)}${token[index] === "A" ? "B" : "A"}${token.substring(index + 1)}`;
        const rejected = await fetchClientConfiguration(entity.registration_client_uri, tampered);
        assert.notEqual(rejected.status, 200);

        await cas.log("The registered client obtains an access token");
        await cas.doPost("https://localhost:8443/cas/oidc/token?grant_type=client_credentials&scope=openid", "", {
            "Content-Type": "application/json",
            "Authorization": `Basic ${btoa(`${entity.client_id}:${entity.client_secret}`)}`
        }, (res) => {
            assert(res.data.access_token !== undefined);
        }, (error) => {
            throw error;
        });
    } catch (e) {
        failed = true;
        throw e;
    } finally {
        await cas.doDelete("https://localhost:8443/cas/actuator/registeredServices", 200);
        if (!failed) {
            await process.exit(0);
        }
    }
})();
