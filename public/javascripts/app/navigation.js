define(['jquery','underscore'], function($,_){
   return {
     start:function(){
         alert('start');
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