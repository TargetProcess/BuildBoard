define(['jquery', 'underscore'], function ($, _) {
    return {
        parseHash: function (hash, defaults) {

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

        },

        buildHash: function (currentValues) {
            var keyRegex = /^#([^=]+)=/;
            return _(currentValues).chain()
                .pairs()
                .map(function (pair) {
                    return pair[0] + '=' + pair[1];
                })
                .value()
                .join('&');
        },

        getHashKey: function (a) {
            var keyRegex = /^#([^=]+)=/;
            var href = a.hash;
            var match = keyRegex.exec(href);
            return (match != null) ? match[1] : '';
        },
        start: function (defaults) {
            var getHashKey = this.getHashKey;
            var buildHash = this.buildHash;

            var currentValues = this.parseHash(window.location.hash, defaults);
            window.location.hash = '#' + buildHash(currentValues);

            var $navigation = $('a.navigation');


            var keys = _($navigation)
                .chain()
                .map(getHashKey)
                .filter(function (key) {
                    return key !== ''
                })
                .unique()
                .value();



            $navigation.each(function () {
                var href = this.hash.substr(1);
                var key = getHashKey(this);

                var values = _.clone(currentValues);
                delete values[key];
                var hash = buildHash(values);
                this.hash += '&'+hash;
            })
        }
    };
});
/*function navigate(key, value) {

 }

 $(function () {
 var hashR = /(.*)\/(.*)/g;

 function parse(hash) {
 var m = hashR.exec(hash);

 var user = (m != null && m[1]) || 'all';
 var branches = (m != null && m[2]) || 'all';
 return {
 user: user,
 branches: branches
 };

 }

 /*
 $(window).bind('hashchange', function () {
 var hash = window.location.hash;
 var location = parse(hash);



 $ ( '.role-navigation' ).find ( 'li' ).each ( function ( ) {
 var $li = $ ( this ) ;
 var href = $li.find ( 'a' ).attr ( 'href' ) ;
 if ( href == '@{request.path}') {
 $li.addClass ( 'active' ) ;
 }
 }
 );

 });  */