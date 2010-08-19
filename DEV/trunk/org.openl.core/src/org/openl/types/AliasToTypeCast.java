package org.openl.types;

import org.openl.domain.IDomain;


public class AliasToTypeCast implements IOpenCast {

	private IOpenClass fromClass;
	private IOpenClass toClass;
	
	public AliasToTypeCast(IOpenClass from, IOpenClass to) {
		this.fromClass = from;
		this.toClass = to;
	}

	public Object convert(Object from) {
		
		IDomain domain = fromClass.getDomain();

		// Try to get given object from type domain. If object belongs to domain
		// true value
		// ill be returned; false - otherwise.
		// NOTE: EnumDomain implementation of IDomain (used by alias types)
		// throws runtime exception if object doesn't belong to domain.
		//
		boolean isInDomain = domain.selectObject(from);

		// If object doesn't belong to domain throw runtime exception with
		// appropriate message.
		//
		if (!isInDomain) {
			throw new RuntimeException("Object " + from
					+ " is outside of a valid domain");
		}

		// Return object as a converted value.
		//
		return from;
	}

	public int getDistance(IOpenClass from, IOpenClass to) {
		return 0;
	}

	public boolean isImplicit() {
		return true;
	}

}
