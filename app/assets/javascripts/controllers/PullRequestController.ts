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
            this.$scope.getClass = (prStatus:PRStatus)=> {
                return prStatus ? prStatus.isMerged ? 'btn-primary' : prStatus.isMergeable ? 'btn-success' : 'btn-danger' : '';
            };

            if (this.$scope.branch.pullRequest) {
                $http.get($window.jsRoutes.controllers.Github.pullRequestStatus($scope.branch.pullRequest.id).absoluteURL()).success((data:PRStatus)=> {
                    this.$scope.pullRequestStatus = data;
                });
            }
        }

    }
}