function randomWord() {
    let things = ["admiring", "adoring", "affectionate", "agitated", "amazing",
        "angry", "awesome", "beautiful", "blissful", "bold", "boring",
        "brave", "busy", "charming", "clever", "cool", "compassionate", "competent",
        "confident", "dazzling", "determined", "sweet", "sad", "silly",
        "relaxed", "romantic", "sad", "serene", "sharp", "quirky", "scared",
        "sleepy", "stoic", "strange", "suspicious", "sweet", "tender", "thirsty",
        "trusting", "unruffled", "upbeat", "vibrant", "vigilant", "vigorous",
        "wizardly", "wonderful", "youthful", "zealous", "zen"];

    let names = ["austin", "borg", "bohr", "wozniak", "bose", "wu", "wing", "wilson",
        "boyd", "guss", "jobs", "hawking", "hertz", "ford", "solomon", "spence",
        "turing", "torvalds", "morse", "ford", "penicillin", "lovelace", "davinci",
        "darwin", "buck", "brown", "benz", "boss", "allen", "gates", "bose",
        "edison", "einstein", "feynman", "ferman", "franklin", "lincoln", "jefferson",
        "mandela", "gandhi", "curie", "newton", "tesla", "faraday", "bell",
        "aristotle", "hubble", "nobel", "pascal", "washington", "galileo"];

    const n1 = things[Math.floor(Math.random() * things.length)];
    const n2 = names[Math.floor(Math.random() * names.length)];
    return `${n1}_${n2}`;
}

function copyClipboard(element) {
    element.select();
    element.setSelectionRange(0, 99999);
    document.execCommand("copy");
}

function getLastTwoWords(str) {
    const parts = str.split(".");
    return parts.slice(-2).join(".");
}

function formatDateYearMonthDayHourMinute(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = (d.getMonth() + 1).toString().padStart(2, "0"); // Months are zero-based
    const day = d.getDate().toString().padStart(2, "0");
    const hours = d.getHours().toString().padStart(2, "0");
    const minutes = d.getMinutes().toString().padStart(2, "0");
    return `${year}-${month}-${day} ${hours}:${minutes}`;
}

function formatDateYearMonthDay(date) {
    const d = new Date(date);
    const year = d.getFullYear();
    const month = (d.getMonth() + 1).toString().padStart(2, "0");
    const day = d.getDate().toString().padStart(2, "0");
    return `${year}-${month}-${day}`;
}

function toKebabCase(str) {
    return str
        .replace(/([a-z0-9])([A-Z])/g, '$1-$2')
        .replace(/[^a-zA-Z0-9.[\]]+/g, '-')
        .replace(/^-+|-+$/g, '')
        .toLowerCase();      
}


function camelcaseToTitleCase(str) {
    return str
        .replace(/([A-Z])/g, " $1")
        .replace(/^./, char => char.toUpperCase())
        .trim();
}

function flattenJSON(data) {
    let result = {};
    let l = undefined;

    function recurse(cur, prop) {
        if (Object(cur) !== cur) {
            result[prop] = cur;
        } else if (Array.isArray(cur)) {
            for (let i = 0, l = cur.length; i < l; i++) {
                recurse(cur[i], `${prop}[${i}]`);
            }
            if (l === 0) {
                result[prop] = [];
            }
        } else {
            let isEmpty = true;
            for (let p in cur) {
                isEmpty = false;
                recurse(cur[p], prop ? `${prop}.${p}` : p);
            }
            if (isEmpty && prop) {
                result[prop] = {};
            }
        }
    }

    recurse(data, "");
    return result;
}

function convertMemoryToGB(memoryStr) {
    const units = {
        B: 1,
        KB: 1024,
        MB: 1024 ** 2,
        GB: 1024 ** 3,
        TB: 1024 ** 4
    };
    const regex = /(\d+(\.\d+)?)\s*(B|KB|MB|GB|TB)/i;
    const match = memoryStr.match(regex);
    const value = parseFloat(match[1]);
    const unit = match[3].toUpperCase();
    const bytes = value * units[unit];
    return bytes / units.GB;
}

function isValidURL(str) {
    let pattern = new RegExp("^(https?:\\/\\/)?" + // protocol
        "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" + // domain name
        "((\\d{1,3}\\.){3}\\d{1,3}))" + // OR ip (v4) address
        "(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*" + // port and path
        "(\\?[;&a-z\\d%_.~+=-]*)?" + // query string
        "(\\#[-a-z\\d_]*)?$", "i"); // fragment locator
    return !!pattern.test(str);
}

function requestGeoPosition() {
    // console.log('Requesting GeoLocation data from the browser...');
    if (navigator.geolocation) {
        navigator.geolocation.watchPosition(showGeoPosition, logGeoLocationError,
            {maximumAge: 600000, timeout: 5000, enableHighAccuracy: true});
    } else {
        console.log("Browser does not support Geo Location");
    }
}

function logGeoLocationError(error) {
    switch (error.code) {
    case error.PERMISSION_DENIED:
        console.log("User denied the request for GeoLocation.");
        break;
    case error.POSITION_UNAVAILABLE:
        console.log("Location information is unavailable.");
        break;
    case error.TIMEOUT:
        console.log("The request to get user location timed out.");
        break;
    default:
        console.log("An unknown error occurred.");
        break;
    }
}

function showGeoPosition(position) {
    let loc = `${position.coords.latitude},${position.coords.longitude},${position.coords.accuracy},${position.timestamp}`;
    console.log(`Tracking geolocation for ${loc}`);
    $("[name=\"geolocation\"]").val(loc);
}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}

function preserveAnchorTagOnForm() {
    $("#fm1").submit(() => {
        let location = self.document.location;

        let action = $("#fm1").attr("action");
        if (action === undefined) {
            action = location.href;
        } else {
            action += location.search + encodeURIComponent(location.hash);
        }
        console.log(`Preserving URL fragment in form action: ${action}`);
        $("#fm1").attr("action", action);

    });
}

function preventFormResubmission() {
    $("form").submit(() => {
        const dataDisableSubmitValue = $(this).attr('data-disable-submit');
        if (dataDisableSubmitValue) {
            $(":submit").attr("disabled", true);
        }
        let altText = $(":submit").attr("data-processing-text");
        if (altText) {
            $(":submit").attr("value", altText);
        }
        return true;
    });
}

/**
 * Checks if a specified storage type is available for use.
 * Attempts to perform a read and write operation to verify availability.
 * 
 * @param {string} type - The storage type to test (e.g., 'localStorage', 'sessionStorage').
 * @returns {boolean} True if the specified storage type is available, otherwise false.
 */
function isStorageAvailable(type) {
    try {
        const storage = window[type];
        const testKey = '__storage_test__';
        storage.setItem(testKey, testKey);
        storage.removeItem(testKey);
        return true;
    } catch (e) {
        return e instanceof DOMException && (
            e.code === 22 || // Chrome-specific error code for storage quota exceeded
            e.code === 1014 || // Firefox-specific error code for storage quota exceeded
            e.name === 'QuotaExceededError' || 
            e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
            storage && storage.length !== 0;
    }
}

/**
 * Determines the most suitable storage mechanism for use.
 * Prioritises `localStorage` if available, falling back to `sessionStorage` when necessary.
 * 
 * @returns {Storage|null} The chosen storage object, or null if no storage is available.
 */
function getStorage() {
    if (isStorageAvailable('localStorage')) {
        return window.localStorage;
    }
    if (isStorageAvailable('sessionStorage')) {
        console.warn('Falling back to sessionStorage as localStorage is unavailable.');
        return window.sessionStorage;
    }
    console.error('No available storage detected.');
    return null;
}

/**
 * Writes a value to the specified storage with a nested context key.
 * The value is stored in JSON format, enabling support for complex payloads.
 * 
 * @param {Storage} storage - The storage object to write to.
 * @param {string} key - The key under which the data should be stored.
 * @param {object} browserStorage - An object containing `context` (sub-key) and `payload` (value) to store.
 */
function writeToStorage(storage, key, browserStorage) {
    if (!storage) {
        console.error("No storage available for write-ops.");
        return;
    }
    try {
        const payload = readFromStorage(storage, key) || {};
        payload[browserStorage.context] = browserStorage.payload;
        storage.setItem(key, JSON.stringify(payload));
        console.log(`Stored payload for context "${browserStorage.context}": ${browserStorage.payload}`);
    } catch (e) {
        console.error(`Failed to write to storage: ${e.message}`);
    }
}

/**
 * Reads a value from the specified storage.
 * Parses JSON payloads and handles corrupted data gracefully by removing it.
 * 
 * @param {Storage} storage - The storage object to read from.
 * @param {string} key - The key to read data from.
 * @returns {object} The parsed data object, or an empty object if no data is found or an error occurs.
 */
function readFromStorage(storage, key) {
    if (!storage) {
        console.error("No storage available for read-ops.");
        return {};
    }
    try {
        const payload = storage.getItem(key);
        if (!payload) {
            console.log(`No data found under key "${key}".`);
            return {};
        }
        console.log(`Read payload for key "${key}": ${payload}`);
        return JSON.parse(payload);
    } catch (e) {
        console.error(`Failed to read from storage: ${e.message}`);
        storage.removeItem(key); // Clean up corrupted data
        return {};
    }
}

/**
 * The key used for storing CAS-specific data in localStorage or sessionStorage.
 */
const STORAGE_KEY = "CAS";

/**
 * Writes a value to the appropriate storage (localStorage or sessionStorage).
 * 
 * @param {object} browserStorage - An object containing `context` (sub-key) and `payload` (value) to store.
 */
function writeToLocalStorage(browserStorage) {
    writeToStorage(getStorage(), STORAGE_KEY, browserStorage);
}

/**
 * Reads a value from the appropriate storage (localStorage or sessionStorage).
 * 
 * @returns {object} The data object stored under the `STORAGE_KEY`, or an empty object if no data is found.
 */
function readFromLocalStorage() {
    return readFromStorage(getStorage(), STORAGE_KEY);
}

/**
 * Clears the CAS-specific data stored in the given storage (localStorage or sessionStorage).
 * 
 * @param {Storage} storage - The storage object to clear data from.
 */
function clearStorage(storage) {
    if (storage) {
        storage.removeItem(STORAGE_KEY); // Clears only the CAS-specific data
        console.log("Cleared CAS data from storage.");
    } else {
        console.error("No storage available to clear.");
    }
}

function loginFormSubmission() {
    return true;
}

function resourceLoadedSuccessfully() {
    $(document).ready(() => {
        if (trackGeoLocation) {
            requestGeoPosition();
        }

        if ($(":focus").length === 0) {
            $("input:visible:enabled:first").focus();
        }

        preserveAnchorTagOnForm();
        preventFormResubmission();
        $("#fm1 input[name=\"username\"],[name=\"password\"]").trigger("input");
        $("#fm1 input[name=\"username\"]").focus();

        $(".reveal-password").click(ev => {
            if ($(".pwd").attr("type") === "text") {
                $(".pwd").attr("type", "password");
                $(".reveal-password-icon").removeClass("mdi mdi-eye-off").addClass("mdi mdi-eye");
            } else {
                $(".pwd").attr("type", "text");
                $(".reveal-password-icon").removeClass("mdi mdi-eye").addClass("mdi mdi-eye-off");
            }
            ev.preventDefault();
        });
        // console.log(`JQuery Ready: ${typeof (jqueryReady)}`);
        if (typeof (jqueryReady) == "function") {
            jqueryReady();
        }
        if (typeof hljs !== 'undefined') {
            hljs.highlightAll();
        }
    });
}

function autoHideElement(id, timeout = 1500) {
    let elementToFadeOut = document.getElementById(id);

    function hideElement() {
        $(elementToFadeOut).fadeOut(500);
    }

    setTimeout(hideElement, timeout);
}

function initializeAceEditor(id, mode="json") {
    ace.require("ace/ext/language_tools");
    const beautify = ace.require("ace/ext/beautify");
    const editor = ace.edit(id);
    editor.setTheme("ace/theme/cobalt");
    editor.session.setMode(`ace/mode/${mode}`);
    editor.session.setUseWrapMode(true);
    editor.session.setTabSize(4);
    editor.setShowPrintMargin(false);
    editor.commands.addCommand({
        name: "deleteLine",
        bindKey: {
            win: "Ctrl-Y",
            mac: "Command-Y"
        },
        exec: editor => {
            const cursorPosition = editor.getCursorPosition();
            editor.session.remove({
                start: {row: cursorPosition.row, column: 0},
                end: {row: cursorPosition.row + 1, column: 0}
            });
        },
        readOnly: false
    });
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableLiveAutocompletion: true,
        enableSnippets: true,
        selectionStyle: "text",
        highlightActiveLine: true,
        highlightSelectedWord: true,
        enableAutoIndent: true,
        cursorStyle: "wide",
        useSoftTabs: true,
        hScrollBarAlwaysVisible: false,
        showInvisibles: false,
        animatedScroll: true,
        highlightGutterLine: true,
        showLineNumbers: true,
        fontSize: "16px"
    });
    beautify.beautify(editor.session);
    return editor;
}
