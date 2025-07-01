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

function writeToLocalStorage(browserStorage) {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support local storage for write-ops");
    } else {
        let payload = readFromLocalStorage(browserStorage);
        window.localStorage.removeItem("CAS");
        payload[browserStorage.context] = browserStorage.payload;
        window.localStorage.setItem("CAS", JSON.stringify(payload));
        console.log(`Stored ${browserStorage.payload} in local storage under key ${browserStorage.context}`);
    }
}

function readFromLocalStorage(browserStorage) {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support local storage for read-ops");
        return null;
    }
    try {
        let payload = window.localStorage.getItem("CAS");
        console.log(`Read ${payload} in local storage`);
        return payload === null ? {} : JSON.parse(payload);
    } catch (e) {
        console.log(`Failed to read from local storage: ${e}`);
        window.localStorage.removeItem("CAS");
        return {};
    }
}

function clearLocalStorage() {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support local storage for write-ops");
    } else {
        window.localStorage.clear();
    }
}

function writeToSessionStorage(browserStorage) {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support session storage for write-ops");
    } else {
        let payload = readFromSessionStorage(browserStorage);
        window.sessionStorage.removeItem("CAS");
        payload[browserStorage.context] = browserStorage.payload;
        window.sessionStorage.setItem("CAS", JSON.stringify(payload));
        console.log(`Stored ${browserStorage.payload} in session storage under key ${browserStorage.context}`);
    }
}

function clearSessionStorage() {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support session storage for write-ops");
    } else {
        window.sessionStorage.clear();
        console.log("Cleared session storage");
    }
}

function readFromSessionStorage(browserStorage) {
    if (typeof (Storage) === "undefined") {
        console.log("Browser does not support session storage for read-ops");
        return null;
    }
    try {
        let payload = window.sessionStorage.getItem("CAS");
        console.log(`Read ${payload} in session storage`);
        return payload === null ? {} : JSON.parse(payload);
    } catch (e) {
        console.log(`Failed to read from session storage: ${e}`);
        window.sessionStorage.removeItem("CAS");
        return {};
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

        $(".reveal-password").on("click", function(ev) {
            ev.preventDefault();
            const btn  = $(this);
            const pwd  = $(".pwd");
            const icon = $(".reveal-password-icon");

            btn.attr("aria-checked", (i, val) => val === "true" ? "false" : "true");
            if (pwd.attr("type") === "text") {
                pwd.attr("type", "password");
                icon.removeClass("mdi-eye-off").addClass("mdi-eye");
            } else {
                pwd.attr("type", "text");
                icon.removeClass("mdi-eye").addClass("mdi-eye-off");
            }
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
