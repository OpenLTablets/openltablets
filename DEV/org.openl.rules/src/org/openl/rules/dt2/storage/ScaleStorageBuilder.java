package org.openl.rules.dt2.storage;

import org.openl.rules.dt2.DTScale.RowScale;

@SuppressWarnings("rawtypes")
public class ScaleStorageBuilder implements  IStorageBuilder {

	
	RowScale scale; 
	StorageBuilder sb;
	
	public ScaleStorageBuilder(RowScale scale, StorageBuilder sb) {
		super();
		this.scale = scale;
		this.sb = sb;
	}


	public void writeObject(Object loadedValue, int index) {
		sb.writeObject(loadedValue, getStorageIndex(index));
	}

	private int getStorageIndex(int index) {
		return scale.getActualIndex(index);
	}

	public int size() {
		return sb.size() * scale.getMultiplier();
	}


	@Override
	public IStorage optimizeAndBuild() {
		IStorage storage = sb.optimizeAndBuild();
		
		return new ScaledStorage(scale, storage, sb.getInfo());
	}





}
