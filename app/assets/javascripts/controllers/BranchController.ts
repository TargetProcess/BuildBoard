/// <reference path='../_all.ts' />
module buildBoard {
    'use strict';

    export interface IBranchDetailsScope extends IBranchScope {
        loadBuild(buildInfo:Build):void;
        getActivity():ActivityEntry[];
        getBranch():Branch;
        getBuildStatus(build:Build):Status;
        getBuildTime(build:Build):number;
        getGravatar(email:string):string;
        processCommitMessage(message:string):string;
        getSuccessCount(buildNodes:BuildNode[]):number;

        isCollapsible(node:BuildNode):boolean
    }

    export class BranchController {
        public static $inject = [
            '$scope',
            '$state',
            ModelProvider.NAME,
            BackendService.NAME
        ];

        constructor(private $scope:IBranchDetailsScope, $state:ng.ui.IStateService, modelProvider:ModelProvider, backendService:BackendService) {
            this.$scope.isCollapsible = node=> StatusHelper.parseBuildNode(node) == Status.Success && node.children.length == 0;

            this.$scope.getSuccessCount = nodes=> {
                return _.filter(nodes, node=>StatusHelper.parseBuildNode(node) == Status.Success).length;
            };

            this.$scope.branchName = $state.params['name'];
            this.$scope.closeView = ()=> $state.go("list");
            this.$scope.getBuildStatus = StatusHelper.parse;
            this.$scope.getBuildTime = build=> {
                var status = StatusHelper.parse(build);
                var endTime:number = undefined;
                var result;

                if (status == Status.InProgress || build.timestampEnd === 0) {
                    endTime = new Date().getTime()
                }
                else if (_.isUndefined(build.timestampEnd)) {
                    endTime = undefined;
                }
                else {
                    endTime = build.timestampEnd;
                }
                if (_.isUndefined(endTime)) {
                    result = -1;
                }
                else {
                    result = endTime - build.timestamp;
                }
                if (result < 0) {
                    return undefined;
                }
                else {
                    return result / 1000;
                }

            };

            this.$scope.getGravatar = _.memoize((email:string)=> email ? md5(email.toLowerCase().trim()) : '0');
            this.$scope.processCommitMessage = subject=>subject &&
            subject.replace(/Merge pull request #(\d+) from (.*)/g, 'Merge pull request <a href="https://github.com/TargetProcess/TP/pull/$1" target="_blank">#$1</a> from <span>$2</span>');


            modelProvider.getBranchWithActivities(this.$scope.branchName)
                .then(branch => {
                    this.$scope.getBranch = ()=> branch;
                    this.$scope.getActivity = ()=> branch.activity;
                });


            var defer:ng.IHttpPromise<Build> = null;
            $scope.loadBuild = buildInfo => {
                if (buildInfo.node == null && !defer) {
                    defer = backendService.build(this.$scope.getBranch().name, buildInfo.number).success(build => {
                        buildInfo.node = build.node;
                        defer = null;
                    });
                }
            };
        }
    }
}