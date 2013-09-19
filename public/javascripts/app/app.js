angular.module('BuildBoard', ['ui.bootstrap']);

var BranchesController = function($scope, $http){

    $http.get(jsRoutes.controllers.Application.branches().absoluteURL()).success(function(data){
        $scope.branches = data;
    });

};

