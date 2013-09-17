require(['jquery'], function($){
    $(function () {
        /*
        $('.role-create-pull-request-dialog').dialog({
            autoOpen: false,
            height: 300,
            width: 350,
            modal: true,
            draggable: false,
            resizable: false,
            buttons: {
                "Create pull request": function () {
                    $(this).dialog("close");
                },
                Cancel: function () {
                    $(this).dialog("close");
                }
            }
        });

          */

        $('.pull-request').each(function () {
            var $a = $(this);
            var id = $a.data('id');
            if (id) {
                jsRoutes.controllers.Github.pullRequestStatus(id).ajax().done(function (resp) {
                    var isMerged = resp.isMerged;
                    var isMergeable = resp.isMergeable;
                    $a.removeClass('btn-default');
                    if (isMerged) {
                        $a.addClass('btn-primary');
                    } else if (isMergeable) {
                        $a.addClass('btn-success');
                    } else {
                        $a.addClass('btn-danger');
                    }
                });
            }
        });

        $('tr').on('click', '.role-change-state', function (e) {
            e.preventDefault();
            var $nextState = $(this);

            var $button = $nextState.closest('.role-next-states-group').find('.role-next-states-trigger');
            $button.attr('disabled', 'disabled');

            var entityStateId = $nextState.data("stateId");
            var entityId = $nextState.data("entityId");

            jsRoutes.controllers.Targetprocess.changeEntityState(entityId, entityStateId).ajax().done(function (newState) {
                $nextState.closest('td.role-state').replaceWith(newState.text);
            });
        });
    });
});
