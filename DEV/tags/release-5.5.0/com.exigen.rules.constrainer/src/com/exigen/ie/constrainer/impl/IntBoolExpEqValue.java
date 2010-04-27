package com.exigen.ie.constrainer.impl;

import java.util.Map;

import com.exigen.ie.constrainer.EventOfInterest;
import com.exigen.ie.constrainer.Failure;
import com.exigen.ie.constrainer.IntExp;
import com.exigen.ie.constrainer.NonLinearExpression;
import com.exigen.ie.constrainer.Observer;
import com.exigen.ie.constrainer.Subject;

/**
 * An implementation of the expression: <code>(IntExp == value)</code>.
 */
public final class IntBoolExpEqValue extends IntBoolExpForSubject {
    final class ObserverMinMax extends Observer {
        @Override
        public Object master() {
            return IntBoolExpEqValue.this;
        }

        @Override
        public int subscriberMask() {
            return ALL;
        }

        @Override
        public void update(Subject exp, EventOfInterest interest) throws Failure {
            setDomainMinMax();
        }

    } // ~ObserverMinMax
    private IntExp _exp;

    private int _value;

    public IntBoolExpEqValue(IntExp exp, int value) {
        super(exp.constrainer());

        _exp = exp;
        _value = value;

        if (constrainer().showInternalNames()) {
            _name = "(" + exp.name() + "==" + value + ")";
        }

        setDomainMinMaxSafe();

        _exp.attachObserver(new ObserverMinMax());
    }

    @Override
    public double calcCoeffs(Map map, double factor) throws NonLinearExpression {
        return (_exp.neg().add(_value)).calcCoeffs(map, factor);
    }

    @Override
    public boolean isLinear() {
        return _exp.isLinear();
    }

    @Override
    protected boolean isSubjectFalse() {
        return !_exp.contains(_value);
    }

    @Override
    protected boolean isSubjectTrue() {
        return _exp.min() == _value && _exp.max() == _value;
    }

    @Override
    protected void setSubjectFalse() throws Failure {
        _exp.removeValue(_value);
    }

    @Override
    protected void setSubjectTrue() throws Failure {
        _exp.setValue(_value);
    }

    // public Constraint asConstraint()
    // {
    // return _exp.equals(_value);
    // }

} // ~IntBoolExpEqValue
