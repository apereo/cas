(function () {

}());

$(function () {

    var initializeCasSearchEngine = function (engine) {
        $.getJSON("/properties.json", function (data) {
            engine.clear();
            $.each(data.dependencies, function (idx, item) {
                if (item.weight === undefined) {
                    item.weight = 0;
                }
            });
            engine.add(data.dependencies);
        });
    };


    var maxSuggestions = 10;
    var propertiesContainer = new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.nonword('name', 'description', 'keywords', 'group'),
        queryTokenizer: Bloodhound.tokenizers.nonword,
        identify: function (obj) {
            return obj.id;
        },
        sorter: function (a, b) {
            return b.weight - a.weight;
        },
        limit: maxSuggestions,
        cache: false
    });

    initializeCasSearchEngine(propertiesContainer);

    $('#properties').typeahead(
        {
            minLength: 2,
            autoSelect: true,
            highlight: true
        }, {
            name: 'propertiesContainer',
            display: 'name',
            source: propertiesContainer,
            templates: {
                suggestion: function (data) {
                    return "<div><strong>" + data.name + "</strong><br/><small>" + data.description + "</small></div>";
                },
                footer: function (search) {
                    if (search.suggestions && search.suggestions.length == maxSuggestions) {
                        return "<div class=\"tt-footer\">More matches, please refine your search</div>";
                    }
                    else {
                        return "";
                    }
                }
            }
        });

    $('#properties').bind('typeahead:select', function (ev, suggestion) {
        var alreadySelected = $("#propertiesContainer input[value='" + suggestion.id + "']").prop('checked');
        if (alreadySelected) {
            //removeTag(suggestion.id);
            //$("#dependencies input[value='" + suggestion.id + "']").prop('checked', false);
        }
        else {
            //addTag(suggestion.id, suggestion.name);
            //$("#dependencies input[value='" + suggestion.id + "']").prop('checked', true);
        }
        $('#properties').typeahead('val', '');
    });

    $("#propertiesContainer").on("click", "button", function () {
        var id = $(this).parent().attr("data-id");
        //$("#dependencies input[value='" + id + "']").prop('checked', false);
        //removeTag(id);
    });

    // $("#dependencies input").bind("change", function () {
    //     var value = $(this).val()
    //     if ($(this).prop('checked')) {
    //         var results = starters.get(value);
    //         //addTag(results[0].id, results[0].name);
    //     } else {
    //         //removeTag(value);
    //     }
    // });

    // Mousetrap.bind(['command+enter', 'alt+enter'], function (e) {
    //     //$("#form").submit();
    //     return false;
    // });

    // var autocompleteTrap = new Mousetrap($("#autocomplete").get(0));
    // autocompleteTrap.bind(['command+enter', 'alt+enter'], function (e) {
    //     //$("#form").submit();
    //     return false;
    // });

    // autocompleteTrap.bind("enter", function (e) {
    //     if (e.preventDefault) {
    //         e.preventDefault();
    //     } else {
    //         e.returnValue = false;
    //     }
    // });

});
