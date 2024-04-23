((material, $) => {
    let cas = {
        init: () => {
            cas.attachFields();
            material.autoInit();
        },
        attachFields: () => {
            new material.textField.MDCTextFieldHelperText(document.querySelectorAll('.mdc-text-field-helper-text'));
            let divs = document.querySelectorAll('.mdc-text-field'), field;
            let div;
            for (let i = 0; i < divs.length; ++i) {
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

            let tooltips = document.querySelectorAll('.mdc-tooltip');
            if (tooltips != null) {
                tooltips.forEach(t => {
                    new material.tooltip.MDCTooltip(t);
                })
            }
            let banners = document.querySelectorAll('.mdc-banner');
            if (banners != null) {
                banners.forEach(b => {
                    new material.banner.MDCBanner(b);
                })
            }
        },
        checkCaps: ev => {
            let s = String.fromCharCode(ev.which);
            if (s.toUpperCase() === s && s.toLowerCase() !== s && !ev.shiftKey) {
                for (let el of document.getElementsByClassName("caps-warn")) {
                    el.classList.remove("caps-warn");
                    el.classList.add('caps-on');
                }
            } else {
                for (let el of document.getElementsByClassName("caps-on")) {
                    el.classList.remove("caps-on");
                    el.classList.add('caps-warn');
                }
            }

        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        cas.init();
    });
})(mdc, jQuery);
