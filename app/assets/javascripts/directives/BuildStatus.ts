/// <reference path="../_all.ts" />
module buildBoard {
    export class BuildStatusDirective implements ng.IDirective {
        static NAME = "buildStatus";
        scope = {
            build: "=",
            branch: "=",
            type: "@",
            statusType:"@"
        };
        controller = LastBuildStatusController;
        templateUrl = 'assets/partials/buildStatus.html';
        restrict = "E";
        replace = true;
    }


    export interface IBuildStatusScope extends ng.IScope {
        build:Build;
        branch:Branch;
        type:string;
        statusType: string;
        showTimestamp:boolean;
        showList: boolean;
        execute(buildAction:BuildAction);
        toggleParameters(buildAction:BuildAction);
        clearTimeoutOnFocus();
        hideOnBlur();
        buildActions:BuildAction[]
        getBuildStatus(build:Build):Status;
        toggleBuild(build:Build);
        toggle();
        pending:boolean;
    }

    export class LastBuildStatusController {
        public static $inject = ['$scope', BackendService.NAME, '$timeout'];

        constructor(private $scope:IBuildStatusScope, backendService:BackendService, $timeout:ng.ITimeoutService) {

            this.$scope.showTimestamp = this.$scope.statusType == "branch";

            this.$scope.toggle = () => {
                if (!this.$scope.showList) {
                    this.$scope.showList = true;
                    this.$scope.pending = true;

                    var buildNumber =
                        this.$scope.statusType=="branch"? null : this.$scope.build.number;

                    backendService.getBuildActions(this.$scope.branch.name, buildNumber).then(data=> {
                        this.$scope.buildActions = data.data;
                        this.$scope.pending = false;
                    });


                }
                else {
                    this.$scope.showList = false;
                    this.$scope.buildActions = [];
                }


            };


            this.$scope.execute = (buildAction:BuildAction) => {
                if (this.$scope.statusType == 'build') {
                    buildAction.buildNumber = this.$scope.build.number;
                }

                backendService[buildAction.action](buildAction).success(build => {
                    this.$scope.showList = false;
                    if(build.message !== 'Ok') {
                        this.$scope.build = build;
                    } else {
                        alert('build deploy')
                    }
                });
            };

            var timeoutId:ng.IPromise<any> = null;


            this.$scope.clearTimeoutOnFocus = () => {
                $timeout.cancel(timeoutId);
            };
            this.$scope.hideOnBlur = () => {
                timeoutId = $timeout(() => {
                    this.$scope.showList = false;
                    this.$scope.buildActions = [];
                    this.$scope.$digest()
                }, 0);
            };

            this.$scope.toggleParameters = (buildAction:BuildAction) => {
                var currentState = buildAction.showParameters;
                this.$scope.buildActions.forEach(function (buildAction:BuildAction) {
                    buildAction.showParameters = false;
                });
                buildAction.showParameters = !currentState;
            };
            this.$scope.getBuildStatus = StatusHelper.parse;
            this.$scope.toggleBuild = (build:Build)=> {
                var branch = this.$scope.branch;
                var toggled = !build.toggled;
                backendService.toggleBuild(branch.name, build.number, toggled).success(b=> {
                    build.toggled = toggled;
                    if (build.number == branch.lastBuild.number) {
                        branch.lastBuild.toggled = build.toggled;
                        branch.lastBuild.parsedStatus = StatusHelper.parseInfo(branch.lastBuild.status, build.toggled);
                    }
                });
            }
        }
    }
}