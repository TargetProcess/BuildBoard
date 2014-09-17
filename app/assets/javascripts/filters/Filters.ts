/// <reference path='../_all.ts' />
module buildBoard {

    export function activeFilter() {
        return (isActive:boolean)=>isActive ? 'active' : '';
    }

    export function encode() {
        return encodeURIComponent;
    }

    export function status2Class() {
        return (status:Status)=> {
            switch (status) {
                case Status.Toggled:
                    return "toggled success";
                case Status.Success:
                    return "success";
                case Status.Failed:
                case Status.Aborted:
                    return "warning";
                case Status.InProgress:
                    return "in_progress";
                case Status.TimedOut:
                    return "warning";
                case Status.Unknown:
                default:
                    return "default";
            }
        }
    }

    export function parseBuildNodeStatus() {
        return (node:BuildNode)=>status2Class()(StatusHelper.parseBuildNode(node));
    }

    export function parseTestCaseStatus() {
        return (testCase:TestCase) => status2Class()(StatusHelper.parseTestCase(testCase));
    }

    export function status2text() {
        return (status:Status)=> {
            switch (status) {
                case Status.Toggled:
                    return "Toggled";
                case Status.Success:
                    return "Success";
                case Status.Failed:
                    return "Failed";
                case Status.Aborted:
                    return "Aborted";
                case Status.InProgress:
                    return "In progress";
                case Status.TimedOut:
                    return "Timed out";
                case Status.Unknown:
                default:
                    return "Unknown";
            }
        }
    }


    export function pullRequestStatus() {
        return (pullRequest:PullRequest)=> {
            if (pullRequest && pullRequest.status) {
                if (pullRequest.status.isMerged) {
                    return Status.Success;
                } else if (pullRequest.status.isMergeable) {
                    return Status.Success;
                } else {
                    return Status.Failed;
                }
            }
            else {
                return Status.Unknown;
            }
        }
    }

    export function duration() {
        return (seconds:number) => {
            var result = '';

            var hours = (seconds / 3600);
            if (hours > 1) {
                result = Math.round(hours) + 'h ';
            }

            var minutes = (seconds % 3600) / 60;
            if (hours > 1 || minutes > 1) {
                result = result + Math.round(minutes) + "m ";
            }

            var seconds = (seconds % 60);
            result = result + Math.round(seconds) + 's';

            return result;
        }
    }

    export function suppressZero() {
        return (count:number) => count == 0 ? '' : ('' + count);
    }

    export function friendlyDate($filter) {
        var dateFilter = $filter('date');
        return (dateN:number)=> {
            var today = new Date();
            today.setHours(0, 0, 0, 0);
            var date = new Date(dateN);
            date.setHours(0, 0, 0, 0);
            var delta = today.getTime() - date.getTime();
            switch (delta) {
                case 0:
                    return 'Today, ' + dateFilter(dateN, 'HH:mm');
                case 24 * 60 * 60 * 1000:
                    return 'Yesterday, ' + dateFilter(dateN, 'HH:mm');
                default:
                    return dateFilter(dateN, 'd/M/yy HH:mm');
            }
        }
    }
}
