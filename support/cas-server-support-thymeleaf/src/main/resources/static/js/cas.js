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
                    field.foundation_.adapter_.registerInputInteractionHandler('keypress', cas.checkCaps);
                }
            }

            //MDCTextFieldIconAdapter
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

function requestGeoPosition() {
    // console.log('Requesting GeoLocation data from the browser...');
    if (navigator.geolocation) {
        navigator.geolocation.watchPosition(showGeoPosition, logGeoLocationError,
            { maximumAge: 600000, timeout: 8000, enableHighAccuracy: true });
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
