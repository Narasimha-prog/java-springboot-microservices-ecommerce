package com.srinu.chatbot_service.repository;

import com.srinu.chatbot_service.dto.ChromaEmbeddingsRequest;
import com.srinu.chatbot_service.kafka.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ChromaVectorRepository {
    private final VectorStore vectorStore;


    public void indexProductKnowledge(ProductCreatedEvent event) {

        String content = """
                Product Name: %s
                SKU Identifier: %s
                Category Classification: %s
                Pricing: %s
                Description Profile: %s
                """
                .formatted(
                        event.name(),
                        event.sku(),
                        event.categoryName(),
                        event.price(),
                        event.description()
                );

        Document document = Document.builder()
                .id(event.productId().toString())
                .text(content)
                .metadata("productId", event.productId().toString())
                .metadata("sku", event.sku())
                .metadata("category", event.categoryName())
                .metadata("price", event.price().doubleValue())
                .build();

        vectorStore.add(List.of(document));
    }
    public List<Document> searchSimilarDocuments(String queryText, int topK) {
        log.info("Executing similarity search against vector store for query: {}", queryText);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(queryText)
                .topK(topK)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}
