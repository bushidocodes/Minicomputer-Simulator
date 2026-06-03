package com.simulator.awesome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConditionCodeTest {

    private ConditionCode cc;

    @BeforeEach
    void setUp() {
        cc = new ConditionCode();
    }

    @Test
    void isCondition_0_reportsOverflow() {
        cc.setOverflow(true);
        assertTrue(cc.isCondition(0));
    }

    @Test
    void isCondition_0_clearWhenOverflowClear() {
        cc.setOverflow(false);
        assertFalse(cc.isCondition(0));
    }

    @Test
    void isCondition_1_reportsUnderflow() {
        cc.setUnderflow(true);
        assertTrue(cc.isCondition(1));
    }

    @Test
    void isCondition_2_reportsDivideByZero() {
        cc.setDivideByZero(true);
        assertTrue(cc.isCondition(2));
    }

    @Test
    void isCondition_3_reportsEqual() {
        cc.setEqual(true);
        assertTrue(cc.isCondition(3));
    }

    // issue #112: case 4 (greater-than) was missing and always returned false
    @Test
    void isCondition_4_reportsGreaterThan() {
        cc.setGreaterThan(true);
        assertTrue(cc.isCondition(4),
            "isCondition(4) must return true when the greater-than flag is set");
    }

    @Test
    void isCondition_4_clearWhenGreaterThanClear() {
        cc.setGreaterThan(false);
        assertFalse(cc.isCondition(4),
            "isCondition(4) must return false when the greater-than flag is clear");
    }

    @Test
    void isCondition_unknownCode_returnsFalse() {
        cc.setOverflow(true);
        cc.setUnderflow(true);
        cc.setDivideByZero(true);
        cc.setEqual(true);
        cc.setGreaterThan(true);
        assertFalse(cc.isCondition(99));
    }
}
