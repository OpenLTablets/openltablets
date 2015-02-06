package org.openl.rules.dt2.storage;

public interface IStorage<T> {

	public enum StorageType {
		VALUE, SPACE, ELSE, FORMULA;
	}

	int size();

	Object getValue(int index);

	boolean isSpace(int index);

	boolean isFormula(int index);

	boolean isElse(int index);
	
	
	void setValue(int index, Object o);
	void setSpace(int index);
	void setElse(int index);
	
	void setFormula(int index, Object formula);

	
	

}
