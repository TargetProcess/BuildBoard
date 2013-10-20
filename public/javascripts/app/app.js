angular.module('BuildBoard', ['ui.bootstrap', 'angular-underscore','phonecatFilters']);

var BranchesController = function ($scope, $http, $window) {

    var filterBranchesById = function(branches,id) {
        return _.filter(branches,function (branch) {
            return branch.entity && _.any(branch.entity.assignmentsOpt, function (assignment) {
                return assignment.userId == id;
            });
        });
    };
    var filterBranchesByEntity = function(branches) {
        return _.filter(branches,function (branch) {
            return branch.entity;
        });
    };
    $scope.predicate = 'state';
    $scope.isShowingAll = true;
    $http.get($window.jsRoutes.controllers.Application.branches().absoluteURL()).success(function (data) {
        $scope.allBranches = data;
        $scope.entityBranches = filterBranchesByEntity(data);
        $scope.entityBranchesLength = $scope.entityBranches.length;
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

        $scope.branchCount = function (id) {
            return filterBranchesById($scope.allBranches,id).length;
        }

    });
    $scope.filterBranch = function(id) {
        $scope.isShowingAll = false;
        $scope.isShowingEntity = false;
        $scope.isShowingId = id;
        $scope.branches = filterBranchesById($scope.allBranches,id);
    };
    $scope.resetFilterBranch = function() {
        $scope.isShowingId = null;
        $scope.isShowingAll = true;
        $scope.isShowingEntity = false;
        $scope.branches = $scope.allBranches;
    };
    $scope.filterOnlyEntityBranch = function() {
        $scope.isShowingId = null;
        $scope.isShowingAll = false;
        $scope.isShowingEntity = true;
        $scope.branches = $scope.entityBranches;
    }

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
