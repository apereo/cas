(function (material, $) {
    var cas = {
        init: function () {
            cas.attachFields();
            material.autoInit();
        },
        attachFields: function () {
            var divs = document.querySelectorAll('.mdc-text-field'),
                field;
            var div;
            for (i = 0; i < divs.length; ++i) {
                div = divs[i];
                field = material.textField.MDCTextField.attachTo(div);
                if (div.classList.contains('caps-check')) {
                    field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
                }
            }
            let selector = document.querySelector('.mdc-select.authn-source');
            if (selector != null) {
                const select = new material.select.MDCSelect(selector);
                select.listen('MDCSelect:change', () => {
                    $('#source').val(select.value);
                });
                $('#source').val(select.value);
            }
        },
        checkCaps: function (ev) {
            var s = String.fromCharCode(ev.which);
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
                ev.target.parentElement.classList.add('caps-on');
            } else {
                console.log('caps off')
                ev.target.parentElement.classList.remove('caps-on');
            }
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        cas.init();
    });
})(mdc, jQuery);

function randomWord() {
    let things = ["admiring", "adoring", "affectionate", "agitated", "amazing",
        "angry", "awesome", "beautiful", "blissful", "bold", "boring",
        "brave", "busy", "charming", "clever", "cool", "compassionate", "competent",
        "confident", "dazzling", "determined", "sweet", "sad", "silly",
        "relaxed", "romantic", "sad", "serene", "sharp", "quirky", "scared",
        "sleepy", "stoic", "strange", "suspicious", "sweet", "tender", "thirsty",
        "trusting", "unruffled", "upbeat", "vibrant", "vigilant", "vigorous",
        "wizardly", "wonderful", "youthful", "zealous", "zen"]

    let names = ["austin", "borg", "bohr", "wozniak", "bose", "wu", "wing", "wilson",
        "boyd", "guss", "jobs", "hawking", "hertz", "ford", "solomon", "spence",
        "turing", "torvalds", "morse", "ford", "penicillin", "lovelace", "davinci",
        "darwin", "buck", "brown", "benz", "boss", "allen", "gates", "bose",
        "edison", "einstein", "feynman", "ferman", "franklin", "lincoln", "jefferson",
        "mandela", "gandhi", "curie", "newton", "tesla", "faraday", "bell",
        "aristotle", "hubble", "nobel", "pascal", "washington", "galileo"]

    let n1 = things[Math.floor(Math.random() * things.length)];
    let n2 = names[Math.floor(Math.random() * names.length)];
    return n1 + "_" + n2
}

function copyClipboard(element) {
    element.select();
    element.setSelectionRange(0, 99999);
    document.execCommand("copy");
}

function requestGeoPosition() {
    // console.log('Requesting GeoLocation data from the browser...');
    if (navigator.geolocation) {
        navigator.geolocation.watchPosition(showGeoPosition, logGeoLocationError,
            {maximumAge: 600000, timeout: 8000, enableHighAccuracy: true});
    } else {
        console.log('Browser does not support Geo Location');
    }
}

function logGeoLocationError(error) {
    switch (error.code) {
        case error.PERMISSION_DENIED:
            console.log('User denied the request for GeoLocation.');
            break;
        case error.POSITION_UNAVAILABLE:
            console.log('Location information is unavailable.');
            break;
        case error.TIMEOUT:
            console.log('The request to get user location timed out.');
            break;
        default:
            console.log('An unknown error occurred.');
            break;
    }
}

function showGeoPosition(position) {
    let loc = position.coords.latitude + ',' + position.coords.longitude
        + ',' + position.coords.accuracy + ',' + position.timestamp;
    console.log("Tracking geolocation for " + loc);
    $('[name="geolocation"]').val(loc);
}


function preserveAnchorTagOnForm() {
    $('#fm1').submit(function () {
        var location = self.document.location;
        var hash = decodeURIComponent(location.hash);

        if (hash != undefined && hash != '' && hash.indexOf('#') === -1) {
            hash = '#' + hash;
        }

        var action = $('#fm1').attr('action');
        if (action == undefined) {
            action = location.href;
        } else {
            var qidx = location.href.indexOf('?');
            if (qidx != -1) {
                var queryParams = location.href.substring(qidx);
                action += queryParams;
            }
        }
        action += hash;
        $('#fm1').attr('action', action);

    });
}

function preventFormResubmission() {
    $('form').submit(function () {
        $(':submit').attr('disabled', true);
        var altText = $(':submit').attr('data-processing-text');
        if (altText) {
            $(':submit').attr('value', altText);
        }
        return true;
    });
}

function resourceLoadedSuccessfully() {

    $(document).ready(function () {

        if (trackGeoLocation) {
            requestGeoPosition();
        }

        if ($(':focus').length === 0) {
            $('input:visible:enabled:first').focus();
        }

        preserveAnchorTagOnForm();
        preventFormResubmission();
        $('#fm1 input[name="username"],[name="password"]').trigger('input');
        $('#fm1 input[name="username"]').focus();

        let $revealpassword = $('.reveal-password');
        $revealpassword.mouseup(function (ev) {
            $('.pwd').attr('type', 'password');
            $(".reveal-password-icon").removeClass("mdi mdi-eye-off").addClass("mdi mdi-eye");
            ev.preventDefault();
        })

        $revealpassword.mousedown(function (ev) {
            $('.pwd').attr('type', 'text');
            $(".reveal-password-icon").removeClass("mdi mdi-eye").addClass("mdi mdi-eye-off");
            ev.preventDefault();
        });

        if (typeof (jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}
