/**
 * Table editor.
 *
 * @requires prototype JavaScript library
 *
 * @author Andrey Naumenko
 */

var TableEditor = Class.create();

/**
 *  Here is editors registry. The editors would add themselves to this hash with the name as a key
 */
TableEditor.Editors = $H();

TableEditor.Constants = {
    ADD_BEFORE : 1,
    ADD_AFTER : 2,
    MOVE_DOWN : 3,
    MOVE_UP : 4,
    REMOVE : 5
};

// standalone functions

TableEditor.isNavigationKey = function (keyCode) {return  keyCode >= 37 && keyCode <= 41}

/**
 * returns array [row, column] from string like 'B20' - excel style cell coordinates   
 */
TableEditor.parseXlsCell = function (s) {
    var m = s.match(/^([A-Z]+)(\d+)$/)
    if (m) {
        var h = m[1];
        var col = 0;
        var Acode = "A".charCodeAt(0) - 1;
        for (var i = 0; i < h.length; ++i) col = 26 * col + h.charCodeAt(i) - Acode;
        return [Number(m[2]), col]
    }
    return null
}
TableEditor.prototype = {
    editorId : -1,    
    tableContainer : null,
    currentElement : null,
    editor : null,
    saveUrl : null,
    baseUrl : null,
    selectionPos : null,
    selectionHistory : [],
    decorator : null,
    rows : 0,
    columns : 0,
    table : null,
    modFuncSuccess : null,
    editCell : null,

    /** Constructor */
    initialize : function(editorId, url, editCell) {
        this.editorId = editorId;
        this.tableContainer = $(editorId + "_table");
        this.tableContainer.style.cursor = 'default';
        // Suppress text selection
        this.tableContainer.onselectstart = function() {
            return false;
        }
        this.tableContainer.onmousedown = function() {
            return false;
        }
        
        if (editCell) this.editCell = editCell;

        this.baseUrl = url;
        this.saveUrl = this.buildUrl("save");

        var self = this;

        Event.observe(/*Prototype.Browser.IE ? document.body : window */document, "keydown", function(e) {
            self.handleKeyDown(e)
        }, false);

        if (Prototype.Browser.IE || Prototype.Browser.Opera) {
            document.onkeypress = function(e) {self.handleKeyPress(e || window.event)}
        } else
            Event.observe(/*Prototype.Browser.IE ? document.body : window */document, "keypress", function(e) {
                self.handleKeyPress(e)
            }, false);

        this.tableContainer.observe("click", function(e) {
            self.handleClick(e);
        }, false);

        this.tableContainer.observe("dblclick", function(e) {
            self.handleDoubleClick(e);
            Event.stop(e);
        });

        this.modFuncSuccess = function(response) {
            response = eval(response.responseText);
            if (response.status) alert(response.status);
            else {
                this.renderTable(response.response);
                this.selectElement();
                this.processCallbacks(response, "do");
            }
        }.bindAsEventListener(this);
    },

    /**
     * @desc: load data from specific url
     * @type: private
     */
    loadData : function(url) {
        if (!url) url = this.buildUrl("load");
        var self = this;
        new Ajax.Request(url, {
            method      : "get",
            encoding    : "utf-8",
            contentType : "text/javascript",
            parameters : {
                editorId: this.editorId
            },
            onSuccess   : function(data) {
                data = eval(data.responseText);
                self.renderTable(data.tableHTML.strip());
                self.processCallbacks(data, "do");

                if (self.editCell) {
                    var s = TableEditor.parseXlsCell(self.editCell);
                    var t = TableEditor.parseXlsCell(data.topLeftCell);
                    if (s && t) {
                        s[0] -= t[0]-1; s[1] -= t[1]-1;
                        var cell = self.$cell(s);
                        if (cell) self.editBeginRequest(cell, null, true);
                    }
                }
            },
            onFailure: AjaxHelper.handleError
        });
    },

    /**
     * @desc: renders table
     * @type: private
     */
    renderTable : function(data) {
        this.decorator = new Decorator();

        this.tableContainer.innerHTML = data;
        this.table = $(this.tableContainer.childNodes[0]);

        this.computeTableInfo(this.table);
    },

    /**
     * @desc: computes table width in rows, and height in columns (that is sum of all rowSpans in a column
     * and sum of all colSpans in a row).
     * @type: private
     */
    computeTableInfo: function(table) {
        this.rows = 0;
        this.columns = 0;

        var row = table.down("tr");
        if (row) {
            var tdElt = row.down("td");
            while (tdElt) {
                this.columns += tdElt.colSpan ? tdElt.colSpan : 1;
                tdElt = tdElt.next("td");
            }
        }

        while (row) {
            var tdElt = row.down("td")
            this.rows += tdElt.rowSpan ? tdElt.rowSpan : 1;
            row = row.next("tr");
        }
    },

    /**
     * @desc: makes all changes persistant. Sends corresponding request to the server.
     * @type: public
     */
    save: function() {
        var self = this;
        new Ajax.Request(this.buildUrl("saveTable"), {
            parameters : {
                editorId: this.editorId
            },
            onSuccess  : function(response) {
                response = eval(response.responseText);
                if (response.status)
                    alert(response.status);
                else {
                	self.processCallbacks(response, "do");
                	if (window.parent.frames.leftFrame.location.toString().indexOf("?resetStudio=true") == -1) {
                		window.parent.frames.leftFrame.location = window.parent.frames.leftFrame.location + "?resetStudio=true";
                	} else {
                		window.parent.frames.leftFrame.location.reload();
                	}
                    alert("Your changes have been saved!");
                }
            },
            onFailure: function(response) {
                AjaxHelper.handleError(response,
                        "Server failed to save your changes");
            }
        });
    },

    /**
     * @desc: handles mouse double click on table
     * @type: private
     */
    handleDoubleClick: function(event) {
        var cell = Event.findElement(event, "TD");

    // Save value of current editor and close it
        this.editStop();
        this.editBeginRequest(cell);
    },


    /**
     * @desc: sends request to server to find out required editor for a cell. After getting response calls this.editBegin
     * @type: private
     */
    editBeginRequest : function(cell, keyCode, ignoreAjaxRequestCount) {
        if (!ignoreAjaxRequestCount && Ajax.activeRequestCount > 0) return;
        var self = this;

        this.selectElement(cell);

        var typedText = undefined;
        if (keyCode) typedText = String.fromCharCode(keyCode);

        new Ajax.Request(this.buildUrl("getCellType"), {
            onSuccess  : function(response) {
                self.editBegin(cell, eval(response.responseText), typedText)
            },
            parameters : {
                editorId: this.editorId,
                row : self.selectionPos[0],
                col : self.selectionPos[1]
            },
            onFailure: AjaxHelper.handleError
        });
    },

    /**
     *  @desc: Create and activate new editor
     */
    editBegin : function(cell, response, typedText) {
        this.editor = new TableEditor.Editors[response.editor](this, cell, response.params, typedText);
        this.selectElement(cell);
    },

    switchEditor: function(editorName) {
        var prevEditor = this.editor;
        this.editor = new TableEditor.Editors[editorName];
        prevEditor.doSwitching(this.editor);
    },

    editStop : function() {
        if (this.editor) {
            if (!this.editor.isCancelled()) {
                var val = this.editor.getValue();
                var self = this;
                new Ajax.Request(this.saveUrl, {
                    method    : "get",
                    encoding   : "utf-8",
                    contentType : "text/javascript",
                    onSuccess  : function(data) {
                        data = eval(data.responseText);
                        self.processCallbacks(data, "do");
                    },
                    parameters: {
                        editorId: this.editorId,
                        row : self.selectionPos[0],
                        col : self.selectionPos[1],
                        value: val
                    },
                    onFailure: AjaxHelper.handleError
                });
            }
            this.editor.detach();
        }
        this.editor = null;
    },

    /**
     * @desc: handles mouse click on the table
     * @type: private
     */
    handleClick: function(e) {
        var elt = Event.element(e);
        this.editStop();
        if (elt.tagName == "TD") {
            this.selectElement(elt);
            Event.stop(e);
        }
    },

    buildUrl: function(action, paramString) {
        var url = this.baseUrl + action;
        if (paramString)
            url = url + "?" + paramString;
        return url
    },


    /**
     * @desc: makes a cell 'selected', that is sets up this.selectionPos and this.currentElement, and also applies
     * visual decoration to the cell.
     * If elt is null(undefined) than this.currentElement is set based on value of this.selectionPos array.
     * dir param is used to track selections history, if it is not given history is cleared, if it is set to -1 and
     * elt param is not given the new selection is taken from history.
     * @type: private
     */
    selectElement: function(elt, dir) {
        if (elt && this.currentElement && elt.id == this.currentElement.id) return;

        var wasSelected = this.hasSelection();

        if (elt && dir) { // save to selection history
            if (this.selectionPos) this.selectionHistory.push([dir, this.selectionPos[0], this.selectionPos[1]]);
            if (this.selectionHistory.length > 10) this.selectionHistory.shift();
        } else {
            if (dir == -1) {
                var lastEntry = this.selectionHistory.pop();
                this.selectionPos[0] = lastEntry[1];
                this.selectionPos[1] = lastEntry[2];
            } else
                this.selectionHistory.clear();
        }

        if (elt) {
            var newSelectionPos = this.elementPosition(elt);
            if (!newSelectionPos) return;
            this.selectionPos = newSelectionPos;
        } else if (this.selectionPos) {
            elt = this.$cell(this.selectionPos);
        }
        this.decorator.undecorate(this.currentElement);
        this.decorator.decorate(this.currentElement = elt);

        if (!wasSelected != !this.hasSelection()) this.isSelectedUpdated(!wasSelected);
    },

    $cell: function(pos) {
        var cell = $(this.editorId + "_cell-" + pos[0] + ":" + pos[1]);
        if (!cell) return cell;
        if (!cell.rowSpan) cell.rowSpan = 1;
        if (!cell.colSpan) cell.colSpan = 1;
        return cell;
    },

    handleKeyPress:  function(event) {
        if (this.editor) {
            switch (event.keyCode) {
                case 27: this.editor.cancelEdit(); break;
                case 13: if ( this.editor.__do_nothing_on_enter !== true ) {
                    this.editStop();
                    if (Prototype.Browser.Opera) event.preventDefault();
                }
                break;
            }

            return
        }

        if (event.keyCode == 13) {
            if (this.hasSelection()) this.editBeginRequest(this.currentElement);
            return;
        }

        if (this.hasSelection()) {
            if ([event.ctrlKey, event.altKey, event.shiftKey, event.metaKey].any()) return;
            if (event.charCode != undefined) { // FF
                if (event.charCode == 0) return true;
            } else if (event.keyCode < 32 || TableEditor.isNavigationKey(event.keyCode)) return true;

            if (Prototype.Browser.Opera) {
                if (event.which == 0) return;
                event.preventDefault();
            }

            this.editBeginRequest(this.currentElement, event.charCode || event.keyCode);
        }
    },

    /**
     * @desc: handles key presses. Performs table navigation.
     * @type: private
     */
    handleKeyDown: function(event) {
        if (this.editor) {
            switch (event.keyCode) {
                case 113: this.editor.handleF2(event); break;
                case 114: this.editor.handleF3(event); break;
            }
            return;
        }
        if (!TableEditor.isNavigationKey(event.keyCode)) return;

        if (!this.hasSelection()) {
            this.selectionPos = [1, 1];
            this.selectElement();
            return;
        }

        var sp = this.selectionPos.clone();

        // check history
        if (this.selectionHistory.length > 0 && this.selectionHistory.last()[0] == event.keyCode) {
            this.selectElement(null, -1);
            return;
        }

        var scanUpLeft = function(index, noRestore) {
            var tmp = sp[index];
            while (sp[index] >= 1 && !this.$cell(sp)) --sp[index];
            var res = this.$cell(sp);
            if (!noRestore) sp[index] = tmp;
            return res;
        }

        switch (event.keyCode) {
            case 37: case 38: // LEFT, UP
            var cell = null;
            var theIndex = event.keyCode == 38 ? 0 : 1;
            while (--sp[theIndex] >= 1) {
                cell = scanUpLeft.call(this, 1 - theIndex, true);
                if (cell) {
                    if ((sp[0] + cell.rowSpan >= this.selectionPos[0] + theIndex) &&
                        (sp[1] + cell.colSpan >= this.selectionPos[1] + 1 - theIndex))
                        break;
                }
                sp[1 - theIndex] = this.selectionPos[1 - theIndex];
            }
            if (cell) this.selectElement(cell, event.keyCode + 2);
            break;

            case 39: case 40:  //RIGHT, DOWN
            var theIndex = event.keyCode == 40 ? 0 : 1;

            sp[theIndex] += this.currentElement[["rowSpan", "colSpan"][theIndex]];
            if (sp[theIndex] > this[["rows", "columns"][theIndex]]) break;
            var newCell = scanUpLeft.call(this, 1 - theIndex);
            if (newCell) this.selectElement(newCell, event.keyCode - 2);
            break;
        }
    },

    undoredo: function(redo) {
        if (Ajax.activeRequestCount > 0) return;
        new Ajax.Request(this.buildUrl((redo ? "redo" : "undo")), {
            parameters: {
                editorId: this.editorId
            },
            onSuccess: this.modFuncSuccess,
            onFailure: AjaxHelper.handleError
        })
    },

    /**
     * @desc: inspect element id and extracts its position in table. Element is expected to be a TD
     * @type: private
     */
    elementPosition: function(e) {
        var id = $(e).id;
        var pos = id.lastIndexOf("-");
        if (pos < 0) return null;
        var splitted = id.substr(pos + 1).split(":", 2);
        splitted[0] = parseInt(splitted[0]);
        splitted[1] = parseInt(splitted[1]);
        return splitted;
    },


    setAlignment: function(_align) {
        if (!this.hasSelection()) {
            alert("Nothing is selected");
            return;
        }

        var cell = this.currentElement;
        var self = this;
        var params = {
            editorId: this.editorId,    
            row : this.selectionPos[0],
            col : this.selectionPos[1],
            align: _align
        }
        new Ajax.Request(this.buildUrl("setAlign"), {
            onSuccess: function(response) {
                response = eval(response.responseText);
                if (response.status)
                    alert(response.status);
                else {
                    self.processCallbacks(response, "do");
                    var editor = cell.down('input');
                    if (editor) {
                        editor.style.textAlign = _align;
                    }
                    cell.align = _align;
                }
            },
            parameters : params,
            onFailure: AjaxHelper.handleError
        });
    },

    indent: function(_indent) {
        if (!this.hasSelection()) {
            alert("Nothing is selected");
            return;
        }

        var cell = this.currentElement;
        var self = this;
        var params = {
            editorId: this.editorId,    
            row : this.selectionPos[0],
            col : this.selectionPos[1],
            indent: _indent
        }
        new Ajax.Request(this.buildUrl("indent"), {
            onSuccess: function(response) {
                response = eval(response.responseText);
                if (response.status)
                    alert(response.status)
                else {
                    self.processCallbacks(response, "do");
                    resultPadding = 0;
                    if (cell.style.paddingLeft.indexOf("em") > 0) {
                        resultPadding = parseFloat(cell.style.paddingLeft);
                    } else if (cell.style.paddingLeft.indexOf("px") > 0) {
                        resultPadding = parseFloat(cell.style.paddingLeft) * 0.063;
                    }
                    resultPadding = resultPadding + parseInt(_indent);
                    if (resultPadding >= 0) {
                        cell.style.paddingLeft = resultPadding + "em";
                    }
                }
            },
            parameters : params,
            onFailure: AjaxHelper.handleError
        });
    },

    doRowOperation: function(op) {this.doColRowOperation(0, op)},
    doColOperation: function(op) {this.doColRowOperation(1, op)},

    /**
     *  @type: private
     */
    doColRowOperation: function(index, op) {
        if (!this.hasSelection()) {
            alert("Nothing is selected");
            return;
        }

        var params = [];
        params.editorId = this.editorId;
        params[["row", "col"][index]] = (op == TableEditor.Constants.ADD_AFTER ? 1 : 0) + this.selectionPos[index];
        if (op == TableEditor.Constants.MOVE_DOWN) params.move = true;
        if (op == TableEditor.Constants.MOVE_UP) {
            params.move = true;
            params.up = true;
        }

        new Ajax.Request(this.buildUrl(([TableEditor.Constants.MOVE_DOWN, TableEditor.Constants.MOVE_UP, TableEditor.Constants.REMOVE].include(op) ? "removeRowCol" : "addRowColBefore")), {
            onSuccess : this.modFuncSuccess,
            parameters : params,
            onFailure: AjaxHelper.handleError
        });
    },

    hasSelection : function() {return this.selectionPos && this.currentElement},

    processCallbacks: function(obj, action) {
        function isBoolean(t) {return obj.hasUndo === true || obj.hasUndo === false}
        try {
            switch (action) {
                case "do":
                    if (obj) {
                        if (isBoolean(obj.hasUndo)) try {this.undoStateUpdated(obj.hasUndo)} catch (e) {}
                        this.redoStateUpdated(obj.hasRedo)
                    }
                    break;
                case "selection":
                    if (isBoolean(obj)) this.isSelectedUpdated(obj);
                    break;

            }
        } catch(ex) {}
    },
    // ----------------------------------------------------------------- Callback functions --
    undoStateUpdated : Prototype.emptyFunction,
    redoStateUpdated : Prototype.emptyFunction,
    isSelectedUpdated : Prototype.emptyFunction
}

/**
 *  Responsible for visual display of 'selected' element.
 */
var Decorator = Class.create();

Decorator.prototype = {
    /** Holds changed properties of last decorated  element */
    previosState : {},

    /** Empty constructor */
    initialize : Prototype.emptyFunction,

    /**
     * @desc changes elememnt style, so it looks 'selected'
     * @type: public
     */
    decorate: function(/* Element */ elt) {
        if (!elt) return;
        this.previosState = {
            color: elt.style.color,
            backgroundColor: elt.style.backgroundColor
        }

        elt.style.color = "white"
        elt.style.backgroundColor = "rgb(180, 200, 255)"
    },

    /**
     * @desc reverts 'selection' of last decorated element
     * @type: public
     */
    undecorate: function(/* Element */ elt) {
        if (elt) {
            Object.extend(elt.style, this.previosState)
        }
    }
}
