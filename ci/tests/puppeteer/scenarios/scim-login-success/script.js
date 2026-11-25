
const cas = require("../../cas.js");
const assert = require("assert");

(async () => {
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await cas.gotoLogin(page);

    await cas.loginWith(page, "casscimuser", "Mellon");

    await cas.assertCookie(page);
    await cas.assertPageTitle(page, "CAS - Central Authentication Service Log In Successful");
    await cas.assertInnerText(page, "#content div h2", "Log In Successful");

    const headers = { "Authorization": "Basic c2NpbS11c2VyOmNoYW5nZWl0" };
    let userId = undefined;
    await cas.doGet("http://localhost:9666/scim/v2/Users?attributes=userName",
        (res) => {
            assert(res.status === 200);
            const length = res.data.Resources.length;
            cas.log(`Found ${length} record`);
            assert(length === 1);
            assert(res.data.Resources[0].userName === "casscimuser");
            userId = res.data.Resources[0].id;
        },
        (error) => {
            throw error;
        }, headers);

    await cas.log(`Checking group membership of user ${userId}...`);
    await cas.doGet("http://localhost:9666/scim/v2/Groups",
        (res) => {
            assert(res.status === 200);
            const group = res.data.Resources.find((resource) => resource.displayName === "Engineering");
            assert(group !== undefined);
            cas.log(`Group members: ${JSON.stringify(group.members)}`);
            assert(group.members.length === 1);
            assert(group.members[0].value === userId);
        },
        (error) => {
            throw error;
        }, headers);

    await cas.closeBrowser(browser);
})();
