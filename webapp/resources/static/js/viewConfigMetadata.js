$('.ui.search').search({
    type: 'message',
    searchDelay: 500,
    silent: true,
    apiSettings: {
        silent: true,
        cache: false,
        url: urls.search,
        onResponse: function (casResp) {
            var response = {
                results: {}
            };

            for (var key in casResp) {
                if (casResp.hasOwnProperty(key)) {
                    var prop = casResp[key];
                    var group = prop.group;

                    if (response.results[group] === undefined) {
                        response.results[group] = {
                            name: group,
                            results: []
                        };
                    }
                    response.results[group].results.push({
                        title: prop.id,
                        description: prop.description,
                        defaultValue: prop.defaultValue,
                        type: prop.type,
                        deprecated: prop.deprecated,
                        requiredProperty: prop.requiredProperty
                    });
                }
            }

            return response;
        },
    },
    minCharacters: 3,
    templates: {
        message: function (response, type) {
            var html = '';
            $( '#accordion' ).empty();

            if (type === 'empty') {
                html += '<h3><i class=\'fa fa-search\' />&nbsp;<strong>No Results</strong></h3>';
                html += '<div class=\'alert alert-warning\'>';
                html += '<i class=\'fa fa-exclamation-circle\'/>&nbsp;';
                html += 'No search results could be found based on the provided query.';
                html += '</div>';
            }
            for (var group in response.results) {

                html += '<h2><i class=\'fa fa-users\' />&nbsp;<strong>Group: </strong>' + group + '</h2>';
                var props = response.results[group].results;

                html += '<div>';
                for (var i = 0; i < props.length; i++) {
                    html += '<p>';
                    var prop = props[i];
                    html += '<i class=\'fa fa-cogs\'/>&nbsp;';
                    html += 'Property: <code>' + prop.title + '=' + prop.defaultValue + '</code><br/>';
                    if (prop.deprecated) {
                        html += '<p/><div class=\'alert alert-warning\'>';
                        html += '<i class=\'fa fa-exclamation-circle\'/>&nbsp;';
                        html += 'This property is deprecated and will be removed in future CAS versions.';
                        html += '</div>';
                    }
                    if (prop.requiredProperty) {
                        html += '<p/><div class=\'alert alert-success\'>';
                        html += '<i class=\'fa fa-check-square\'/>&nbsp;';
                        html += 'This presence of this property is required.';
                        html += '</div>';
                    }

                    html += '<i class=\'fa fa-codepen\' />&nbsp;Type: <code>' + prop.type + '</code><br/>';
                    if (prop.description != null) {
                        html += '<p>' + prop.description + '</p>';
                    }
                    html += '</p><br/>';
                }
                html += '</div>';
            }

            $( '#accordion' ).html(html);
            $( '#accordion' ).accordion({
                heightStyle: 'content'
            });
            $( '#accordion' ).accordion('refresh');
            return '';
        }
    }
});
