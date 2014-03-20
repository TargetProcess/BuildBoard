/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchScope extends ng.IScope {
        branchName:string;
        getBranch(): Branch;
        changeEntityState(entity:Entity, nextState:number);
        closeView():void;
        mergeStatus():MergeStatus;
        isPossibleToMerge():boolean;
        merge():void;
    }

    export interface MergeStatus{
        isEnabled:boolean;
        reasons:string[];
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

            this.$scope.mergeStatus = ()=>{
                var branch = this.$scope.getBranch();
                if (!branch) {
                    return null;
                }
                var lastDevelop = modelProvider.getLastBuild('develop');
                var prevDevelop = modelProvider.getPrevBuild('develop');

                if (!lastDevelop || !prevDevelop){
                    return null;
                }

                 var mergeStatus:MergeStatus = {
                    isEnabled:false,
                    reasons:[]
                };

                var status = (lastDevelop.parsedStatus == Status.InProgress || lastDevelop.parsedStatus == Status.Unknown) ? prevDevelop.parsedStatus : lastDevelop.parsedStatus;


                if (status !== Status.Success && status !== Status.Toggled){
                    mergeStatus.reasons.push("Develop is red");
                }




                var pullRequest = branch.pullRequest;
                if (pullRequest) {
                    if (!pullRequest.status.isMergeable) {
                        mergeStatus.reasons.push("There are conflicts in pull request");
                    }
                } else {
                    mergeStatus.reasons.push("There is no pull request")
                }

                if (branch.entity) {
                    var entityStatus = branch.entity.state;
                    if (entityStatus.name != 'Tested') {
                        mergeStatus.reasons.push("The "+ branch.entity.entityType + " is not in Tested state")
                    }
                }

                if (branch.lastBuild) {
                    if (branch.lastBuild.parsedStatus !== Status.Toggled && branch.lastBuild.parsedStatus !== Status.Success){
                        mergeStatus.reasons.push("The last build on branch was not success");
                    }
                } else {
                    mergeStatus.reasons.push("There was no build on branch");
                }

                mergeStatus.isEnabled = mergeStatus.reasons.length === 0;

                return mergeStatus;
            };


            this.$scope.merge = ()=>{

                var branch = this.$scope.getBranch();
                if (!branch) {
                    return null;
                }

                this.backendService.merge(branch.name).success(
                    result=>{
                        if (result.nextState){
                            branch.entity.state = result.nextState
                        }
                        if (result.isMerged){
                            branch.pullRequest = null;
                        }
                        this.$scope.$apply();

                        alert(result.message);
                    }
                )
                .error(error=>{
                        alert(error.message);
                        console.log(error);
                    })
            }
        }
    }
}