const assert = require('assert');
const axios = require('axios');
const https = require('https');
const http = require('http');
const {spawn} = require('child_process');
const waitOn = require('wait-on');
const JwtOps = require('jsonwebtoken');
const colors = require('colors');
const fs = require("fs");
const {ImgurClient} = require('imgur');
const path = require("path");
const mockServer = require('mock-json-server');
const {Buffer} = require('buffer');
const {PuppeteerScreenRecorder} = require('puppeteer-screen-recorder');
const ps = require("ps-node");
const NodeStaticAuth = require("node-static-auth");
const operativeSystemModule = require("os");
const figlet = require("figlet");
const CryptoJS = require("crypto-js");
const jose = require('jose');
const pino = require('pino');
const xml2js = require('xml2js');

const LOGGER = pino({
    level: "debug",
    transport: {
        target: 'pino-pretty'
    }
});

const BROWSER_OPTIONS = {
    ignoreHTTPSErrors: true,
    headless: (process.env.CI === "true" || process.env.HEADLESS === "true") ? "new" : false,
    devtools: process.env.CI !== "true",
    defaultViewport: null,
    timeout: 60000,
    dumpio: false,
    slowMo: process.env.CI === "true" ? 0 : 10,
    args: ['--start-maximized', "--window-size=1920,1080"]
};


exports.browserOptions = () => BROWSER_OPTIONS;
exports.browserOptions = (opt) => ({
    ...BROWSER_OPTIONS,
    ...opt
});

exports.log = async(text, ...args) => {
    await LOGGER.debug(`👉 ${text}`, args);
};

exports.logy = async (text) => {
    await LOGGER.warn(`🔥 ${colors.yellow(text)}`);
};

exports.logb = async (text) => {
    await LOGGER.debug(`ℹ️ ${colors.blue(text)}`);
};

exports.logg = async (text) => {
    await LOGGER.info(`✅ ${colors.green(text)}`);
};

exports.logr = async (text) => {
    await LOGGER.error(`🔴 ${colors.red(text)}`);
};

exports.logPage = async(page) => {
    const url = await page.url();
    await this.log(`Page URL: ${url}`);
};

exports.removeDirectory = async (directory) => {
    this.logg(`Removing directory ${colors.green(directory)}`);
    if (fs.existsSync(directory)) {
        await fs.rmSync(directory, {recursive: true});
    }
    this.logg(`Removed directory ${colors.green(directory)}`);
    if (fs.existsSync(directory)) {
        await this.logr(`Removed directory still present at: ${directory}`);
    }
};

exports.click = async (page, button) => {
    await page.evaluate((button) => {
        let buttonNode = document.querySelector(button);
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
        let buttons = document.querySelectorAll(button);
        buttons[buttons.length - 1].click();
    }, button);
};

exports.innerHTML = async (page, selector) => {
    let text = await page.$eval(selector, el => el.innerHTML.trim());
    await this.log(`HTML for selector [${selector}] is: [${text}]`);
    return text;
};

exports.innerText = async (page, selector) => {
    let text = await page.$eval(selector, el => el.innerText.trim());
    await this.log(`Text for selector [${selector}] is: [${text}]`);
    return text;
};

exports.elementValue = async (page, selector, valueToSet = undefined) => {
    let text = await page.$eval(selector, el => el.value.trim());
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
    await page.evaluate((button) => {
        let results = [];
        let elements = document.querySelectorAll(button);
        elements.forEach(entry => results.push(entry.innerText.trim()));
        return results;
    }, selector);

exports.textContent = async (page, selector) => {
    let element = await page.$(selector);
    let text = await page.evaluate(element => element.textContent.trim(), element);
    await this.log(`Text content for selector [${selector}] is: [${text}]`);
    return text;
};

exports.inputValue = async (page, selector) => {
    const element = await page.$(selector);
    const text = await page.evaluate(element => element.value, element);
    await this.log(`Input value for selector [${selector}] is: [${text}]`);
    return text;
};

exports.uploadImage = async (imagePath) => {
    let clientId = process.env.IMGUR_CLIENT_ID;
    if (clientId !== null && clientId !== undefined) {
        const client = new ImgurClient({clientId: clientId});
        await this.logg(`Uploading image ${colors.green(imagePath)}`);
        client.on('uploadProgress', (progress) => this.log(progress));
        const response = await client.upload({
            image: fs.createReadStream(imagePath),
            type: 'stream',
        });
        await this.logg(`Uploaded image is at ${colors.green(response.data.link)}`);
    }
};

exports.waitForElement = async (page, selector, timeout = 10000) => await page.waitForSelector(selector, {timeout: timeout});

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
    return await page.waitForNavigation();
};

exports.fetchGoogleAuthenticatorScratchCode = async (user = "casuser") => {
    await this.log(`Fetching Scratch codes for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/gauthCredentialRepository/${user}`,
        "GET", {
            'Accept': 'application/json'
        });
    return JSON.stringify(JSON.parse(response)[0].scratchCodes[0]);
};
exports.isVisible = async (page, selector) => {
    let element = await page.$(selector);
    let result = (element != null && await element.boundingBox() != null);
    await this.log(`Checking element visibility for ${selector} while on page ${page.url()}: ${result}`);
    return result;
};

exports.assertVisibility = async (page, selector) => {
    assert(await this.isVisible(page, selector));
};

exports.assertInvisibility = async (page, selector) => {
    let element = await page.$(selector);
    let result = element == null || await element.boundingBox() == null;
    await this.log(`Checking element invisibility for ${selector} while on page ${page.url()}: ${result}`);
    assert(result);
};


exports.assertCookie = async (page, cookieMustBePresent = true, cookieName = "TGC") => {
    const cookies = (await page.cookies()).filter(c => {
        this.log(`Checking cookie ${c.name}:${c.value}`);
        return c.name === cookieName
    });
    await this.log(`Found cookies ${cookies.length}`);
    if (cookieMustBePresent) {
        await this.log(`Checking for cookie ${cookieName}, which MUST be present`);
        assert(cookies.length !== 0);
        await this.logg(`Asserting cookie:\n${colors.green(JSON.stringify(cookies, undefined, 2))}`);
        return cookies[0];
    }
    await this.log(`Checking for cookie ${cookieName}, which MUST NOT be present`);
    if (cookies.length === 0) {
        await this.logg(`Correct! Cookie ${cookieName} cannot be found`);
    } else {
        await this.logr(`Incorrect! Cookie ${cookieName} can be found`);
        let ck = cookies[0];
        let msg = `Found cookie => name: ${ck.name},value:${ck.value},path:${ck.path},domain:${ck.domain},httpOnly:${ck.httpOnly},secure:${ck.secure}`;
        await this.logb(msg);
        throw msg;
    }
};

exports.submitForm = async (page, selector, predicate = undefined) => {
    await this.log(`Submitting form ${selector}`);
    if (predicate === undefined) {
        await this.log("Waiting for page to produce a valid response status code");
        predicate = async response => response.status() > 0;
    }
    return await Promise.all([
        page.waitForResponse(predicate),
        page.$eval(selector, form => form.submit()),
        page.waitForTimeout(3000)
    ]);
};

exports.pressEnter = async (page) => {
    page.keyboard.press('Enter');
    page.waitForTimeout(1000);
};

exports.type = async (page, selector, value, obfuscate = false) => {
    let logValue = obfuscate ? `******` : value;
    await this.log(`Typing ${logValue} in field ${selector}`);
    await page.$eval(selector, el => el.value = '');
    await page.type(selector, value);
};

exports.newPage = async (browser) => {
    let page = (await browser.pages())[0];
    if (page === undefined) {
        await this.log("Opening a new page...");
        page = await browser.newPage();
    }
    // await page.setDefaultNavigationTimeout(0);
    // await page.setRequestInterception(true);
    await page.bringToFront();
    page
        .on('console', message => {
            if (message.type() === "warning") {
                this.logy(`Console ${message.type()}: ${message.text()}`)
            } else if (message.type() === "error") {
                this.logr(`Console ${message.type()}: ${message.text()}`)
            } else {
                this.logg(`Console ${message.type()}: ${message.text()}`)
            }
        })
        .on('pageerror', ({message}) => this.logr(`Console: ${message}`));
    return page;
};

exports.assertParameter = async (page, param) => {
    await this.log(`Asserting parameter ${param} in URL: ${page.url()}`);
    let result = new URL(page.url());
    let value = result.searchParams.get(param);
    await this.logg(`Parameter ${colors.green(param)} with value ${colors.green(value)}`);
    assert(value != null);
    return value;
};

exports.assertMissingParameter = async (page, param) => {
    let result = new URL(page.url());
    assert(result.searchParams.has(param) === false);
};

exports.sleep = async (ms) =>
    new Promise((resolve) => {
        this.logg(`Waiting for ${ms / 1000} second(s)...`);
        setTimeout(resolve, ms);
    });

exports.assertTicketParameter = async (page, found = true) => {
    await this.log(`Page URL: ${page.url()}`);
    let result = new URL(page.url());
    if (found) {
        assert(result.searchParams.has("ticket"));
        let ticket = result.searchParams.get("ticket");
        await this.log(`Ticket: ${ticket}`);
        assert(ticket != null);
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
        let options = {
            method: method,
            rejectUnauthorized: false,
            headers: headers
        };
        options.agent = new https.Agent(options);

        this.logg(`Contacting ${colors.green(url)} via ${colors.green(method)}`);
        const handler = (res) => {
            this.logg(`Response status code: ${colors.green(res.statusCode)}`);
            // this.logg(`Response headers: ${colors.green(res.headers)}`)
            if (statusCode > 0) {
                assert(res.statusCode === statusCode);
            }
            res.setEncoding("utf8");
            const body = [];
            res.on("data", chunk => body.push(chunk));
            res.on("end", () => resolve(body.join("")));
            if (callback !== undefined) {
                callback(res);
            }
        };

        if (requestBody !== undefined) {
            let request = https.request(url, options, res => handler(res)).on("error", reject);
            request.write(requestBody);
        } else {
            https.get(url, options, res => handler(res)).on("error", reject);
        }
    });

exports.doGet = async (url, successHandler, failureHandler, headers = {}, responseType = undefined) => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    let config = {
        headers: headers
    };
    if (responseType !== undefined) {
        config["responseType"] = responseType
    }
    await this.log(`Sending GET request to ${url}`);
    return await instance
        .get(url, config)
        .then(res => {
            if (responseType !== "blob" && responseType !== "stream") {
                // let json = JSON.parse(body)
                console.dir(res.data, {depth: null, colors: true});
                // this.log(res.data);
            }
            return successHandler(res);
        })
        .catch(error => {
            return failureHandler(error);
        })
};

exports.doPost = async (url, params = "", headers = {}, successHandler, failureHandler) => {
    const instance = axios.create({
        httpsAgent: new https.Agent({
            rejectUnauthorized: false
        })
    });
    let urlParams = params instanceof URLSearchParams ? params : new URLSearchParams(params);
    await this.logg(`Posting to URL ${colors.green(url)}`);
    return await instance
        .post(url, urlParams, {headers: headers})
        .then(res => {
            this.log(res.data);
            return successHandler(res);
        })
        .catch(error => {
            if (error.response !== undefined) {
                this.logr(error.response.data)
            }
            return failureHandler(error);
        })
};

exports.waitFor = async (url, successHandler, failureHandler) => {
    let opts = {
        resources: [url],
        delay: 1000,
        interval: 2000,
        timeout: 120000
    };
    await waitOn(opts)
        .then(() => {
            successHandler("good")
        })
        .catch(err => {
            failureHandler(err);
        });
};

exports.runGradle = async (workdir, opts = [], exitFunc) => {
    let gradleCmd = './gradlew';
    if (operativeSystemModule.type() === 'Windows_NT') {
        gradleCmd = 'gradlew.bat';
    }
    const exec = spawn(gradleCmd, opts, {cwd: workdir});
    await this.logg(`Spawned ${gradleCmd} process ID: ${exec.pid}`);
    exec.stdout.on('data', (data) => {
        this.log(data.toString());
    });
    exec.stderr.on('data', (data) => {
        console.error(data.toString());
    });
    exec.on('exit', exitFunc);
    return exec;
};

exports.launchWsFedSp = async (spDir, opts = []) => {
    let args = ['build', 'appStart', '-q', '-x', 'test', '--no-daemon', `-Dsp.sslKeystorePath=${process.env.CAS_KEYSTORE}`];
    args = args.concat(opts);
    await this.logg(`Launching WSFED SP in ${spDir} with ${args}`);
    return this.runGradle(spDir, args, (code) => this.log(`WSFED SP Child process exited with code ${code}`));
};

exports.stopGradleApp = async (gradleDir, deleteDir = true) => {
    let args = ['appStop', '-q', '--no-daemon'];
    await this.logg(`Stopping process in ${gradleDir} with ${args}`);
    return this.runGradle(gradleDir, args, (code) => {
        this.log(`Stopped child process exited with code ${code}`);
        if (deleteDir) {
            this.sleep(3000);
            this.removeDirectory(gradleDir);
        }
    });
};

exports.shutdownCas = async (baseUrl) => {
    await this.logg(`Stopping CAS via shutdown actuator`);
    const response = await this.doRequest(`${baseUrl}/actuator/shutdown`,
        "POST", {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        });
    return JSON.parse(response)
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
    assert(header === value)
};

exports.assertPageTitle = async (page, value) => {
    const title = await page.title();
    await this.log(`Page Title: ${title}`);
    assert(title === value)
};

exports.assertPageTitleContains = async (page, value) => {
    const title = await page.title();
    await this.log(`Page Title: ${title}`);
    assert(title.includes(value))
};

exports.recordScreen = async (page) => {
    let index = Math.floor(Math.random() * 10000);
    let filePath = path.join(__dirname, `/recording-${index}.mp4`);
    const config = {
        followNewTab: true,
        fps: 60,
        videoFrame: {
            width: 1024,
            height: 768,
        },
        aspectRatio: '4:3',
    };
    const recorder = new PuppeteerScreenRecorder(page, config);
    await this.log(`Recording screen to ${filePath}`);
    await recorder.start(filePath);
    return recorder;
};

exports.createJwt = async (payload, key, alg = "RS256", options = {}) => {
    let allOptions = {...{algorithm: alg}, ...options};
    const token = JwtOps.sign(payload, key, allOptions, undefined);
    await this.logg(`Created JWT:\n${colors.green(token)}\n`);
    return token;
};

exports.verifyJwt = async (token, secret, options) => {
    await this.log(`Decoding token ${token}`);
    let decoded = JwtOps.verify(token, secret, options, undefined);
    if (options.complete) {
        await this.logg(`Decoded token header: ${colors.green(decoded.header)}`);
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
        const keyContent = fs.readFileSync(keyPath, 'utf8');
        await this.logg("Using private key to verify JWT:");
        await this.log(keyContent);
        const secretKey = await jose.importPKCS8(keyContent, alg);
        const decoded = await jose.jwtDecrypt(ticket, secretKey, {});
        await this.log("Verified JWT:\n");
        await this.logg(decoded.payload);
        return decoded;
    }
    throw `Unable to locate private key ${keyPath} to verify JWT`
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

    let decoded = JwtOps.decode(token, {complete: complete});
    if (complete) {
        await this.logg(`Decoded token header: ${colors.green(decoded.header)}`);
        await this.log("Decoded token payload:");
        await this.logg(decoded.payload);
    } else {
        await this.log("Decoded token payload:");
        await this.logg(decoded);
    }
    return decoded;
};

exports.fetchDuoSecurityBypassCodes = async (user = "casuser") => {
    await this.log(`Fetching Bypass codes from Duo Security for ${user}...`);
    const response = await this.doRequest(`https://localhost:8443/cas/actuator/duoAdmin/bypassCodes?username=${user}`,
        "POST", {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
        });
    return JSON.parse(response)["mfa-duo"];
};

exports.base64Decode = async (data) => {
    let buff = Buffer.from(data, 'base64');
    return buff.toString('ascii');
};

exports.screenshot = async (page) => {
    if (await this.isCiEnvironment()) {
        let index = Date.now();
        let filePath = path.join(__dirname, `/screenshot-${index}.png`);
        try {
            let url = await page.url();
            await this.log(`Page URL when capturing screenshot: ${url}`);
            await this.log(`Attempting to take a screenshot and save at ${filePath}`);
            await page.setViewport({width: 1920, height: 1080});
            await page.screenshot({path: filePath, captureBeyondViewport: true, fullPage: true});
            this.logg(`Screenshot saved at ${colors.green(filePath)}`);
            await this.uploadImage(filePath);
        } catch (e) {
            this.logr(colors.red(`Unable to capture screenshot ${filePath}: ${e}`));
        }
    } else {
        await this.log("Capturing screenshots is disabled in non-CI environments");
    }
};

exports.isCiEnvironment = async () => process.env.CI !== undefined && process.env.CI === "true";

exports.isNotCiEnvironment = async () => !this.isCiEnvironment();

exports.assertTextContent = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    let header = await this.textContent(page, selector);
    assert(header === value);
};

exports.assertTextContentStartsWith = async (page, selector, value) => {
    await page.waitForSelector(selector, {visible: true});
    let header = await this.textContent(page, selector);
    assert(header.startsWith(value));
};

exports.mockJsonServer = async (pathMappings, port = 8000) => {
    let app = mockServer(pathMappings, port, "localhost");
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
            filename: 'restapi.log',
            folder: root
        }
    };
    return new NodeStaticAuth(config);
};

exports.randomNumber = async (min = 1, max = 100) =>
    Math.floor(Math.random() * (max - min + 1)) + min;

exports.killProcess = async (command, arguments) => {
    ps.lookup({
        command: command,
        arguments: arguments
    }, (err, resultList) => {
        if (err) {
            throw new Error(err);
        }
        resultList.forEach(process => {
            this.log('PID: %s, COMMAND: %s, ARGUMENTS: %s',
                process.pid, process.command, process.arguments);
            if (process) {
                ps.kill(process.pid, err => {
                    if (err) {
                        throw new Error(err);
                    } else {
                        this.log('Process %s has been killed!', process.pid);
                    }
                });
            }
        });
    });
};

exports.sha256 = async (value) => CryptoJS.SHA256(value);

exports.base64Url = async (value) => CryptoJS.enc.Base64url.stringify(value);

exports.pageVariable = async (page, name) => await page.evaluate(name);

exports.goto = async (page, url, retryCount = 5) => {
    let response = null;
    let attempts = 0;
    const timeout = 2000;

    while (response === null && attempts < retryCount) {
        attempts += 1;
        try {
            await this.logg(`Navigating to: ${colors.green(url)}`);
            response = await page.goto(url);
            assert(await page.evaluate(() => document.title) !== null);
        } catch (err) {
            this.logr(colors.red(`#${attempts}: Failed to goto to ${url}.`));
            this.logr(colors.red(err.message));
            await this.sleep(timeout);
        }
    }
    if (response != null) {
        this.logg(`Response status: ${colors.green(await response.status())}`);
    }
    return response;
};

exports.gotoLogin = async(page, service = undefined, port = 8443) => {
    const url = `https://localhost:${port}/cas/login` + (service === undefined ? "" : `?service=${service}`);
    return await this.goto(page, url);
};

exports.gotoLogout = async(page, port = 8443) => await this.goto(page, `https://localhost:${port}/cas/logout`);

exports.parseXML = async(xml, options = {}) => {
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

exports.loginDuoSecurityBypassCode = async (page, username = "casuser") => {
    await page.waitForTimeout(12000);
    await this.click(page, "button#passcode");
    let bypassCodes = await this.fetchDuoSecurityBypassCodes(username);
    await this.log(`Duo Security: Retrieved bypass codes ${bypassCodes}`);
    let i = 0;
    let error = false;
    while (!error && i < bypassCodes.length) {
        let bypassCode = `${String(bypassCodes[i])}`;
        await page.keyboard.sendCharacter(bypassCode);
        await this.screenshot(page);
        await this.log(`Submitting Duo Security bypass code ${bypassCode}`);
        await this.type(page, "input[name='passcode']", bypassCode);
        await this.screenshot(page);
        await this.pressEnter(page);
        await this.log(`Waiting for Duo Security to accept bypass code...`);
        await page.waitForTimeout(10000);
        let error = await this.isVisible(page, "div.message.error");
        if (error) {
            await this.log(`Duo Security is unable to accept bypass code`);
            await this.screenshot(page);
            i++;
        } else {
            await this.log(`Duo Security accepted the bypass code ${bypassCode}`);
            return;
        }
    }
};

this.asciiart("Apereo CAS - Puppeteer");
