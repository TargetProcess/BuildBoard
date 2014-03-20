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
            'httpNotifiable'
        ];

        controllers:Controllers;

        constructor(private $window:IBuildBoardWindow, private $http:HttpServiceNotificationDecorator) {
            this.controllers = $window.jsRoutes.controllers;
        }

        branches():ng.IHttpPromise<Branch[]> {
            return this.$http.get(this.controllers.Application.branches().absoluteURL());
        }

        build(branchId:string, buildNumber: number):ng.IHttpPromise<Build> {
            return this.$http.get(this.controllers.Jenkins.build(branchId, buildNumber).absoluteURL());
        }

        forceBuild(buildAction:BuildAction):ng.IHttpPromise<Build> {
            return this.$http.post(this.controllers.Jenkins.forceBuild(buildAction.pullRequestId, buildAction.branchId, buildAction.cycleName).absoluteURL(), {});
        }

        toggleBuild(branchId:string, buildNumber: number):ng.IHttpPromise<Build> {
            return this.$http.post(this.controllers.Jenkins.toggleBuild(branchId, buildNumber).absoluteURL(), {});
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

        merge(branch: string): ng.IHttpPromise<MergeButtonResult> {
            return this.$http.get(this.controllers.Github.merge(branch).absoluteURL())
        }
    }

}