/// <reference path='../_all.ts' />
module buildBoard {

    export interface IBranchesService {
        allBranches:ng.IPromise<Branch[]>
    }

    export class BranchesService implements IBranchesService {
        public static NAME = "branchesService";

        public static $inject = [
            BackendService.NAME,
            '$q'
        ];

        public allBranches:ng.IPromise<Branch[]>;


        constructor(private backendService:BackendService, $q: ng.IQService) {
            var branchesWithoutBuilds = backendService.branches();
            var lastBuildInfos = backendService.lastBuilds();
            var promises:ng.IPromise<any>[] = [branchesWithoutBuilds, lastBuildInfos];

            this.allBranches = $q.all(promises).then(x=>{
                var branchesResult:Branch[] = x[0].data;
                var buildsResult:IMap<Build> = x[1].data;

                _.chain(buildsResult)
                    .keys()
                    .each(key=> {
                        var match = key.match(/origin\/(?:pr\/(\d*)\/merge|(.+))/i);
                        if (match != null) {
                            var branch = _.find(branchesResult, x=> (match[2] && x.name.toLowerCase() == match[2].toLowerCase()) || (x.pullRequest &&
                                x.pullRequest.prId == match[1]));
                            if (branch) {
                                if (!branch.lastBuild || branch.lastBuild.timeStamp < buildsResult[key].timeStamp) {
                                    branch.lastBuild = buildsResult[key];
                                }
                            }
                        }
                    });

                return branchesResult;
            });
        }

    }

}