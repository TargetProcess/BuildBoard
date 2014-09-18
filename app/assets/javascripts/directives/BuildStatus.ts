/// <reference path="../_all.ts" />
module buildBoard {
    export class BuildStatusDirective implements ng.IDirective {
        static NAME = "buildStatus";
        scope = {
            build: "=",
            buildActions: "=",
            branch: "=",
            buildNumber: "=",
            isActivityBuild: "=",
            type: "@"
        };
        controller = LastBuildStatusController;
        templateUrl = 'assets/partials/buildStatus.html';
        restrict = "E";
        replace = true;
    }

    export class LastBuildStatusController {
        public static $inject = ['$scope', BackendService.NAME, '$timeout'];

        constructor(private $scope:any, backendService:BackendService, $timeout:ng.ITimeoutService) {
            this.$scope.forceBuild = (buildAction:BuildAction) => {
                backendService.forceBuild(buildAction, this.$scope.buildNumber).success(buildResult=> {
                    this.$scope.showList = false;
                    if (!this.$scope.isActivityBuild) {
                        this.$scope.build = buildResult;
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
                    this.$scope.$digest()
                }, 200);
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