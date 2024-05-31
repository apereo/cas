const cas = require("../../cas.js");
const querystring = require("querystring");

(async () => {
    await loginWith("BadPassw0rd", 401);
    for (let i = 0; i < 3; i++) {
        await loginWith("BadPassw0rd", 423);
    }
    await cas.sleep(3000);
    await cas.log("Fetching throttled submissions");
    await cas.doRequest("https://localhost:8443/cas/actuator/throttles");
    await cas.log("Cleaning throttled submissions");
    await cas.doDelete("https://localhost:8443/cas/actuator/throttles");
    await cas.doDelete("https://localhost:8443/cas/actuator/throttles?clear=true");
    await loginWith("BadPassw0rd", 401);
})();

async function loginWith(password, expectedStatus) {
    const formData = {
        username: "casuser",
        password: password
    };
    const postData = querystring.stringify(formData);
    const body = await cas.doRequest("https://localhost:8443/cas/v1/users",
        "POST",
        {
            "Accept": "application/json",
            "Content-Length": Buffer.byteLength(postData),
            "Content-Type": "application/x-www-form-urlencoded"
        },
        expectedStatus,
        postData);
    await cas.log(body);
    return JSON.parse(body);
}

