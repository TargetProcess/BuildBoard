/// <reference path="../_all.ts" />
module buildBoard {
    export class BuildStatusDirective implements ng.IDirective {
        static NAME = "buildStatus";
        scope = {
            build: "=",
            branch: "=",
            type: "@",
            statusType: "@"
        };
        controller = BuildStatusController;
        templateUrl = 'assets/partials/buildStatus.html';
        restrict = "E";
        replace = true;
    }


    export interface IBuildStatusScope extends ng.IScope {
        build:Build;
        branch:Branch;
        type:string;
        statusType:string;
        showTimestamp:boolean;
        showList:boolean;
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

    export class BuildStatusController {
        public static $inject = ['$scope', '$element', BackendService.NAME, '$timeout'];

        constructor(private $scope:IBuildStatusScope, private $element:JQuery, backendService:BackendService, $timeout:ng.ITimeoutService) {

            var ifPartFailed = function (buildNode:BuildNode, runName:String, part:String) {
                if (buildNode.runName == runName && buildNode.name == part) {
                    var status = StatusHelper.parseBuildNode(buildNode);
                    return status != Status.Success;
                }
                return _.any(buildNode.children, child=>ifPartFailed(child, runName, part));
            };

            this.$scope.showTimestamp = this.$scope.statusType == "branch";

            this.$scope.toggle = () => {
                if (!this.$scope.showList) {
                    this.$scope.showList = true;
                    this.$scope.pending = true;

                    var buildNumber =
                        this.$scope.statusType == "branch" ? null : this.$scope.build.number;

                    backendService.getBuildActions(this.$scope.branch.name, buildNumber).then(data=> {
                        this.$scope.buildActions = data.data;

                        if (this.$scope.build) {
                            var forceCustoms = _.filter(this.$scope.buildActions, c=>c.cycleName == 'Custom');
                            _.each(forceCustoms, buildAction=> {
                                var partitioned = _.filter(buildAction.buildParametersCategories, category=>!_.isUndefined(category.runName));
                                _.each(partitioned, category=> {
                                    category.selectedParts = category.parts.filter(part=> {
                                        return ifPartFailed(this.$scope.build.node, category.runName, part);
                                    });
                                });
                            });


                        }


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
                    if (build.message !== 'Ok') {
                        this.$scope.build = build;
                    } else {
                        alert('build deploy')
                    }
                });
            };

            var timeoutId:ng.IPromise<any> = null;

            $element[0].addEventListener('focus', ()=> {
                $timeout.cancel(timeoutId);
            }, true);
            $element[0].addEventListener('blur', ()=> {
                timeoutId = $timeout(() => {
                    this.$scope.showList = false;
                    this.$scope.buildActions = [];
                    this.$scope.$digest();
                }, 50);
            }, true);

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