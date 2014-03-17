/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        builds: BuildInfo[];
        branch: Branch;
        changeEntityState(entity:Entity, nextState:number);
        closeView():void;
        isMergeable():boolean;
        isPossibleToMerge():boolean;
    }

    export class BranchControllerBase {
        constructor(public $scope:IBranchScope, public backendService:BackendService) {
            this.$scope.changeEntityState = (entity:Entity, nextState:number)=> {
                console.log(entity, nextState);
            };

            this.$scope.isPossibleToMerge=()=>{
                return !!this.$scope.branch.pullRequest;
            };

            this.$scope.isMergeable = ()=>{
                var pullRequest = this.$scope.branch.pullRequest;
                if (!pullRequest){
                    return false;
                }

                var prStatus = this.$scope.branch.pullRequest.status;
                if (!prStatus){
                    return false;
                }

                if (!prStatus.isMergeable || prStatus.isMerged) {
                    return false;
                }

                var entityStatus = this.$scope.branch.entity.state;
                if (entityStatus.name != 'Tested'){
                    return false;
                }



                return true;
            };
        }

        public loadPullRequestStatus(branch:Branch) {
            if (branch.pullRequest && !branch.pullRequest.status) {
                this.backendService.pullRequestStatus(branch.pullRequest.prId).success(data=> {
                    branch.pullRequest.status = data;
                })
            }
        }
    }
}