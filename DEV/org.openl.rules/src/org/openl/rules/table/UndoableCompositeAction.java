/**
 * Created Feb 17, 2007
 */
package org.openl.rules.table;

import java.util.Iterator;
import java.util.List;


/**
 * @author snshor
 *
 */
public class UndoableCompositeAction implements IUndoableGridAction
{

	List actions;
	
	public UndoableCompositeAction(List actions)
	{
		this.actions = actions;
	}

	public void doAction(IWritableGrid grid, IUndoGrid undo)
	{
		for (Iterator iter = actions.iterator(); iter.hasNext();)
		{
			IUndoableGridAction element = (IUndoableGridAction) iter.next();
			element.doAction(grid, undo);
		}
	}

	public void undoAction(IWritableGrid grid, IUndoGrid undo)
	{
		for (Iterator iter = actions.iterator(); iter.hasNext();)
		{
			IUndoableGridAction element = (IUndoableGridAction) iter.next();
			element.undoAction(grid, undo);
		}
		
	}

}
