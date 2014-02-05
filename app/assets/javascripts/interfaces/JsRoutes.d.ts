declare module buildBoard {
    interface IAction {
        absoluteURL(): string
    }

    interface JsRoutes {
        controllers: Controllers
    }

    interface Controllers {
        Login : ILogin;
        Application : IApplication;
        Github: IGithub
        Jenkins: IJenkins
        Targetprocess: ITargetProcess
    }

    interface ITargetProcess {
        changeEntityState(entityId:number, nextStateId:number)
    }

    interface IJenkins {
        forceBuild(prId:number, branchId:string, fullCycle:boolean):IAction
        toggleBuild(branchId:string, buildNumber:number):IAction
        lastBuildInfos():IAction
        builds(branch:string):IAction
        testCasePackages(file:string):IAction
    }

    interface IGithub {
        pullRequestStatus(id:number):IAction
    }


    interface IApplication {
        branches(): IAction;
        branch(id:string): IAction;
    }

    export interface ILogin {
        logout():IAction
    }

}