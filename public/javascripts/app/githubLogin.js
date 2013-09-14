require(['jquery'], function($){
    var $login = $('.login-to-github');
    $login.click(function(){
        window.open('https://github.com/login/oauth/authorize?client_id=@GitHubApplication.clientId&scope=repo&redirect_uri=@routes.Login.oauth().absoluteURL(false)', '', 'popup');
    });
});