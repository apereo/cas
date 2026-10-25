const cas = require("../../cas.js");
const assert = require("assert");
const fs = require("fs");
const path = require("path");

const SERVICE = "https://localhost:9859/anything/redis";

/*
 * accounts.json holds RedisUserAccount records the way the Redis authentication handler reads them: Java-serialized,
 * then LZ4-compressed behind a four-byte length header, as CAS Redis templates write values. Both accounts use the
 * password p@SSw0rd, stored as its SHA-512 hex digest, with the attributes name=Redis and group=sso,staff;
 * redisuser is active and redisdisabled is disabled.
 */
async function seedAccounts() {
    const accounts = JSON.parse(fs.readFileSync(path.join(__dirname, "accounts.json"), "utf8"));
    for (const [username, value] of Object.entries(accounts)) {
        await cas.dockerExec("redis_server_master", ["redis-cli", "-x", "SET", username], Buffer.from(value, "base64"));
    }
}

(async () => {
    await seedAccounts();
    const browser = await cas.newBrowser(cas.browserOptions());
    try {
        const page = await cas.newPage(browser);

        await cas.logg("A wrong password and an unknown user are rejected");
        await cas.gotoLogin(page);
        await cas.loginWith(page, "redisuser", "WrongPassword");
        await cas.assertCookie(page, false);
        await cas.gotoLogin(page);
        await cas.loginWith(page, "unknownuser", "p@SSw0rd");
        await cas.assertCookie(page, false);

        await cas.logg("A disabled account is told so");
        await cas.gotoLogin(page);
        await cas.loginWith(page, "redisdisabled", "p@SSw0rd");
        await cas.assertCookie(page, false);
        await cas.assertInnerText(page, "#content h2", "This account has been disabled.");

        await cas.logg("The account logs in and its attributes are released");
        await cas.gotoLogin(page, SERVICE);
        await cas.loginWith(page, "redisuser", "p@SSw0rd");
        const ticket = await cas.assertTicketParameter(page);
        const payload = await cas.validateTicket(SERVICE, ticket);
        const success = payload.serviceResponse.authenticationSuccess;
        assert(success.user === "redisuser");
        assert(success.attributes.name.includes("Redis"));
        assert(success.attributes.group.includes("sso") && success.attributes.group.includes("staff"));
    } finally {
        await cas.closeBrowser(browser);
    }
})();
