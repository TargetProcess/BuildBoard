/// <reference path='../_all.ts' />
module buildBoard {

    export interface IMap<T> {
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
            return this.$http.get(this.controllers.Branches.branches().absoluteURL());
        }

        build(branchId:string, buildNumber:number):ng.IHttpPromise<Build> {
            return this.$http.get(this.controllers.Jenkins.build(branchId, buildNumber).absoluteURL());
        }

        forceBuild(buildAction:BuildAction):ng.IHttpPromise<Build> {
            var categories:BuildParametersCategory[] = _.chain(buildAction.buildParametersCategories).map((x:BuildParametersCategory) => {
                    return {
                        name: x.name,
                        parts: x.selectedParts == null ? [] : x.selectedParts
                    }
                }
            ).value();
            return this.$http.post(this.controllers.Jenkins.forceBuild().absoluteURL(), {pullRequestId:buildAction.pullRequestId, branchId:buildAction.branchId, cycleName : buildAction.cycleName,  parameters: categories});
        }

        toggleBuild(branchId:string, buildNumber:number, toggled:boolean):ng.IHttpPromise<Build> {
            return this.$http.post(this.controllers.Jenkins.toggleBuild(branchId, buildNumber, toggled).absoluteURL(), {});
        }

        changeEntityState(entityId:number, nextStateId:number):ng.IHttpPromise<EntityState> {
            return this.$http.post(this.controllers.Targetprocess.changeEntityState(entityId, nextStateId).absoluteURL(), {});
        }

        getBuildActions(branch:string, build?:number):ng.IHttpPromise<BuildAction[]>{
           return this.$http.get(this.controllers.Jenkins.buildActions(branch, build).absoluteURL());
        }

        run(branch:string, build:number, part:string, run:string):ng.IHttpPromise<BuildNode> {
            return this.$http.get(this.controllers.Jenkins.run(branch, build, part, run).absoluteURL())
        }

        testCase(branch:string, build:number, part:string, run:string, test:string):ng.IHttpPromise<TestCase> {
            return this.$http.get(this.controllers.Jenkins.testCase(branch, build, part, run, test).absoluteURL())
        }

        merge(branch:string):ng.IHttpPromise<MergeButtonResult> {
            return this.$http.get(this.controllers.Github.merge(branch).absoluteURL())
        }

        updateInfo(slackName:string):ng.IHttpPromise<any> {
            return this.$http.post(this.controllers.Login.updateInfo(slackName).absoluteURL(), {});
        }

        getActivities(branchName:string):ng.IHttpPromise<ActivityEntry[]> {
            return this.$http.get(this.controllers.Branches.activities(branchName).absoluteURL());
        }

    }

}