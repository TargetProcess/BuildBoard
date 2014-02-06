/// <reference path='../_all.ts' />
module buildBoard {

    export interface IMap<T>
    {
        [name:string]:T
    }

    export class BackendService {
        public static NAME = "backendService";

        public static $inject = [
            '$window',
            '$http'
        ];

        controllers:Controllers;

        constructor(private $window:IBuildBoardWindow, private $http:ng.IHttpService) {
            this.controllers = $window.jsRoutes.controllers;
        }

        branch(id:string):ng.IHttpPromise<Branch> {
            var url = this.controllers.Application.branch(id).absoluteURL();
            return this.$http.get(url);
        }

        branches():ng.IHttpPromise<Branch[]> {
            return this.$http.get(this.controllers.Application.branches().absoluteURL());
        }

        builds(branchId:string):ng.IHttpPromise<Build[]> {
            return this.$http.get(this.controllers.Jenkins.builds(branchId).absoluteURL());
        }

        lastBuilds():ng.IHttpPromise<IMap<Build>> {
            return this.$http.get(this.controllers.Jenkins.lastBuildInfos().absoluteURL());
        }

        forceBuild(buildAction:BuildAction):ng.IHttpPromise<Build> {
            return this.$http.get(this.controllers.Jenkins.forceBuild(buildAction.pullRequestId, buildAction.branchId, buildAction.fullCycle).absoluteURL());
        }

        toggleBuild(branchId:string, buildNumber: number):ng.IHttpPromise<Build> {
            return this.$http.get(this.controllers.Jenkins.toggleBuild(branchId, buildNumber).absoluteURL());
        }

        pullRequestStatus(pullRequest:number):ng.IHttpPromise<PRStatus> {
            return this.$http.get(this.controllers.Github.pullRequestStatus(pullRequest).absoluteURL());
        }

        changeEntityState(entityId:number, nextStateId:number):ng.IHttpPromise<EntityState> {
            return this.$http.post(this.controllers.Targetprocess.changeEntityState(entityId,nextStateId).absoluteURL(), {});
        }

        run(branch: string, build: number, part: string, run: string): ng.IHttpPromise<BuildNode> {
            return this.$http.get(this.controllers.Jenkins.run(branch, build, part, run).absoluteURL())
        }

        testCase(branch: string, build: number, part: string, run: string, test: string): ng.IHttpPromise<TestCase> {
            return this.$http.get(this.controllers.Jenkins.testCase(branch, build, part, run, test).absoluteURL())
        }
    }

}