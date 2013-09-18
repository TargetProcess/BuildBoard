define(['jquery', 'underscore', 'libs/jquery.ba-hashchange.min'], function ($, _) {
    var parseHash = function (hash, defaults) {

        var match,
            pl = /\+/g,  // Regex for replacing addition symbol with a space
            search = /([^&=]+)=?([^&]*)/g,
            decode = function (s) {
                return decodeURIComponent(s.replace(pl, " "));
            },
            query = hash.substring(1);

        var result = {};
        while (match = search.exec(query))
            result[decode(match[1])] = decode(match[2]);

        return _.defaults(result, defaults);

    };


    var buildHash = function (currentValues) {
        return _(currentValues).chain()
            .pairs()
            .map(function (pair) {
                return pair[0] + '=' + pair[1];
            })
            .value()
            .join('&');
    };

    $.fn.navigation = function (config) {

        config = _.defaults(config, {
            activeClass: 'active',
            activeElementSelector: function ($a) {
                return $a;
            },
            callback: $.noop
        });

        var $nav = this;

        $(window).hashchange(function () {
            var currentValues = parseHash(window.location.hash, config.defaults);

            $nav.each(function () {
                var $a = $(this);
                var keyValue = $a.data('href').split('=');
                var key = keyValue[0];
                var value = keyValue[1];

                var values = _.clone(currentValues);

                config.activeElementSelector($a).toggleClass(config.activeClass, values[key] == value);

                values[key] = value;
                var hash = buildHash(values);
                $a.attr('href', '#' + hash);
                if (currentValues[key] == value) {
                    config.callback(key, value, values, currentValues);
                }
            });
        });

        return this;
    };

    return $;

});