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
                var branch = this.$scope.getBranch();
                if (!branch)
                    return false;

                var pullRequest = branch.pullRequest;
                if (!pullRequest){
                    return false;
                }

                var prStatus = branch.pullRequest.status;
                if (!prStatus){
                    return false;
                }

                if (!prStatus.isMergeable || prStatus.isMerged) {
                    return false;
                }

                if (branch.entity) {
                    var entityStatus = branch.entity.state;
                    if (entityStatus.name != 'Tested') {
                        return false;
                    }
                }



                return true;
            };
        }
    }
}