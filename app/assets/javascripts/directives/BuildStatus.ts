/// <reference path="../_all.ts" />
module buildBoard {
    export class BuildStatusDirective implements ng.IDirective {
        static NAME = "buildStatus";
        scope = {
            build: "=",
            buildActions: "=",
            branch: "=",
            type: "@"
        };
        controller = LastBuildStatusController;
        templateUrl = 'assets/partials/buildStatus.html';
        restrict = "E";
        replace = true;
    }

    export class LastBuildStatusController {
        public static $inject = ['$scope', BackendService.NAME];

        constructor(private $scope:any, backendService:BackendService) {

            this.$scope.forceBuild = (buildAction:BuildAction) => {
                backendService.forceBuild(buildAction).success(build=> {
                    this.$scope.build = build;
                });
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