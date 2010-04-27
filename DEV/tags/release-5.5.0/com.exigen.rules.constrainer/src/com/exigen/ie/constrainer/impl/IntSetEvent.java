package com.exigen.ie.constrainer.impl;

import com.exigen.ie.constrainer.EventOfInterest;
import com.exigen.ie.constrainer.IntSetVar;
import com.exigen.ie.tools.Reusable;
import com.exigen.ie.tools.ReusableFactory;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author unascribed
 * @version 1.0
 */

public class IntSetEvent extends EventOfInterest {

    public interface IntSetEventConstants {
        int REMOVE = 1;
        int REQUIRE = 2;
        int VALUE = 4;
        int ALL = 8;
    }

    static ReusableFactory _factory = new ReusableFactory() {
        @Override
        protected Reusable createNewElement() {
            return new IntSetEvent();
        }
    };
    private IntSetVar _var;
    private int _val;

    private int _type;

    static IntSetEvent getEvent(IntSetVar var, int val, int type) {
        IntSetEvent ev = (IntSetEvent) _factory.getElement();
        ev.init(var, val, type);
        return ev;
    }

    void init(IntSetVar var, int val, int type) {
        _var = var;
        _val = val;
        _type = type;
    }

    @Override
    public boolean isMaxEvent() {
        return false;
    }

    @Override
    public boolean isMinEvent() {
        return false;
    }

    @Override
    public boolean isRemoveEvent() {
        return (_type & IntSetEventConstants.REMOVE) != 0;
    }

    public boolean isRequireEvent() {
        return (_type & IntSetEventConstants.REQUIRE) != 0;
    }

    @Override
    public boolean isValueEvent() {
        return (_type & IntSetEventConstants.VALUE) != 0;
    }

    @Override
    public String name() {
        return "IntSetEvent";
    }

    @Override
    public int type() {
        return _type;
    }
}