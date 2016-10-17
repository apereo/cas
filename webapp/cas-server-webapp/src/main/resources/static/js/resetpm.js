function jqueryReady() {
    $("input[type=text]").input(validate());
    function validate() {
        var val = this.value;
        var disableSubmit = val == "";
        $('#submit').prop("disabled", disableSubmit)
    }
}
