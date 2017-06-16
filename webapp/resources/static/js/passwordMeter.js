/* global jqueryReady, policyPattern, zxcvbn */
/*eslint-disable no-unused-vars*/
function jqueryReady() {
    var strength = {
        0: 'Worst ☹',
        1: 'Bad ☹',
        2: 'Weak ☹',
        3: 'Good ☺',
        4: 'Strong ☻'
    };

    var policyPatternRegex = new RegExp(policyPattern);
    var password = document.getElementById('password');
    var confirmed = document.getElementById('confirmedPassword');
    var meter = document.getElementById('password-strength-meter');
    
    password.addEventListener('input', validate);
    confirmed.addEventListener('input', validate);

    function validate() {
        var val = password.value;
        var cnf = confirmed.value;
        var responseText;

        var disableSubmit = val == '' || cnf == '' || val != cnf || !policyPatternRegex.test(val) || !policyPatternRegex.test(cnf);
        $('#submit').prop('disabled', disableSubmit);
        
        if (disableSubmit) {
            $('#password-strength-text').show();
            responseText = '<div class=\'alert alert-danger\' role=\'alert\'>' +
                '<span class=\'glyphicon glyphicon-exclamation-sign\' aria-hidden=\'true\'></span>' +
                '<strong>Password does not match the password policy requirement.</strong></div>';
            $('#password-strength-text').html(responseText);
            return;
        }
        var result = zxcvbn(val);

        // Update the password strength meter
        meter.value = result.score;

        // Update the text indicator
        if (val !== '') {
            $('#password-strength-text').show();
            responseText = 'Strength: <strong>' + strength[result.score] + '</strong>'
                + '<span class=\'feedback\'>' + result.feedback.warning + ' ' + result.feedback.suggestions + '</span>';
            $('#password-strength-text').html(responseText);
        } else {
            $('#password-strength-text').hide();
        }
    }
}
