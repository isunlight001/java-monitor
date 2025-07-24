package com.acme.monitor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class ArrayEqualsJsonSchemaDemo {
    public static void main(String[] args) throws Exception {
        // 目标数组
        String expectedJson = "[1,2,3]";
        // 测试数组
        String testJson = "[1,2,3]"; // 改成 [1,3,2] 看看效果

        // 构造 schema
        String schemaStr = "{\n" +
                "  \"type\": \"array\",\n" +
                "  \"items\": [\n" +
                "    { \"const\": 1 },\n" +
                "    { \"const\": 2 },\n" +
                "    { \"const\": 3 }\n" +
                "  ],\n" +
                "  \"minItems\": 3,\n" +
                "  \"maxItems\": 3\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode schemaNode = mapper.readTree(schemaStr);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema schema = factory.getSchema(schemaNode);

        JsonNode testNode = mapper.readTree(testJson);
        Set<ValidationMessage> errors = schema.validate(testNode);
        if (errors.isEmpty()) {
            System.out.println("数组一致，校验通过");
        } else {
            System.out.println("数组不一致，校验失败：" + errors);
        }
    }
} 