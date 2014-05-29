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

    class Toggle implements ToggleInfo {
        user:buildBoard.User;
        timestamp:Date;
    }

    export class LastBuildStatusController {
        public static $inject = ['$scope', BackendService.NAME, LoggedUserService.NAME];

        constructor(private $scope:any, backendService:BackendService, loggedUser:LoggedUserService) {

            this.$scope.forceBuild = (buildAction:BuildAction) => {
                backendService.forceBuild(buildAction).success(build=> {
                    this.$scope.build = build;
                });
            };

            this.$scope.getBuildStatus = StatusHelper.parse;

            this.$scope.toggleBuild = (build:Build)=> {
                var branch = this.$scope.branch;
                var toggled = build.toggle ? false : true;
                backendService.toggleBuild(branch.name, build.number, toggled).success(b=> {

                    var toggle = new Toggle();
                    toggle.user = loggedUser.getLoggedUser();
                    toggle.timestamp = new Date();

                    build.toggle = toggled ? toggle:null;
                    if (build.number == branch.lastBuild.number) {
                        branch.lastBuild.toggle = build.toggle;
                        branch.lastBuild.parsedStatus = StatusHelper.parseInfo(branch.lastBuild.status, build.toggle!=null);
                    }
                });
            }
        }
    }
}