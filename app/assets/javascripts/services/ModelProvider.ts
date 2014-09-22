/// <reference path='../_all.ts' />
/// <reference path='BranchesService.ts'/>
module buildBoard {

    export class ModelProvider {
        public static NAME = "modelProvider";

        public static $inject = [
            BranchesService.NAME,
            '$q'
        ];

        public branches:ng.IPromise<Branch[]>;
        public lastDevelopBuilds:ng.IPromise<Build[]>;

        constructor(private $branchesService:BranchesService, private $q:ng.IQService) {
            this.branches = $branchesService.allBranches.then(branches=> {

                _.each(branches, (br:Branch)=> {
                    _.each(br.activity, a=> {
                        switch (a.activityType) {
                            case 'build':
                                var build:Build = a;
                                build.parsedStatus = StatusHelper.parseInfo(build.status, build.toggled);
                                break;
                        }
                    });
                    if (br.lastBuild) {
                        br.lastBuild.parsedStatus = StatusHelper.parseInfo(br.lastBuild.status, br.lastBuild.toggled);
                    }
                });

                return branches;
            });

            this.lastDevelopBuilds = this.$branchesService.getLastBuilds('develop', 2).then(builds=>{
                _.forEach(builds, b=>{
                    b.parsedStatus = StatusHelper.parseInfo(b.status, b.toggled);
                });
                return builds;
            });
        }

        public findBranch(branchName:string):ng.IPromise<Branch> {
            return this.branches.then(branches=>_.find(branches, b=>b.name == branchName));
        }

        public getBranchWithActivities(branchName:string):ng.IPromise<Branch> {
            var activitiesQ = this.$branchesService.getActivities(branchName);
            var branchQ = this.findBranch(branchName);

            return this.$q.all([activitiesQ, branchQ])
                .then(results=> {
                    var branch = <Branch>results[1];
                    if (branch) {
                        branch.activity = results[0];
                    }
                    return branch;
                });

        }
    }
}