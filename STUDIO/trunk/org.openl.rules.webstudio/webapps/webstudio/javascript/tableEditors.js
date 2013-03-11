function PropsEditor () {
    this.initElement = function(cell) {
        var cellProps = cell.data;
        
        var propName = cellProps.type;

        var actionURL = "/webstudio/action/prop_values";
        $j.ajax({
            type:"GET",
            url:actionURL,
            data: "propName="+propName,
            dataType: "json",
            success:function(data, textStatus, XMLHttpRequest){
                hideLoader();
                initComplexSelect(data, cell);
            },
            error:function(XMLHttpRequest, textStatus, errorThrown){
                hideLoader();
            }
        });
    };
};

function createDiv() {
    var newElement = document.createElement('div');
    newElement.id = "div_editor_holder"+Math.floor(Math.random() * 1001);

    return newElement;
}

function initComplexSelect(data, cell) {
   var element = createDiv();

   prop = cell.data;
   var editor;
   showEditorDiv(cell, element);

   if (data.type == "DATE") {
       var specElement = createDiv();
       $j(element).append(specElement);
       
       editor = new DateEditor('', specElement.id, '', prop.getValue() , true);
   } else if (data.type == "TEXT") {
       editor = new TextEditor('', element.id, '', prop.getValue() , true);
   } else if (data.type == "SINGLE") {
       editor = new DropdownEditor('', element.id, data.param, prop.getValue() , true);
   } else if (data.type == "BOOLEAN") {
       editor = new BooleanEditor('', element.id, '', prop.getValue() == true ? "true" : "false", true);
   } else {
       editor = new MultiselectEditor('', element.id, data, prop.getValue(), true);
   }

   setNewEditor(cell, editor);
};

function showEditorDiv(cell, elementForAdding) {
    if (typeof elementForAdding != "undefined") {
        $j("#editor_div").html("");
        $j("#editor_div").append(elementForAdding);
    }

    var minWidth = 20;
    var width = cell.offsetWidth - 2;

    if (width < minWidth) {
        cell.style.minWidth = minWidth + "px";
        width = cell.offsetWidth - 2;
    }

    var topPos = $j(cell).position().top;
    var leftPos = $j(cell).position().left;

    var browserName = navigator.appName; 
    if (browserName == "Netscape") { 
        if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
            //chrome
            $j("#editor_div").height(cell.offsetHeight + 2 + "px");
            $j("#editor_div").width(width + 2 +"px");


            $j("#editor_div").find(">:first-child").height(cell.offsetHeight + 2 + "px");
            $j("#editor_div").find(">:first-child").width(width + 2 +"px");
        } else {
            $j("#editor_div").height(cell.offsetHeight - 2 + "px");
            $j("#editor_div").width(width+"px");


            $j("#editor_div").find(">:first-child").height(cell.offsetHeight - 2 + "px");
            $j("#editor_div").find(">:first-child").width(width+"px");
        }
    } else if (browserName=="Microsoft Internet Explorer") {
        $j("#editor_div").height(cell.offsetHeight - 9 + "px");
        $j("#editor_div").width((width - 7)+"px");


        $j("#editor_div").find(">:first-child").height(cell.offsetHeight - 9 + "px");
        $j("#editor_div").find(">:first-child").width((width - 7)+"px");
    }

    var position = {
        top : topPos,
        left : leftPos
    };

    $j('#editor_div').css(position);
    $j("#editor_div").show();
}

function setNewEditor(cell, editor) {
    $j("#editor_div").keypress(function(event) {
        if(event.keyCode == 13) {
            closeEditor(cell, editor);
            return false;
        }
    });

    editor.bind("blur", function() {
        closeEditor(cell, editor);
    });
}

function closeEditor(cell, editor) {
    var dataCell = cell.data;
    $j("#editor_div").hide();
    $j("#editor_div").offset({left:0,top:0});
    dataCell.value = editor.getValue();
    editor.destroy();
    cell.innerHTML = dataCell.getValue();
};

function Editor(){
    this.initElement = function(cell) {
        var dataCell = cell.data;

        var element = createDiv();
        showEditorDiv(cell, element);
        var editor = null;

        if((dataCell.valueType == "INT" || dataCell.valueType == "FLOAT" ) && !dataCell.iterable) {
            //this.html = this.getIntElement(cell);
            editor = new NumericEditor('', element.id, '', dataCell.getValue() , true);
        } else if(dataCell.valueType == "BOOLEAN" && !dataCell.iterable) {
            //this.html = this.getBooleanElement(cell);
            editor = new BooleanEditor('', element.id, '', dataCell.getValue() == true ? "true" : "false", true);
        } else if(dataCell.valueType == "DATE" && !dataCell.iterable) {
            editor = new DateEditor('', element.id, '', dataCell.getValue() , true);
        } else if(dataCell.valueType == "STRING" && !dataCell.iterable) {
            //this.html = this.getStringElement(cell);
            editor = new TextEditor('', element.id, '', dataCell.getValue() , true);
        } else {
            editor = new NumericEditor('', element.id, '', dataCell.getValue() , true);
        }

        setNewEditor(cell, editor);
    };
};