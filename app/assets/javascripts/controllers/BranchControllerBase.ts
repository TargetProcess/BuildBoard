/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        builds: Build[];
        branch: Branch;
        changeEntityState(entity:Entity, nextState:number);
        closeView():void;
        getArtifact(file:BuildNode);
    }

    export class BranchControllerBase {
        constructor(public $scope:IBranchScope, public backendService:BackendService) {
            this.$scope.changeEntityState = (entity:Entity, nextState:number)=> {
                console.log(entity, nextState);
            };
        }

        public loadPullRequestStatus(branch:Branch) {
            if (branch.pullRequest && !branch.pullRequest.status) {
                this.backendService.getPullRequestStatus(branch.pullRequest.prId).success(data=> {
                    branch.pullRequest.status = data;
                })
            }
        }
    }
}