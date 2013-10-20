angular.module('BuildBoard', ['ui.bootstrap', 'angular-underscore']);

var BranchesController = function ($scope, $http, $window) {

    $scope.predicate = 'state';
    $http.get($window.jsRoutes.controllers.Application.branches().absoluteURL()).success(function (data) {
        $scope.branches = data;
        $scope.users = _.chain(data)
            .filter(function (branch) {
                return !!branch.entity;
            })
            .map(function (branch) {
                return branch.entity.assignmentsOpt;
            })
            .flatten()
            .unique(function (user) {
                return user.userId;
            })
            .value();

        $scope.branchCount = function () {
            //todo move to service or resource
            var id = $window.currentUser.id;
            return _.filter($scope.branches,function (branch) {
                return branch.entity && _.any(branch.entity.assignmentsOpt, function (assignment) {
                    return assignment.userId == id;
                });
            }).length;
        }

    });

};


var PullRequestController = function ($scope, $http, $window) {
    $scope.getClass = function (prStatus) {
        if (!prStatus) {
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
