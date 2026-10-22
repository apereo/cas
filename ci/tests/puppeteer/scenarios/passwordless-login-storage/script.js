
const assert = require("assert");
const cas = require("../../cas.js");

const MONGO_URI = "mongodb://root:secret@localhost:27017/passwordless?authSource=admin";

/**
 * Counts the passwordless tokens the current variation's repository keeps, straight from the backend.
 */
async function storedTokens() {
    if (process.env.SCENARIO_VARIATION === "mongo") {
        return Number((await cas.dockerExec("mongodb-server", ["mongosh", "--quiet", MONGO_URI, "--eval",
            "db.PasswordlessTokens.countDocuments({})"])).split("\n").pop());
    }
    const mysql = (sql) => cas.dockerExec("mysql-server", ["mysql", "-uroot", "-ppassword", "-N", "-D", "cas", "-e", sql]);
    const table = (await mysql("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA='cas' AND TABLE_NAME LIKE '%passwordless%'")).split("\n")[0];
    assert(table.length > 0, "The passwordless token table must exist");
    return Number(await mysql(`SELECT COUNT(*) FROM ${table}`));
}

(async () => {
    await cas.log(`Running with the ${process.env.SCENARIO_VARIATION} passwordless repositories`);
    if (process.env.SCENARIO_VARIATION === "mongo") {
        await cas.logg("The passwordless account is kept in MongoDb");
        await cas.dockerExec("mongodb-server", ["mongosh", "--quiet", MONGO_URI, "--eval",
            "db.PasswordlessAccounts.replaceOne({username: 'casuser'}, {username: 'casuser', email: 'cas@example.org', phone: '1234567890', name: 'CAS'}, {upsert: true}).acknowledged"]);
    }
    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    
    await cas.gotoLogin(page);

    await cas.assertElementDoesNotExist(page, "#password");

    await cas.attributeValue(page, "#username", "autocapitalize", "none");
    await cas.attributeValue(page, "#username", "spellcheck", "false");
    await cas.attributeValue(page, "#username", "autocomplete", "username");

    await cas.type(page,"#username", "casuser");
    await cas.pressEnter(page);
    await cas.waitForNavigation(page);
    await cas.assertInnerText(page, "#login h3", "Provide Token");
    await cas.assertInnerTextStartsWith(page, "#login p", "Please provide the security token sent to you");
    await cas.assertVisibility(page, "#token");

    const code = await cas.extractFromEmail(browser);
    assert(await storedTokens() === 1, "The token must be kept in the backend");

    await page.bringToFront();
    await cas.type(page, "#token", code);
    await cas.submitForm(page, "#fm1");
    await cas.sleep(3000);
    await cas.assertCookie(page);
    assert(await storedTokens() === 0, "A used token must be removed from the backend");
    await cas.closeBrowser(browser);
})();
