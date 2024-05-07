const assert = require("assert");
const axios = require("axios");
const https = require("https");
const {spawn} = require("child_process");
const waitOn = require("wait-on");
const JwtOps = require("jsonwebtoken");
const colors = require("colors");
const fs = require("fs");
const util = require("util");
const path = require("path");
const mockServer = require("mock-json-server");
const {Buffer} = require("buffer");
const ps = require("ps-node");
const NodeStaticAuth = require("node-static-auth");
const operativeSystemModule = require("os");
const figlet = require("figlet");
const CryptoJS = require("crypto-js");
const jose = require("jose");
const pino = require("pino");
const xml2js = require("xml2js");
const {Docker} = require("node-docker-api");
const docker = new Docker({socketPath: "/var/run/docker.sock"});
const archiver = require("archiver");
const unzipper = require("unzipper");
const puppeteer = require("puppeteer");

const LOGGER = pino({
    level: "debug",
    options: {
        colorize: true
    },
    transport: {
        target: "pino-pretty"
    }
});

/**
 * IMPORTANT: Controls settings for the chromium browser.
 * This appears to be necessary in newer versions of chromium
 * so we can alter and store the security and privacy checks
 * and disable everything, particular the password manager that
 * forces a popup dialog, warning about password data breaches.
 *
 * The value should be:
 * /path/to/cas-server/ci/tests/puppeteer/chromium
 */
const CHROMIUM_USER_DATA_DIR = `${__dirname}/chromium`;

const BROWSER_OPTIONS = {
    ignoreHTTPSErrors: true,
    headless: (process.env.CI === "true" || process.env.HEADLESS === "true") ? "new" : false,
    devtools: process.env.CI !== "true",
    defaultViewport: null,
    timeout: 60000,
    protocolTimeout: 30000,
    dumpio: false,
    slowMo: process.env.CI === "true" ? 0 : 10,
    args: [
        `--user-data-dir=${CHROMIUM_USER_DATA_DIR}/user-data-dir`,
        "--no-sandbox",
        "--disable-web-security",
        "--start-maximized",
        "--window-size=1920,1080"
    ]
};

exports.browserOptions = () => BROWSER_OPTIONS;
exports.browserOptions = (opt) => ({
    ...BROWSER_OPTIONS,
    ...opt
});

function inspect(text) {
    let result;
    try {
        result = JSON.parse(text);
    } catch {
        result = text;
    }
    return util.inspect(result, {colors: false, depth: null});
}

exports.newBrowser = async (options) => {
    let browser = undefined;
    let retry = 0;
    const maxRetries = 5;
    while (retry < maxRetries) {
        try {
            await this.logg(`Attempt #${retry} to launch browser...`);
            browser = await puppeteer.launch(options);
            await this.sleep();
            await this.logg(`Browser ${await browser.version()} / ${await browser.userAgent()} is launched...`);
            return browser;
        } catch (e) {
            retry++;
            await this.logr(e);
            await this.logg("Failed to launch browser. Retrying...");
        }
    }
    throw "Failed to launch browser after multiple attempts";
};

exports.log = async (text, ...args) => {
    const toLog = inspect(text);
    await LOGGER.debug(`💬 ${colors.blue(toLog)}`, args);
};

exports.logy = async (text) => {
    const toLog = inspect(text);
    await LOGGER.warn(`🔥 ${colors.yellow(toLog)}`);
};

exports.logb = async (text) => {
    const toLog = inspect(text);
    await LOGGER.debug(`💬 ${colors.blue(toLog)}`);
};

exports.logg = async (text) => {
    const toLog = inspect(text);
    await LOGGER.info(`✅ ${colors.green(toLog)}`);
};

exports.logr = async (text) => {
    const toLog = inspect(text);
    await LOGGER.error(`📛 ${colors.red(toLog)}`);
};

exports.logPage = async (page) => {
    const url = await page.url();
    await this.log(`Page URL: ${url}`);
};

exports.removeDirectoryOrFile = async (directory) => {
    this.logg(`Removing directory ${directory}`);
    if (fs.existsSync(directory)) {
        await fs.rmSync(directory, {recursive: true});
    }
    await this.logg(`Removed directory ${directory}`);
    if (fs.existsSync(directory)) {
        await this.logr(`Removed directory still present at: ${directory}`);
    }
};

exports.click = async (page, button) => {
    await page.evaluate((button) => {
        const buttonNode = document.querySelector(button);
        console.log(`Clicking element ${button} with href ${buttonNode.href}`);
        buttonNode.click();
    }, button);
};

exports.asciiart = async (text) => {
    const art = figlet.textSync(text);
    console.log(colors.blue(art));
};

exports.clickLast = async (page, button) => {
    await page.evaluate((button) => {
        const buttons = document.querySelectorAll(button);
        buttons[buttons.length - 1].click();
    }, button);
};

exports.innerHTML = async (page, selector) => {
    const text = await page.$eval(selector, (el) => el.innerHTML.trim());
    await this.log(`HTML for selector [${selector}] is: [${text}]`);
    return text;
};

exports.innerText = async (page, selector) => {
    const text = await page.$eval(selector, (el) => el.innerText.trim());
    await this.log(`Text for selector [${selector}] is: ${text}`);
    return text;
};

exports.elementValue = async (page, selector, valueToSet = undefined) => {
    const text = await page.$eval(selector, (el) => el.value.trim());
    if (valueToSet !== undefined && valueToSet !== null) {
        await this.log(`Setting value for selector [${selector}] to: [${valueToSet}]`);
        await page.$eval(selector, (el, toSet) => {
            el.value = toSet;
        }, valueToSet);
    } else {
        await this.log(`Value for selector [${selector}] is: [${text}]`);
    }
    return text;
};

exports.innerTexts = async (page, selector) =>
    page.evaluate((button) => {
        const results = [];
        const elements = document.querySelectorAll(button);
        elements.forEach((entry) => results.push(entry.innerText.trim()));
        return results;
    }, selector);

exports.textContent = async (page, selector) => {
    const element = await page.$(selector);
    const text = await page.evaluate((element) => element.textContent.trim(), element);
    await this.log(`Text content for selector [${selector}] is: [${text}]`);
    return text;
};

exports.inputValue = async (page, selector) => {
    const element = await page.$(selector);
    const text = await page.evaluate((element) => element.value, element);
    await this.log(`Input value for selector [${selector}] is: [${text}]`);
    return text;
};

exports.waitForElement = async (page, selector, timeout = 10000) => page.waitForSelector(selector, {timeout: timeout});

exports.loginWith = async (page,
    user = "casuser",
    password = "Mellon",
    usernameField = "#username",
    passwordField = "#password") => {
    await this.log(`Logging in with ${user} and ${password}`);
    await page.waitForSelector(usernameField, {visible: true});
    await this.type(page, usernameField, user);

    await page.waitForSelector(passwordField, {visible: true});
    await this.type(page, passwordField, password, true);

    await this.pressEnter(page);
    try {
        const response = await this.waitForNavigation(page);
        await this.log(`Page response status after navigation: ${response.status()}`);
        return response;
    } catch (e) {
        await this.logr(e);
    }
    return undefined;
};

exports.fetchGoogleAuthenticatorScratchCode = async (user = "casuser") => {
    await this.log(`Fetching Scratch codes for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/gauthCredentialRepository/${user}`,
        "GET", {
            "Accept": "application/json"
        });
    return JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);
};

exports.isVisible = async (page, selector) => {
    try {
        const element = await page.$(selector);
        const result = (element !== null && await element.boundingBox() !== null);
        await this.log(`Checking element visibility for ${selector} while on page ${page.url()}: ${result}`);
        return result;
    } catch (e) {
        await this.logr(e);
        return false;
    }
};

exports.assertVisibility = async (page, selector) => {
    assert(await this.isVisible(page, selector), `The element ${selector} must be visible but it's not.`);
};

exports.assertInvisibility = async (page, selector) => {
    const element = await page.$(selector);
    const result = element === null || await element.boundingBox() === null;
    await this.log(`Checking element invisibility for ${selector} while on page ${page.url()}: ${result}`);
    assert(result, `The element ${selector} must be invisible but it's not.`);
};

exports.assertCookie = async (page, cookieMustBePresent = true, cookieName = "TGC") => {
    const cookies = (await page.cookies()).filter((c) => {
        this.log(`Checking cookie ${c.name}:${c.value}`);
        return c.name === cookieName;
    });
    await this.log(`Found cookies ${cookies.length}`);
    if (cookieMustBePresent) {
        await this.log(`Checking for cookie ${cookieName}, which MUST be present`);
        assert(cookies.length !== 0, `Cookie ${cookieName} must be present`);
        await this.logg("Asserting cookies:");
        await this.logg(`${JSON.stringify(cookies, undefined, 2)}`);
        return cookies[0];
    }
    await this.log(`Checking for cookie ${cookieName}, which MUST NOT be present`);
    if (cookies.length === 0) {
        await this.logg(`Correct! Cookie ${cookieName} cannot be found`);
    } else {
        await this.logr(`Incorrect! Cookie ${cookieName} can be found`);
        const ck = cookies[0];
        const msg = `Found cookie => name: ${ck.name},value:${ck.value},path:${ck.path},domain:${ck.domain},httpOnly:${ck.httpOnly},secure:${ck.secure}`;
        await this.logb(msg);
        throw msg;
    }
};

exports.submitForm = async (page, selector, predicate = undefined, statusCode = 0) => {
    await this.log(`Submitting form ${selector}`);
    if (predicate === undefined) {
        predicate = async (response) => {
            const responseStatus = response.status();
            await this.log(`Page response status: ${responseStatus}`);
            if (statusCode <= 0) {
                await this.log("Waiting for page to produce a valid response status code");
                return responseStatus > statusCode;
            }
            await this.log(`Waiting for page response status code ${statusCode}`);
            return responseStatus === statusCode;
        };
    }
    return Promise.all([
        page.waitForResponse(predicate),
        page.$eval(selector, (form) => form.submit()),
        this.sleep(3000)
    ]);
};

exports.pressEnter = async (page) => {
    this.screenshot(page);
    page.keyboard.press("Enter");
    this.sleep(1000);
};

exports.type = async (page, selector, value, obfuscate = false) => {
    await page.waitForSelector(selector, {visible: true, timeout: 3000});
    const logValue = obfuscate ? "******" : value;
    await this.log(`Typing ${logValue} in field ${selector}`);
    await page.$eval(selector, (el) => el.value = "");
    await page.type(selector, value);
};

exports.attributeValue = async (page, selector, attribute, expectedValue = undefined) => {
    const element = await page.$(selector);
    const value = await page.evaluate((elem, attrib) => elem.getAttribute(attrib), element, attribute);
    await this.logb(`Node [${selector}] attribute [${attribute}] has value: [${value}]`);
    if (expectedValue !== undefined) {
        assert.equal(value, expectedValue);
    }
    return value;
};

exports.newPage = async (browser) => {
    let page = undefined;
    try {
        page = (await browser.pages())[0];
    } catch (e) {
        this.logr(e);
        await this.sleep(1000);
    }

    if (page === undefined) {
        let counter = 0;
        while (page === undefined && counter < 5) {
            try {
                counter++;
                await this.log("Opening a new browser page...");
                page = await browser.newPage();
            } catch (e) {
                this.logr(e);
                await this.sleep(2000);
            }
        }
    }
    if (page === null || page === undefined) {
        const err = "Unable to open a new browser page";
        await this.logr(err);
        throw err;
    }

    page
        .on("console", (message) => {
            if (message.type() === "warning") {
                this.logy(`Console ${message.type()}: ${message.text()}`);
            } else if (message.type() === "error") {
                this.logr(`Console ${message.type()}: ${message.text()}`);
            } else {
                this.logg(`Console ${message.type()}: ${message.text()}`);
            }
        })
        .on("pageerror", ({message}) => this.logr(`Console: ${message}`));

    try {
        await page.bringToFront();
    } catch (e) {
        await this.logr(e);
    }
    return page;
};

exports.assertParameter = async (page, param) => {
    await this.log(`Asserting parameter ${param} in URL: ${page.url()}`);
    const result = new URL(page.url());
    const value = result.searchParams.get(param);
    await this.logg(`Parameter ${param} with value ${value}`);
    assert(value !== null, `Parameter ${param} cannot be null`);
    return value;
};

exports.assertPageUrl = async (page, url) => {
    const result = await page.url();
    assert.equal(result, url);
};

exports.assertPageUrlStartsWith = async (page, url) => {
    const result = await page.url();
    assert(result.startsWith(url));
};

exports.assertPageUrlProtocol = async (page, protocol) => {
    const result = new URL(await page.url());
    assert.equal(result.protocol, protocol);
};

exports.assertPageUrlHost = async (page, host) => {
    const result = new URL(await page.url());
    assert.equal(result.host, host);
};

exports.assertPageUrlPort = async (page, port) => {
    const result = new URL(await page.url());
    assert.equal(result.port, port);
};

exports.assertMissingParameter = async (page, param) => {
    const result = new URL(await page.url());
    assert.equal(result.searchParams.has(param), false);
};

exports.sleep = async (ms = 1000) =>
    new Promise((resolve) => {
        this.logg(`Waiting for ${ms / 1000} second(s)...`);
        setTimeout(resolve, ms);
    });

exports.assertTicketParameter = async (page, found = true) => {
    await this.log(`Page URL: ${page.url()}`);
    const result = new URL(page.url());
    if (found) {
        assert(result.searchParams.has("ticket"));
        const ticket = result.searchParams.get("ticket");
        await this.log(`Ticket: ${ticket}`);
        assert(ticket !== null);
        return ticket;
    }
    assert(result.searchParams.has("ticket") === false);
    return null;
};

exports.doRequest = async (url, method = "GET",
    headers = {},
    statusCode = 200,
    requestBody = undefined,
    callback = undefined) =>
    new Promise((resolve, reject) => {
        const options = {
            method: method,
            rejectUnauthorized: false,
            headers: headers
        };
        options.agent = new https.Agent(options);

        this.logg(`Sending ${method} request to ${url}`);
        const handler = async (res) => {
            await this.logg(`Response status: ${res.statusCode}`);
            if (statusCode > 0) {
                assert.equal(res.statusCode, statusCode);
            }
            res.setEncoding("utf8");
            const body = [];
            res.on("data", (chunk) => body.push(chunk));
            res.on("end", () => resolve(body.join("")));
            if (callback !== undefined) {
                await callback(res);
            }
        };

        if (requestBody === undefined) {
            https.get(url, options, (res) => handler(res)).on("error", reject);
        } else {
            const request = https.request(url, options, (res) => handler(res)).on("error", reject);
            request.write(requestBody);
        }
    });

exports.doGet = async (url, successHandler, failureHandler, headers = {}, responseType = undefined) => {
    const instance = axios.create({
        timeout: 5000,
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    const config = {
        headers: headers
    };
    if (responseType !== undefined) {
        config["responseType"] = responseType;
    }
    await this.log(`Sending GET request to ${url}`);
    return instance
        .get(url, config)
        .then((res) => {
            if (responseType !== "blob" && responseType !== "stream") {
                // let json = JSON.parse(body)
                console.dir(res.data, {depth: null, colors: true});
                // this.log(res.data);
            }
            return successHandler(res);
        })
        .catch((error) => failureHandler(error));
};

exports.doPost = async (url, params = "", headers = {}, successHandler, failureHandler) => {
    const instance = axios.create({
        timeout: 12000,
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    const urlParams = params instanceof URLSearchParams ? params : new URLSearchParams(params);
    await this.logg(`Posting to URL ${url}`);
    return instance
        .post(url, urlParams, {headers: headers})
        .then((res) => {
            this.log(res.data);
            return successHandler(res);
        })
        .catch((error) => {
            if (error.response !== undefined) {
                this.logr(error.response.data);
            }
            return failureHandler(error);
        });
};

exports.waitFor = async (url, successHandler, failureHandler) => {
    const opts = {
        resources: [url],
        delay: 1000,
        interval: 2000,
        timeout: 120000
    };
    await waitOn(opts)
        .then(() => {
            successHandler("good");
        })
        .catch((err) => {
            failureHandler(err);
        });
};

exports.runGradle = async (workdir, opts = [], exitFunc) => {
    let gradleCmd = "./gradlew";
    if (operativeSystemModule.type() === "Windows_NT") {
        gradleCmd = "gradlew.bat";
    }
    const exec = spawn(gradleCmd, opts, {cwd: workdir});
    await this.logg(`Spawned ${gradleCmd} process ID: ${exec.pid}`);
    exec.stdout.on("data", (data) => {
        this.log(data.toString());
    });
    exec.stderr.on("data", (data) => {
        console.error(data.toString());
    });
    exec.on("exit", exitFunc);
    return exec;
};

exports.shutdownCas = async (baseUrl) => {
    await this.logg("Stopping CAS via shutdown actuator");
    const response = await this.doRequest(`${baseUrl}/actuator/shutdown`,
        "POST", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        });
    return JSON.parse(response);
};

exports.assertInnerTextStartsWith = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    await this.log(`Checking ${header} to start with ${value}`);
    assert(header.startsWith(value));
};

exports.assertInnerTextContains = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    await this.log(`Checking [${header}] to contain [${value}]`);
    assert(header.includes(value));
};

exports.assertInnerTextDoesNotContain = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    await this.log(`Checking ${header} to contain ${value}`);
    assert(!header.includes(value));
};

exports.assertInnerText = async (page, selector, value) => {
    const header = await this.innerText(page, selector);
    assert.equal(header, value);
};

exports.assertPageTitle = async (page, value) => {
    const title = await page.title();
    await this.log(`Page Title: ${title}`);
    assert.equal(title, value);
};

exports.assertPageTitleContains = async (page, value) => {
    const title = await page.title();
    await this.log(`Page Title: ${title}`);
    assert(title.includes(value));
};

exports.substring = async (text, word1, word2) => {
    const regex = new RegExp(`${word1}(.*?)${word2}`);
    const match = regex.exec(text);
    return match ? match[1].trim() : null;
};

exports.createJwt = async (payload, key, alg = "RS256", options = {}) => {
    const allOptions = {...{algorithm: alg}, ...options};
    const token = JwtOps.sign(payload, key, allOptions, undefined);
    await this.logg(`Created JWT:\n${token}\n`);
    return token;
};

exports.verifyJwt = async (token, secret, options) => {
    await this.log(`Decoding token ${token}`);
    const decoded = JwtOps.verify(token, secret, options, undefined);
    if (options.complete) {
        await this.logg(`Decoded token header: ${decoded.header}`);
        await this.log("Decoded token payload:");
        await this.logg(decoded.payload);
    } else {
        await this.log("Decoded token payload:");
        await this.logg(decoded);
    }
    return decoded;
};

exports.verifyJwtWithJwk = async (ticket, keyContent, alg = "RS256") => {
    await this.logg("Using key to verify JWT:");
    await this.log(keyContent);
    const secretKey = await jose.importJWK(keyContent, alg);
    const decoded = await jose.jwtVerify(ticket, secretKey);
    await this.log("Verified JWT:");
    await this.logg(decoded.payload);
    return decoded;
};

exports.decryptJwt = async (ticket, keyPath, alg = "RS256") => {
    await this.log(`Using private key path ${keyPath}`);
    if (fs.existsSync(keyPath)) {
        const keyContent = fs.readFileSync(keyPath, "utf8");
        await this.logg("Using private key to verify JWT:");
        await this.log(keyContent);
        const secretKey = await jose.importPKCS8(keyContent, alg);
        const decoded = await jose.jwtDecrypt(ticket, secretKey, {});
        await this.log("Verified JWT:\n");
        await this.logg(decoded.payload);
        return decoded;
    }
    throw `Unable to locate private key ${keyPath} to verify JWT`;
};

exports.decryptJwtWithJwk = async (ticket, keyContent, alg = "RS256") => {
    const secretKey = await jose.importJWK(keyContent, alg);
    await this.log(`Decrypting JWT with key ${JSON.stringify(keyContent)}`);
    const decoded = await jose.jwtDecrypt(ticket, secretKey);
    await this.log("Verified JWT:");
    await this.logg(decoded);
    return decoded;
};

exports.decryptJwtWithSecret = async (jwt, secret, options = {}) => {
    await this.log(`Decrypting JWT with key ${secret}`);
    const buff = jose.base64url.decode(secret);
    const decoded = await jose.jwtDecrypt(jwt, buff, options);
    await this.log("Verified JWT:");
    await this.logg(decoded);
    return decoded;
};

exports.decodeJwt = async (token, complete = false) => {
    await this.log(`Decoding token ${token}`);
    assert(token !== undefined, "Token cannot be undefined");
    const decoded = JwtOps.decode(token, {complete: complete});
    if (complete) {
        await this.logg(`Decoded token header: ${decoded.header}`);
        await this.log("Decoded token payload:");
        await this.logg(decoded.payload);
    } else {
        await this.log("Decoded token payload:");
        await this.logg(decoded);
    }
    return decoded;
};

exports.updateDuoSecurityUserStatus = async (user = "casuser", status = "AUTH") => {
    await this.log(`Updating user account status to ${status} in Duo Security for ${user}...`);
    const body = JSON.stringify({"status": status});
    await this.doRequest(`https://localhost:8443/cas/actuator/duoAdmin/${user}`,
        "PUT",
        {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        200,
        body);
};

exports.fetchDuoSecurityBypassCodes = async (user = "casuser") => {
    await this.log(`Fetching Bypass codes from Duo Security for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/duoAdmin/bypassCodes?username=${user}`,
        "POST", {
            "Accept": "application/json",
            "Content-Type": "application/json"
        });
    return JSON.parse(response)["mfa-duo"];
};

exports.base64Decode = async (data) => {
    const buff = Buffer.from(data, "base64");
    return buff.toString("ascii");
};

exports.screenshot = async (page) => {
    const screenshotsDir = path.join(__dirname, "screenshots");
    if (!fs.existsSync(screenshotsDir)) {
        fs.mkdirSync(screenshotsDir);
        await this.log(`Created screenshots directory: ${screenshotsDir}`);
    }
    const index = Date.now();
    const filePath = path.join(screenshotsDir, `${process.env.SCENARIO}-${index}.png`);
    try {
        const url = await page.url();
        await this.log(`Page URL when capturing screenshot: ${url}`);
        await this.log(`Attempting to take a screenshot and save at ${filePath}`);
        await page.setViewport({width: 1920, height: 1080});
        await page.screenshot({path: filePath, captureBeyondViewport: true, fullPage: true, optimizeForSpeed: true});
        this.logg(`Screenshot saved at ${filePath}`);
    } catch (e) {
        this.logr(`Unable to capture screenshot ${filePath}: ${e}`);
    }
};

exports.isCiEnvironment = async () => process.env.CI !== undefined && process.env.CI === "true";

exports.isNotCiEnvironment = async () => !this.isCiEnvironment();

exports.assertTextContent = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    const header = await this.textContent(page, selector);
    assert.equal(header, value);
};

exports.assertTextContentStartsWith = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    const header = await this.textContent(page, selector);
    assert(header.startsWith(value));
};

exports.mockJsonServer = async (pathMappings, port = 8000) => {
    const app = mockServer(pathMappings, port, "localhost");
    await app.start();
    return app;
};

exports.httpServer = async (root,
    port = 5432,
    authEnabled = true,
    authUser = "restapi",
    authPassword = "YdCP05HvuhOH^*Z") => {
    const config = {
        nodeStatic: {
            root: root
        },
        server: {
            port: port,
            ssl: {
                enabled: false
            }
        },
        auth: {
            enabled: authEnabled,
            name: authUser,
            pass: authPassword
        },
        logger: {
            use: true,
            filename: "restapi.log",
            folder: root
        }
    };
    return new NodeStaticAuth(config);
};

exports.randomNumber = async (min = 1, max = 100) =>
    Math.floor(Math.random() * (max - min + 1)) + min;

exports.killProcess = async (command, args) => {
    ps.lookup({
        command: command,
        arguments: args
    }, (err, resultList) => {
        if (err) {
            throw new Error(err);
        }
        resultList.forEach((process) => {
            this.log("PID: %s, COMMAND: %s, ARGUMENTS: %s",
                process.pid, process.command, process.arguments);
            if (process) {
                ps.kill(process.pid, (err) => {
                    if (err) {
                        throw new Error(err);
                    } else {
                        this.log("Process %s has been killed!", process.pid);
                    }
                });
            }
        });
    });
};

exports.sha256 = async (value) => CryptoJS.SHA256(value);

exports.base64Url = async (value) => CryptoJS.enc.Base64url.stringify(value);

exports.pageVariable = async (page, name) => page.evaluate(name);

exports.extractFromEmail = async (browser) => {
    const page = await browser.newPage();
    await page.goto("http://localhost:8282");
    await this.sleep(2000);
    await this.click(page, "table tbody td a");
    await this.sleep(2000);
    const text = await this.textContent(page, "div[name=bodyPlainText] .well");
    await page.close();
    await this.log(`Extracted from email: ${text}`);
    return text;
};

exports.waitForNavigation = async (page, timeout = 10000) => page.waitForNavigation({timeout: timeout});

exports.goto = async (page, url, retryCount = 5) => {
    let response = null;
    let attempts = 0;
    const timeout = 2000;

    while (response === null && attempts < retryCount) {
        attempts += 1;
        try {
            await this.logg(`Navigating to: ${url}`);
            response = await page.goto(url);
            assert(await page.evaluate(() => document.title) !== null);
        } catch (err) {
            this.logr(`#${attempts}: Failed to goto to ${url}.`);
            this.logr(err.message);
            await this.sleep(timeout);
        }
    }
    if (response !== null) {
        this.logg(`Response status: ${await response.status()}`);
    }
    return response;
};

exports.gotoLoginWithLocale = async (page, service, locale) => this.gotoLoginWithAuthnMethod(page, service, undefined, locale);

exports.gotoLoginWithAuthnMethod = async (page, service, authnMethod = undefined, locale = undefined) => {
    let queryString = (service === undefined ? "" : `service=${service}&`);
    queryString += (authnMethod === undefined ? "" : `authn_method=${authnMethod}&`);
    queryString += (locale === undefined ? "" : `locale=${locale}&`);
    const url = `https://localhost:8443/cas/login?${queryString}`;
    return this.goto(page, url);
};

exports.gotoLogin = async (page, service = undefined, port = 8443, renew = undefined, method = undefined) => {
    let queryString = (service === undefined ? "" : `service=${service}&`);
    queryString += (renew === undefined ? "" : "renew=true&");
    queryString += (method === undefined ? "" : `method=${method}&`);
    const url = `https://localhost:${port}/cas/login?${queryString}`;
    return this.goto(page, url);
};

exports.gotoLogout = async (page, service = undefined, port = 8443) => {
    const url = `https://localhost:${port}/cas/logout${service === undefined ? "" : `?service=${service}`}`;
    return this.goto(page, url);
};

exports.parseXML = async (xml, options = {}) => {
    let parsedXML = undefined;
    const parser = new xml2js.Parser(options);
    await parser.parseString(xml, (err, result) => {
        parsedXML = result;
    });
    return parsedXML;
};

exports.refreshContext = async (url = "https://localhost:8443/cas") => {
    await this.log("Refreshing CAS application context...");
    const response = await this.doRequest(`${url}/actuator/refresh`, "POST");
    await this.log(response);
};

exports.refreshBusContext = async (url = "https://localhost:8443/cas") => {
    await this.log(`Refreshing CAS application context in ${url}`);
    const response = await this.doRequest(`${url}/actuator/busrefresh`, "POST", {}, 204);
    await this.log(response);
};

exports.loginDuoSecurityBypassCode = async (page, username = "casuser", currentCodes = undefined) => {
    await this.sleep(12000);
    await this.click(page, "button#passcode");
    const bypassCodes = currentCodes ?? await this.fetchDuoSecurityBypassCodes(username);
    await this.log(`Duo Security: Retrieved bypass codes ${bypassCodes}`);
    let i = 0;
    const error = false;
    let accepted = false;
    while (!accepted && !error && i < bypassCodes.length) {
        const bypassCode = `${String(bypassCodes[i])}`;
        await page.keyboard.sendCharacter(bypassCode);
        await this.screenshot(page);
        await this.log(`Submitting Duo Security bypass code ${bypassCode}`);
        await this.type(page, "input[name='passcode']", bypassCode);
        await this.screenshot(page);
        await this.pressEnter(page);
        await this.log("Waiting for Duo Security to accept bypass code...");
        await this.sleep(10000);
        const error = await this.isVisible(page, "div.message.error");
        if (error) {
            await this.log("Duo Security is unable to accept bypass code");
            await this.screenshot(page);
            i++;
        } else {
            await this.log(`Duo Security accepted the bypass code ${bypassCode}`);
            accepted = true;
        }
    }
};

exports.dockerContainer = async (name) => {
    const containers = await docker.container.list();
    const results = containers.filter((c) => c.data.Names[0].slice(1) === name);
    await this.log(`Docker containers found for ${name} are\n:`);
    await this.log(results);
    if (results.length > 0) {
        return results[0];
    }
    await this.logr(`Unable to find Docker container with name ${name}`);
    return undefined;
};

exports.readLocalStorage = async (page) => {
    const results = await page.evaluate(() => {
        const json = {};
        for (let i = 0; i < localStorage.length; i++) {
            const key = localStorage.key(i);
            json[key] = localStorage.getItem(key);
        }
        return json;
    });
    this.log(results);
    return results;
};

exports.createZipFile = async (file, callback) => {
    const zip = fs.createWriteStream(file);
    const archive = archiver("zip", {
        zlib: {level: 9}
    });
    archive.pipe(zip);
    await callback(archive);
    await archive.finalize();
};

exports.unzipFile = async (file, targetDirectory) => {
    await fs.createReadStream(file)
        .pipe(unzipper.Extract({path: targetDirectory}))
        .on("close", () => this.log(`Files unzipped successfully @ ${targetDirectory}`));
};

exports.prepareChromium = async () => {
    this.log(`Chromium directory: ${CHROMIUM_USER_DATA_DIR}`);
    const targetDirectory = `${CHROMIUM_USER_DATA_DIR}/user-data-dir`;
    this.removeDirectoryOrFile(targetDirectory);
    this.unzipFile(`${CHROMIUM_USER_DATA_DIR}/user-data-dir.zip`, targetDirectory);
    this.log(`Chromium user data directory: ${targetDirectory}`);
};

this.asciiart("Apereo CAS - Puppeteer");
this.prepareChromium();
