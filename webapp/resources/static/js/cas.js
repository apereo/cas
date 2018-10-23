/* global trackGeoLocation, jqueryReady */

/* exported resourceLoadedSuccessfully */

function requestGeoPosition() {
    // console.log('Requesting GeoLocation data from the browser...');
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(showGeoPosition, logGeoLocationError,
            {maximumAge: 600000, timeout: 5000, enableHighAccuracy: true});
    } else {
        // console.log('Browser does not support Geo Location');
    }
}

function logGeoLocationError(error) {
    switch (error.code) {
    case error.PERMISSION_DENIED:
        // console.log('User denied the request for GeoLocation.');
        break;
    case error.POSITION_UNAVAILABLE:
        // console.log('Location information is unavailable.');
        break;
    case error.TIMEOUT:
        // console.log('The request to get user location timed out.');
        break;
    default:
        // console.log('An unknown error occurred.');
        break;
    }
}

function showGeoPosition(position) {
    $('[name="geolocation"]').val(position.coords.latitude + ','
        + position.coords.longitude + ',' + position.coords.accuracy + ',' + position.timestamp);
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

function areCookiesEnabled() {
    if ($.cookie == undefined) {
        return;
    }

    $.cookie('cookiesEnabled', 'true');
    var value = $.cookie('cookiesEnabled');
    $.removeCookie('cookiesEnabled');
    return value != undefined;

}

function resourceLoadedSuccessfully() {
    $(document).ready(function () {

        if (trackGeoLocation) {
            requestGeoPosition();
        }

        if ($(':focus').length === 0) {
            $('input:visible:enabled:first').focus();
        }

        if (areCookiesEnabled()) {
            $('#cookiesDisabled').hide();
        } else {
            $('#cookiesDisabled').show();
        }

        preserveAnchorTagOnForm();
        preventFormResubmission();

        $('#capslock-on').hide();
        $('#fm1 input[name="username"],[name="password"]').trigger('input');
        $('#fm1 input[name="username"]').focus();

        $('#password').keypress(function (e) {
            var s = String.fromCharCode(e.which);
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !e.shiftKey) {
                $('#capslock-on').show();
            } else {
                $('#capslock-on').hide();
            }
        });
        if (typeof(jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}
