package com.acme.monitor.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ArrayEqualsUtil工具类的测试类
 */
public class ArrayEqualsUtilTest {

    /**
     * 测试两个相等的整数数组
     */
    @Test
    public void testEqualIntegerArrays() throws Exception {
        Object[] expected = {1, 2, 3};
        Object[] test = {1, 2, 3};
        
        assertTrue(ArrayEqualsUtil.arraysEqual(expected, test), 
            "两个相同的整数数组应该被认为是相等的");
    }

    /**
     * 测试两个顺序不同的整数数组
     */
    @Test
    public void testDifferentOrderIntegerArrays() throws Exception {
        Object[] expected = {1, 2, 3};
        Object[] test = {1, 3, 2};
        
        assertFalse(ArrayEqualsUtil.arraysEqual(expected, test), 
            "顺序不同的整数数组应该被认为是不相等的");
    }

    /**
     * 测试两个相等的字符串数组
     */
    @Test
    public void testEqualStringArrays() throws Exception {
        Object[] expected = {"apple", "banana", "cherry"};
        Object[] test = {"apple", "banana", "cherry"};
        
        assertTrue(ArrayEqualsUtil.arraysEqual(expected, test), 
            "两个相同的字符串数组应该被认为是相等的");
    }

    /**
     * 测试两个不同的字符串数组
     */
    @Test
    public void testDifferentStringArrays() throws Exception {
        Object[] expected = {"apple", "banana", "cherry"};
        Object[] test = {"apple", "cherry", "banana"};
        
        assertFalse(ArrayEqualsUtil.arraysEqual(expected, test), 
            "内容不同或顺序不同的字符串数组应该被认为是不相等的");
    }

    /**
     * 测试两个相等的整数基本类型数组
     */
    @Test
    public void testEqualPrimitiveIntArrays() throws Exception {
        int[] expected = {1, 2, 3};
        int[] test = {1, 2, 3};
        
        assertTrue(ArrayEqualsUtil.intArraysEqual(expected, test), 
            "两个相同的整数基本类型数组应该被认为是相等的");
    }

    /**
     * 测试两个顺序不同的整数基本类型数组
     */
    @Test
    public void testDifferentOrderPrimitiveIntArrays() throws Exception {
        int[] expected = {1, 2, 3};
        int[] test = {1, 3, 2};
        
        assertFalse(ArrayEqualsUtil.intArraysEqual(expected, test), 
            "顺序不同的整数基本类型数组应该被认为是不相等的");
    }
}