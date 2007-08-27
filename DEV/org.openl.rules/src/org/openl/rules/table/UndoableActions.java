/**
 * Created Feb 17, 2007
 */
package org.openl.rules.table;

import java.util.ArrayList;


/**
 * @author snshor
 *
 */
public class UndoableActions 
{

	ArrayList undoableActions = new ArrayList();
	
	int currentUndoIndex = 0;
	int undoSize = 0;
	
	public void addNewAction(IUndoableAction iu)
	{
		undoableActions.ensureCapacity(currentUndoIndex + 1);
		if (currentUndoIndex < undoableActions.size())
			undoableActions.set(currentUndoIndex, iu);
		else
			undoableActions.add(iu);
		++currentUndoIndex;
		undoSize = currentUndoIndex;
	}
	
	public boolean hasUndo()
	{
		return currentUndoIndex > 0;
	}

	public boolean hasRedo()
	{
		return currentUndoIndex < undoSize;
	}
	
	public IUndoableAction undo()
	{
		return (IUndoableAction) undoableActions.get(--currentUndoIndex);
	}
	

	public IUndoableAction redo()
	{
		return (IUndoableAction) undoableActions.get(currentUndoIndex++);
	}
	
}
