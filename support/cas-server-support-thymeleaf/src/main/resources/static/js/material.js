((material, $) => {
    let cas = {
        init: () => {
            cas.attachFields();
            material.autoInit();
        },
        attachFields: () => {
            new material.textField.MDCTextFieldHelperText(document.querySelectorAll('.mdc-text-field-helper-text'));

            let divs = document.querySelectorAll('.mdc-text-field'),
                field;
            let div;
            for (i = 0; i < divs.length; ++i) {
                div = divs[i];
                field = material.textField.MDCTextField.attachTo(div);
                if (div.classList.contains('caps-check')) {
                    field.foundation.adapter.registerInputInteractionHandler('keypress', cas.checkCaps);
                }
            }
            let selector = document.querySelector('.mdc-select');
            if (selector != null) {
                const select = new material.select.MDCSelect(selector);
                select.listen('MDCSelect:change', () => {
                    $('#source').val(select.value);
                });
                $('#source').val(select.value);
            }

            let tooltips = document.querySelector('.mdc-tooltip')
            if (tooltips != null) {
                new mdc.tooltip.MDCTooltip(tooltips);
            }
            let banners = document.querySelector('.mdc-banner')
            if (banners != null) {
                new mdc.banner.MDCBanner(banners);
            }
        },
        checkCaps: ev => {
            let s = String.fromCharCode(ev.which);
            let el = ev.target.parentElement.nextElementSibling.nextElementSibling;
            if (el != null) {
                if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
                    console.log('CAPSLOCK is on');
                    el.classList.remove("caps-warn");
                    el.classList.add('caps-on');
                } else {
                    console.log('CAPSLOCK is off')
                    el.classList.remove("caps-on");
                    el.classList.add('caps-warn');
                }
            } else {
                console.log("Unable to locate element for CAPSLOCK")
            }
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        cas.init();
    });
})(mdc, jQuery);

function resourceLoadedSuccessfully() {

    $(document).ready(() => {

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

        $('.reveal-password').click(ev => {
            if ($('.pwd').attr('type') != 'text') {
                $('.pwd').attr('type', 'text');
                $(".reveal-password-icon").removeClass("mdi mdi-eye").addClass("mdi mdi-eye-off");
            } else {
                $('.pwd').attr('type', 'password');
                $(".reveal-password-icon").removeClass("mdi mdi-eye-off").addClass("mdi mdi-eye");
            }
            ev.preventDefault();
        });

        console.log(`JQuery Ready: ${typeof (jqueryReady)}`);
        if (typeof (jqueryReady) == 'function') {
            jqueryReady();
        }
    });

}
