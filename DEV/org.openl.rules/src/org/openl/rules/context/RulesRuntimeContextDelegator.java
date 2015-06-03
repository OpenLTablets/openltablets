package org.openl.rules.context;

import org.openl.runtime.IRuntimeContext;


/**
 * Runtime context delegator.
 * 
 * @author PUdalau
 */
public class RulesRuntimeContextDelegator extends DefaultRulesRuntimeContext {

    private IRulesRuntimeContext delegate;

    public RulesRuntimeContextDelegator(IRulesRuntimeContext delegate) {
        this.delegate = delegate;
    }

    public Object getValue(String name) {
        Object value = super.getValue(name);
        if(value == null){
            value = delegate.getValue(name) ;
        }
        return value;
    }

    @Override
    public synchronized String toString() {
        return super.toString()+ "Delegated context:" + delegate.toString();
    }
    // <<< INSERT >>>
	@Override
	public java.util.Date getCurrentDate() {
	    if (super.getCurrentDate() == null){
			return delegate.getCurrentDate();
		}
		return super.getCurrentDate();
	}
	@Override
	public java.util.Date getRequestDate() {
	    if (super.getRequestDate() == null){
			return delegate.getRequestDate();
		}
		return super.getRequestDate();
	}
	@Override
	public java.lang.String getLob() {
	    if (super.getLob() == null){
			return delegate.getLob();
		}
		return super.getLob();
	}
	@Override
	public org.openl.rules.enumeration.UsStatesEnum getUsState() {
	    if (super.getUsState() == null){
			return delegate.getUsState();
		}
		return super.getUsState();
	}
	@Override
	public org.openl.rules.enumeration.CountriesEnum getCountry() {
	    if (super.getCountry() == null){
			return delegate.getCountry();
		}
		return super.getCountry();
	}
	@Override
	public org.openl.rules.enumeration.UsRegionsEnum getUsRegion() {
	    if (super.getUsRegion() == null){
			return delegate.getUsRegion();
		}
		return super.getUsRegion();
	}
	@Override
	public org.openl.rules.enumeration.CurrenciesEnum getCurrency() {
	    if (super.getCurrency() == null){
			return delegate.getCurrency();
		}
		return super.getCurrency();
	}
	@Override
	public org.openl.rules.enumeration.LanguagesEnum getLang() {
	    if (super.getLang() == null){
			return delegate.getLang();
		}
		return super.getLang();
	}
	@Override
	public org.openl.rules.enumeration.RegionsEnum getRegion() {
	    if (super.getRegion() == null){
			return delegate.getRegion();
		}
		return super.getRegion();
	}
// <<< END INSERT >>>

}
