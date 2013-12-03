/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branch:Branch;
        getPullRequestClass()
        getLastBuildStatus()
        forceBuild(buildAction:BuildAction)
    }

    export class BranchLineController {
        public static NAME = 'branchLineController';


        public static $inject = [
            '$scope',
            BackendService.NAME
        ];

        constructor(public $scope:IBranchScope, backendService:BackendService) {
            this.$scope.getPullRequestClass = ()=> {
                var pullRequest = this.$scope.branch.pullRequest;
                if (pullRequest && pullRequest.status) {
                    if (pullRequest.status.isMerged) {
                        return 'finished';
                    } else if (pullRequest.status.isMergeable) {
                        return 'success';
                    } else {
                        return 'failure';
                    }
                }
                else {
                    return '';
                }
            };

            this.$scope.getLastBuildStatus = ()=> {
                if (this.$scope.branch.lastBuild) {
                    return this.$scope.branch.lastBuild.status;
                }
                else {
                    return '';
                }
            };

            this.$scope.forceBuild = (buildAction:BuildAction) => {
                backendService.forceBuild(buildAction);
            };


            if (this.$scope.branch.pullRequest && !this.$scope.branch.pullRequest.status) {
                backendService.getPullRequestStatus($scope.branch.pullRequest.id).success(data=> {
                    this.$scope.branch.pullRequest.status = data;
                });
            }
        }

    }
}