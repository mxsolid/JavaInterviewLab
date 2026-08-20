package com.javainterviewlab.system.seed.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.repository.SeedMapper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Service
public class SeedImportService {
    private final ObjectMapper objectMapper; private final SeedMapper seedMapper;
    public SeedImportService(ObjectMapper objectMapper, SeedMapper seedMapper) { this.objectMapper=objectMapper; this.seedMapper=seedMapper; }
    @Transactional
    public SeedImportResponse importJson(Resource resource) {
        try {
            JsonNode root=objectMapper.readTree(resource.getInputStream()); String pack=required(root,"seedPack"); String version=required(root,"version"); int created=0,skipped=0;
            Iterator<Map.Entry<String,JsonNode>> categories=root.path("categories").fields(); while(categories.hasNext()){Map.Entry<String,JsonNode> item=categories.next(); seedMapper.upsertCategory(item.getKey(),item.getValue().asText());}
            for(JsonNode topic:root.path("topics")){Long categoryId=seedMapper.findCategoryId(required(topic,"category")); if(categoryId==null) throw new BusinessException(ApiErrorCode.VALIDATION_FAILED,"专题引用的分类不存在"); seedMapper.upsertTopic(categoryId,required(topic,"externalKey"),required(topic,"name"),topic.path("starLevel").asInt(3));}
            for(JsonNode question:root.path("questions")) { String externalKey=required(question,"externalKey"); if(seedMapper.questionExists(externalKey)>0){skipped++;continue;} Long topicId=seedMapper.findTopicId(required(question,"topic")); if(topicId==null) throw new BusinessException(ApiErrorCode.VALIDATION_FAILED,"题目引用的专题不存在"); Long id=seedMapper.insertQuestion(topicId,externalKey,required(question,"title"),question.path("starLevel").asInt(3),required(question,"difficulty"),required(question,"frequencyLevel"),question.path("oneLiner").asText(null),question.path("plainExplanation").asText(null),question.path("designReason").asText(null)); int order=0; Iterator<Map.Entry<String,JsonNode>> answers=question.path("answers").fields(); while(answers.hasNext()){Map.Entry<String,JsonNode> answer=answers.next();seedMapper.insertAnswer(id,answer.getKey(),answer.getValue().asText(),order++);} order=0; for(JsonNode followUp:question.path("followUps")) seedMapper.insertFollowUp(id,followUp.asText(),order++); created++; }
            seedMapper.markImported(pack,version); return new SeedImportResponse(pack,version,created,skipped);
        } catch(IOException exception) { throw new BusinessException(ApiErrorCode.REQUEST_BODY_INVALID,"种子文件无法读取"); }
    }
    private String required(JsonNode node,String field) { String value=node.path(field).asText(); if(value==null||value.isBlank()) throw new BusinessException(ApiErrorCode.VALIDATION_FAILED,"种子字段缺失："+field); return value; }
}
