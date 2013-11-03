/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export class PullRequestController {
        public static $inject = [
            '$scope',
            '$http',
            '$window'
        ];

        constructor(public $scope:IPullRequestScope, $http:ng.IHttpService, $window:IBuildBoardWindow) {
            this.$scope.getClass = function (prStatus) {
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

            if (this.$scope.branch.pullRequest) {
                $http.get($window.jsRoutes.controllers.Github.pullRequestStatus($scope.branch.pullRequest.id).absoluteURL()).success((data:PRStatus)=> {
                    this.$scope.pullRequestStatus = data;
                });
            }
        }

    }
}