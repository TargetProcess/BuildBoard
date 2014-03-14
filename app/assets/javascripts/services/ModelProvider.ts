/// <reference path='../_all.ts' />
/// <reference path='BranchesService.ts'/>
module buildBoard {

    export class ModelProvider {
        public static NAME = "modelProvider";

        public static $inject = [
            '$q',
            BranchesService.NAME,
        ];

        public $branches:ng.IPromise<Branch[]>

        constructor($q:ng.IQService, $branchesService:BranchesService) {
            this.$branches = $branchesService.allBranches;
            this.$branches.then((branches:Branch[])=> {
                var $buildsPerBranchName = $branchesService.allBranchesWithLastBuilds;

                $buildsPerBranchName.then((builds:IMap<Build>)=>{
                    _.each(branches, branch=>{
                        var build = builds[branch.name.toLowerCase()];
                        if (build) {
                            if ((!branch.lastBuild || branch.lastBuild.timeStamp < build.timeStamp)) {
                                branch.lastBuild = build;
                            }
                        }
                        else {
                            branch.lastBuild = null;
                        }
                    })
                });
            });
        }
    }


}