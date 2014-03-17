/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        getBranch(): Branch;
        changeEntityState(entity:Entity, nextState:number);
        closeView():void;
        isMergeable():boolean;
        isPossibleToMerge():boolean;
    }

    export class BranchControllerBase {
        constructor(public $scope:IBranchScope, public backendService:BackendService, private modelProvider:ModelProvider) {
            this.$scope.getBranch = () => modelProvider.findBranch(this.$scope.branchName);

            this.$scope.changeEntityState = (entity:Entity, nextState:number)=> {
                console.log(entity, nextState);
            };

            this.$scope.isPossibleToMerge=()=>{
                return this.$scope.getBranch() && !!this.$scope.getBranch().pullRequest;
            };

            this.$scope.isMergeable = ()=>{
                if (!this.$scope.getBranch())
                    return false;

                var pullRequest = this.$scope.getBranch().pullRequest;
                if (!pullRequest){
                    return false;
                }

                var prStatus = this.$scope.getBranch().pullRequest.status;
                if (!prStatus){
                    return false;
                }

                if (!prStatus.isMergeable || prStatus.isMerged) {
                    return false;
                }

                if (this.$scope.getBranch().entity) {
                    var entityStatus = this.$scope.getBranch().entity.state;
                    if (entityStatus.name != 'Tested') {
                        return false;
                    }
                }



                return true;
            };
        }

        public loadPullRequestStatus(branchName:string) {
            if (branch && branch.pullRequest && !branch.pullRequest.status) {
                this.backendService.pullRequestStatus(branch.pullRequest.prId).success(data=> {
                    branch.pullRequest.status = data;
                })
            }
        }
    }
}