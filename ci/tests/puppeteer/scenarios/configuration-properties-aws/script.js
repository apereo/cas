const cas = require("../../cas.js");
const assert = require("assert");

const ACTUATOR = "https://localhost:8443/cas/actuator";
const SERVICE = "https://localhost:9859/anything/cas";
const SSM = "bootstrapProperties-AmazonSimpleSystemsManagementPropertySource";
const SECRETS_MANAGER = "bootstrapProperties-AmazonSecretsManagerPropertySource";
const S3 = "bootstrapProperties-org.apereo.cas.config.AmazonS3BucketPropertySource";

/*
 * Reads a setting through the env actuator endpoint and checks the property source that won along with its value.
 */
async function assertSetting(name, source, value) {
    const response = JSON.parse(await cas.doRequest(`${ACTUATOR}/env/${name}`, "GET", {"Accept": "application/json"}));
    const property = response.property;
    await cas.log(`${name} is ${property.value} from ${property.source}`);
    assert(property.source === source, `${name} must come from ${source}, not ${property.source}`);
    assert(property.value === value, `${name} must be ${value}, not ${property.value}`);
}

async function awslocal(args, input = undefined) {
    return cas.dockerExec("localstack", ["awslocal", ...args], input);
}

async function assertLogin(page, password, success) {
    await cas.gotoLogout(page);
    await cas.gotoLogin(page);
    await cas.loginWith(page, "awsuser", password);
    await cas.sleep(1000);
    await cas.assertCookie(page, success);
}

async function assertLogoutRedirect(page, followed) {
    await cas.gotoLogout(page, SERVICE);
    await cas.sleep(1000);
    await cas.logPage(page);
    await cas.assertPageUrlStartsWith(page, followed ? SERVICE : "https://localhost:8443/cas/logout");
}

/*
 * Puts the settings back the way init.sh stored them, so that a retry of the scenario against the same CAS instance
 * and localstack container starts from the same state.
 */
async function resetSettings() {
    await awslocal(["ssm", "put-parameter", "--name", "/cas/cas.authn.accept.users",
        "--value", "awsuser::Mellon", "--type", "String", "--overwrite"]);
    await awslocal(["secretsmanager", "put-secret-value", "--secret-id", "cas.server.scope", "--secret-string", "example.com"]);
    await awslocal(["s3", "cp", "-", "s3://cas-config/cas.properties"],
        "cas.logout.follow-service-redirects=false\ncas.audit.engine.enabled=false\n");
    try {
        await awslocal(["secretsmanager", "delete-secret", "--secret-id", "cas.authn.accept.users", "--force-delete-without-recovery"]);
    } catch {
        await cas.log("No secret for cas.authn.accept.users to remove");
    }
    await cas.refreshContext();
    await cas.sleep(2000);
}

(async () => {
    await resetSettings();
    await assertSetting("cas.authn.accept.users", SSM, "awsuser::Mellon");
    await assertSetting("cas.server.scope", SECRETS_MANAGER, "example.com");
    await assertSetting("cas.logout.follow-service-redirects", S3, "false");
    await assertSetting("cas.audit.engine.enabled", S3, "false");

    const browser = await cas.newBrowser(cas.browserOptions());
    const page = await cas.newPage(browser);
    await assertLogin(page, "Mellon", true);
    await assertLogoutRedirect(page, false);

    await cas.logg("Changing a setting in each of SSM, Secrets Manager and S3");
    await awslocal(["ssm", "put-parameter", "--name", "/cas/cas.authn.accept.users",
        "--value", "awsuser::Changed", "--type", "String", "--overwrite"]);
    await awslocal(["secretsmanager", "put-secret-value", "--secret-id", "cas.server.scope", "--secret-string", "example.net"]);
    await awslocal(["s3", "cp", "-", "s3://cas-config/cas.properties"],
        "cas.logout.follow-service-redirects=true\ncas.audit.engine.enabled=false\n");
    await cas.refreshContext();
    await cas.sleep(2000);

    await assertSetting("cas.authn.accept.users", SSM, "awsuser::Changed");
    await assertSetting("cas.server.scope", SECRETS_MANAGER, "example.net");
    await assertSetting("cas.logout.follow-service-redirects", S3, "true");
    await assertSetting("cas.audit.engine.enabled", S3, "false");

    await assertLogin(page, "Mellon", false);
    await assertLogin(page, "Changed", true);
    await assertLogoutRedirect(page, true);

    await cas.logg("After a refresh, a setting changed through CAS is written to SSM, Secrets Manager and S3");
    const payload = JSON.stringify([{name: "cas.authn.accept.users", value: "awsuser::Written"}]);
    const updated = JSON.parse(await cas.doRequest(`${ACTUATOR}/casConfig/update`, "POST",
        {"Content-Type": "application/json"}, 200, payload));
    assert(updated.length >= 3, `Every AWS property source must take the change, not only ${updated}`);
    const parameter = JSON.parse(await awslocal(["ssm", "get-parameter", "--name", "/cas/cas.authn.accept.users", "--with-decryption"]));
    assert(parameter.Parameter.Value === "awsuser::Written");
    const secret = JSON.parse(await awslocal(["secretsmanager", "get-secret-value", "--secret-id", "cas.authn.accept.users"]));
    assert(secret.SecretString === "awsuser::Written");
    const object = await awslocal(["s3", "cp", "s3://cas-config/cas.properties", "-"]);
    assert(object.includes("cas.authn.accept.users=awsuser\\:\\:Written") || object.includes("cas.authn.accept.users=awsuser::Written"));
    assert(object.includes("cas.logout.follow-service-redirects=true"));
    await cas.refreshContext();
    await cas.sleep(2000);
    await assertLogin(page, "Changed", false);
    await assertLogin(page, "Written", true);

    await cas.closeBrowser(browser);
})();
