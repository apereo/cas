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
        }
        var qidx = location.href.indexOf('?');
        if (qidx != -1) {
            var queryParams = location.href.substring(qidx);
            action += queryParams;
        }
        action += hash;
        $('#fm1').attr('action', action);
        
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

function animateCasMessageBoxes() {
    //flash error box
    $('#msg.errors').animate({backgroundColor: 'rgb(187,0,0)'}, 30).animate({backgroundColor: 'rgb(255,238,221)'}, 500);

    //flash success box
    $('#msg.success').animate({backgroundColor: 'rgb(51,204,0)'}, 30).animate({backgroundColor: 'rgb(221,255,170)'}, 500);

    //flash confirm box
    $('#msg.question').animate({backgroundColor: 'rgb(51,204,0)'}, 30).animate({backgroundColor: 'rgb(221,255,170)'}, 500);
}

function disableEmptyInputFormSubmission() {
    var fields = $('#fm1 input[name="username"],[name="password"]');

    if (fields.length == 2) {
        fields.on('input', function (event) {
            var enableSubmission = $('#fm1 input[name="username"]').val().trim() &&
                $('#fm1 input[name="password"]').val().trim();

            if (enableSubmission) {
                $('#fm1 input[name=submit]').removeAttr('disabled');
                event.stopPropagation();
            } else {
                $('#fm1 input[name=submit]').attr('disabled', 'true');
            }
        });
    }

    /**
     * Handle auto-complete events to the extent possible.
     */
    if ($('#fm1 input[name="username"]').length > 0) {
        setTimeout(function () {
            var uid = $('#username').val();
            if (uid != null && uid != '') {
                $('#username').change();
                $('#username').focus();
                $('#fm1 input[name=submit]').removeAttr('disabled');
            }

        }, 100);
    }
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
            $('#cookiesDisabled').animate({backgroundColor: 'rgb(187,0,0)'}, 30).animate({backgroundColor: 'rgb(255,238,221)'}, 500);
        }

        animateCasMessageBoxes();
        disableEmptyInputFormSubmission();
        preserveAnchorTagOnForm();

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
