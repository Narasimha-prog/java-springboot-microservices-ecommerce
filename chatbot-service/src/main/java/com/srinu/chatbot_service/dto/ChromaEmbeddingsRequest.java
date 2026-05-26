package com.srinu.chatbot_service.dto;

import java.util.List;
import java.util.Map;

public record ChromaEmbeddingsRequest(
        List<String> ids,
        List<String> documents,
        List<Map<String, Object>> metadatas
) {
}
