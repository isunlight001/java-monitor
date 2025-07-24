package com.acme.monitor.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

/**
 * 数组相等性校验工具类
 * 
 * 该工具类使用JSON Schema来校验两个数组是否完全相等，
 * 包括元素顺序和值都必须一致。
 */
public class ArrayEqualsUtil {
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    
    /**
     * 比较两个数组是否相等
     * 
     * 该方法通过将期望数组转换为JSON Schema，然后使用该Schema验证测试数组，
     * 确保两个数组在元素顺序和值上都完全一致。
     * 
     * @param expected 期望的数组对象
     * @param test 待测试的数组对象
     * @return 如果两个数组相等返回true，否则返回false
     * @throws Exception 如果在处理JSON过程中发生错误
     */
    public static boolean arraysEqual(Object[] expected, Object[] test) throws Exception {
        // 将数组转换为JSON字符串
        String expectedJson = MAPPER.writeValueAsString(expected);
        String testJson = MAPPER.writeValueAsString(test);
        
        // 构造针对期望数组的JSON Schema
        String schemaStr = buildSchemaFromJson(expectedJson);
        
        // 解析Schema
        JsonNode schemaNode = MAPPER.readTree(schemaStr);
        JsonSchema schema = SCHEMA_FACTORY.getSchema(schemaNode);
        
        // 验证测试数组是否符合Schema
        JsonNode testNode = MAPPER.readTree(testJson);
        Set<ValidationMessage> errors = schema.validate(testNode);
        
        // 如果没有验证错误，则数组相等
        return errors.isEmpty();
    }
    
    /**
     * 根据JSON数组字符串构建JSON Schema
     * 
     * @param json JSON数组字符串
     * @return 对应的JSON Schema字符串
     */
    private static String buildSchemaFromJson(String json) throws Exception {
        // 解析JSON数组
        JsonNode arrayNode = MAPPER.readTree(json);
        
        // 构建Schema基础结构
        StringBuilder schemaBuilder = new StringBuilder();
        schemaBuilder.append("{\n");
        schemaBuilder.append("  \"type\": \"array\",\n");
        schemaBuilder.append("  \"items\": [\n");
        
        // 为数组中的每个元素添加const验证规则
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode element = arrayNode.get(i);
            schemaBuilder.append("    { \"const\": ");
            
            // 根据元素类型处理序列化
            if (element.isTextual()) {
                schemaBuilder.append("\"").append(element.asText()).append("\"");
            } else {
                schemaBuilder.append(element.toString());
            }
            
            schemaBuilder.append(" }");
            if (i < arrayNode.size() - 1) {
                schemaBuilder.append(",");
            }
            schemaBuilder.append("\n");
        }
        
        // 添加数组长度限制
        schemaBuilder.append("  ],\n");
        schemaBuilder.append("  \"minItems\": ").append(arrayNode.size()).append(",\n");
        schemaBuilder.append("  \"maxItems\": ").append(arrayNode.size()).append("\n");
        schemaBuilder.append("}");
        
        return schemaBuilder.toString();
    }
    
    /**
     * 比较两个整数数组是否相等
     * 
     * @param expected 期望的整数数组
     * @param test 待测试的整数数组
     * @return 如果两个数组相等返回true，否则返回false
     * @throws Exception 如果在处理JSON过程中发生错误
     */
    public static boolean intArraysEqual(int[] expected, int[] test) throws Exception {
        // 转换为对象数组
        Object[] expectedObjects = new Object[expected.length];
        Object[] testObjects = new Object[test.length];
        
        for (int i = 0; i < expected.length; i++) {
            expectedObjects[i] = expected[i];
        }
        
        for (int i = 0; i < test.length; i++) {
            testObjects[i] = test[i];
        }
        
        return arraysEqual(expectedObjects, testObjects);
    }
}