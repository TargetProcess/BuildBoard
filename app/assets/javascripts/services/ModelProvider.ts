/// <reference path='../_all.ts' />
/// <reference path='BranchesService.ts'/>
module buildBoard {

    export class ModelProvider {
        public static NAME = "modelProvider";

        public static $inject = [
            BranchesService.NAME
        ];

        public branches:Branch[];

        constructor($branchesService:BranchesService) {
            $branchesService.allBranches.then(branches=> {

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

                this.branches = branches;
            });
        }

        public findBranch(branchName:string):Branch {
            return _.find(this.branches, b=>b.name == branchName);
        }

        public getLastBuild(branchName:string):Build {
            var branch = this.findBranch(branchName);
            if (branch) {
                return branch.lastBuild;
            }
        }

        public getPrevBuild(branchName:string):Build {

            var branch = this.findBranch(branchName);
            if (!branch || !branch.activity) {
                return null;
            }

            var lastBuild = this.getLastBuild(branchName);
            if (!lastBuild) {
                return null;
            }

            return _.chain(branch.activity)
                .find(x=> {
                    if (lastBuild.timestamp == x.timestamp)
                        return false;

                    if (x.activityType != "build" && _.isUndefined(x.parsedStatus)) {
                        return false;
                    }

                    var status = (<BuildBase>(x)).parsedStatus;
                    return !(status == Status.Unknown || status == Status.InProgress);

                })
                .value();

        }
    }
}