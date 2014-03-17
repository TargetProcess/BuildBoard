/// <reference path='../_all.ts' />
/// <reference path='BranchesService.ts'/>
module buildBoard {

    export class ModelProvider {
        public static NAME = "modelProvider";

        public static $inject = [
            '$q',
            BranchesService.NAME,
            BackendService.NAME,
        ];

        public branches:Branch[];

        constructor($q:ng.IQService, $branchesService:BranchesService, $backendService:BackendService) {
            var $branches = $branchesService.allBranches;
            var $buildsPerBranchName = $branchesService.allBranchesWithLastBuilds;
            var $developBuilds = $backendService.builds('develop');

            $branches.then(branches=>{
                this.branches = branches;
                $buildsPerBranchName.then(builds=>{
                    _.each(builds, build=>{
                        if (build)
                            build.getStatus = () => StatusHelper.parse(build)
                        });
                    _.each(this.branches, branch=> {
                        var build = builds[branch.name.toLowerCase()];
                        if (build) {
                            if (!branch.lastBuild || branch.lastBuild.timestamp < build.timestamp) {
                                branch.lastBuild = build;
                            }
                        }
                        else {
                            branch.lastBuild = null;
                        }
                    })
                });

                $developBuilds.success(develop=>{
                    var developBranch:Branch = _.find(this.branches, b=>b.name=='develop');
                    _.each(develop, build=>{build.getStatus = () => StatusHelper.parse(build)});
                    developBranch.builds = develop;
                    developBranch.lastBuild = _.chain(develop).sortBy(x=>-x.timestamp).first().value();
                })
            });
        }


        public findBranch(branchName:string):Branch {
            return _.find(this.branches, b=>b.name == branchName);
        }
    }


}