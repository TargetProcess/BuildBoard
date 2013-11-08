/// <reference path="../_all.ts" />
module buildBoard {
    export class EntityTitleDirective implements ng.IDirective {

        static name = "entityTitle";


        scope = {
            entity: "=",
            clickable: "@"
        };


        template = [
            '<div ng-show="entity">',
            '<a href="{{entity.url}}" target="_blank" class="ui-entity ui-entity-{{entity.entityType.toLowerCase()}}"> {{entity.id}}</a>',
            '{{entity.name}}',
            '</div>'
        ].join("");

        restrict = "EA";
        replace = true;
    }
}