
function openMenu(menuId, td, event) {
    PopupMenu.sheduleShowMenu(menuId, event, 400);
}

function closeMenu(td) {
    PopupMenu.cancelShowMenu();
}

Ajax.Responders.register({
    onFailure: function() {
        alert("global");
    }
});

function triggerEdit(editorId, url, cellToEdit) {
    var cell = cellToEdit;
    if (!cell) {
        cell = $(PopupMenu.lastTarget);
    }
    var editor = $(editorId);
    new Ajax.Request(url, {
        method: "get",
        encoding: "utf-8",
        contentType: "text/javascript",
        parameters: {
            cell: cell.down("input").value.toQueryParams().cell,
            editorId: editor.id.replace('te_comp','')
        },
        onSuccess: function(data) {
            editor.innerHTML = data.responseText.stripScripts();
            new ScriptLoader().evalScripts(data.responseText);
        },
        onFailure: AjaxHelper.handleError
    });
}

function triggerEditXls(url) {
    var excelUri = $(PopupMenu.lastTarget).down("input").value;
    window.open(url + "?uri=" + escape(excelUri));
}