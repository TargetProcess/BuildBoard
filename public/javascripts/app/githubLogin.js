require(['jquery','app/globals'], function($, globals){
    var $login = $('.login-to-github');
    $login.click(function(){
        window.open('https://github.com/login/oauth/authorize?client_id='+globals.github.clientId+'&scope=repo&redirect_uri='+globals.login.oauthRedirect, '', 'popup');
    });
});