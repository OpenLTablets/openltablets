package org.openl.rules.tableeditor.model;

import org.openl.rules.tableeditor.event.TableEditorController.EditorTypeResponse;

public class TextCellEditor implements ICellEditor
{

    public EditorTypeResponse getEditorTypeAndMetadata()
    {
        EditorTypeResponse typeResponse = new EditorTypeResponse(CE_TEXT);
        return typeResponse;
    }

    public ICellEditorServerPart getServerPart()
    {
	return null;
    }

}
