angular.module('BuildBoard', ['ui.bootstrap']);

var BranchesController = function ($scope, $http, $window) {

    $scope.predicate = 'state';
    $http.get($window.jsRoutes.controllers.Application.branches().absoluteURL()).success(function (data) {
        $scope.branches = data;
    });
};


var PullRequestController = function ($scope, $http, $window) {
    $scope.getClass = function (prStatus) {
        if (!prStatus){
          return '';
        } else if (prStatus.isMerged) {
            return 'btn-primary';
        } else if (prStatus.isMergeable) {
            return 'btn-success';
        } else {
            return 'btn-danger';
        }
    };

    if ($scope.branch.pullRequest) {
        $http.get($window.jsRoutes.controllers.Github.pullRequestStatus($scope.branch.pullRequest.id).absoluteURL()).success(function (data) {
            $scope.pullRequestStatus = data;
        });
    }
};
