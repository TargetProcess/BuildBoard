declare module buildBoard {
    interface IAction {
        absoluteURL(): string
    }

    interface JsRoutes {
        controllers: Controllers
    }

    interface Controllers {
        Login : ILogin;
        Branches : IBranches;
        Github: IGithub
        Jenkins: IJenkins
        Targetprocess: ITargetProcess
    }

    interface ITargetProcess {
        changeEntityState(entityId:number, nextStateId:number)
    }

    interface IJenkins {
        forceBuild():IAction
        toggleBuild(branchId:string, buildNumber:number, toggled:boolean):IAction
        lastBuildInfos():IAction;
        builds(branch:string):IAction;
        build(branch:string, buildNumber:number);
        buildActions(branch:string, buildNumber:number):IAction;
        run(branch:string, build:number, part:string, run:string):IAction
        testCase(branch:string, build:number, part:string, run:string, test:string):IAction
    }

    interface IGithub {
        merge(branch:string):IAction
    }


    interface IBranches {
        branches(): IAction;
        activities(name:string) : IAction;
    }

    export interface ILogin {
        logout():IAction
        updateInfo(slackName:string):IAction
    }

}