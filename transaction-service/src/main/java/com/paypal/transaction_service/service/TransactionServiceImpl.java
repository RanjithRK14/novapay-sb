package com.paypal.transaction_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.transaction_service.kafka.KafkaEventProducer;
import com.paypal.transaction_service.model.Transaction;
import com.paypal.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaEventProducer kafkaEventProducer;
    private final RestTemplate restTemplate;

    private static final String WALLET_URL = "http://localhost:8083/api/v1/wallets";

    @Override
    public Transaction createTransaction(Transaction request) {

        System.out.println("🚀 createTransaction() senderId=" + request.getSenderId()
                + " receiverId=" + request.getReceiverId()
                + " amount=" + request.getAmount());

        Long senderId   = request.getSenderId();
        Long receiverId = request.getReceiverId();
        Long amount     = request.getAmount();

        request.setStatus("PENDING");
        request.setTimestamp(LocalDateTime.now());

        Transaction saved = repository.save(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", senderId.toString());

        String holdReference = null;
        boolean captured = false;

        try {
            // Step 1: HOLD sender amount (amount as integer, no decimals)
            String holdJson = String.format(
                    "{\"userId\": %d, \"currency\": \"INR\", \"amount\": %d}",
                    senderId, amount);

            ResponseEntity<String> holdResp = restTemplate.postForEntity(
                    WALLET_URL + "/hold", new HttpEntity<>(holdJson, headers), String.class);

            JsonNode holdNode = objectMapper.readTree(holdResp.getBody());
            holdReference = holdNode.get("holdReference").asText();
            System.out.println("🛑 Hold placed: " + holdReference);

            // Step 2: Verify receiver wallet exists
            ResponseEntity<String> receiverResp = restTemplate.exchange(
                    WALLET_URL + "/" + receiverId,
                    HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!receiverResp.getStatusCode().is2xxSuccessful()) {
                tryReleaseHold(holdReference, headers);
                saved.setStatus("FAILED");
                return repository.save(saved);
            }

            // Step 3: CAPTURE (debit sender balance)
            String captureJson = String.format("{\"holdReference\":\"%s\"}", holdReference);
            ResponseEntity<String> captureResp = restTemplate.postForEntity(
                    WALLET_URL + "/capture", new HttpEntity<>(captureJson, headers), String.class);

            if (!captureResp.getStatusCode().is2xxSuccessful()) {
                tryReleaseHold(holdReference, headers);
                saved.setStatus("FAILED");
                return repository.save(saved);
            }

            captured = true;
            System.out.println("💸 Sender debited via capture");

            // Step 4: CREDIT receiver
            String creditJson = String.format(
                    "{\"userId\": %d, \"currency\": \"INR\", \"amount\": %d}",
                    receiverId, amount);

            try {
                ResponseEntity<String> creditResp = restTemplate.postForEntity(
                        WALLET_URL + "/credit", new HttpEntity<>(creditJson, headers), String.class);

                if (!creditResp.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Receiver credit failed");
                }
                System.out.println("💰 Receiver credited");

            } catch (Exception ex) {
                // Credit failed — refund sender
                System.out.println("❌ Credit failed → refunding sender");
                String refundJson = String.format(
                        "{\"userId\": %d, \"currency\": \"INR\", \"amount\": %d}",
                        senderId, amount);
                restTemplate.postForEntity(
                        WALLET_URL + "/credit", new HttpEntity<>(refundJson, headers), String.class);
                saved.setStatus("FAILED");
                return repository.save(saved);
            }

            saved.setStatus("SUCCESS");
            saved = repository.save(saved);

        } catch (HttpClientErrorException ex) {
            System.out.println("❌ Wallet HTTP error: " + ex.getStatusCode()
                    + " — " + ex.getResponseBodyAsString());
            if (holdReference != null && !captured) {
                tryReleaseHold(holdReference, headers);
            }
            saved.setStatus("FAILED");
            saved = repository.save(saved);

        } catch (Exception ex) {
            System.out.println("❌ Unexpected error: " + ex.getMessage());
            if (holdReference != null && !captured) {
                tryReleaseHold(holdReference, headers);
            }
            saved.setStatus("FAILED");
            saved = repository.save(saved);
        }

        // Kafka — non-blocking, failure does not affect response
        try {
            kafkaEventProducer.sendTransactionEvent(String.valueOf(saved.getId()), saved);
        } catch (Exception e) {
            System.out.println("⚠️ Kafka send failed (non-critical): " + e.getMessage());
        }

        return saved;
    }

    private void tryReleaseHold(String holdReference, HttpHeaders headers) {
        try {
            restTemplate.postForEntity(
                    WALLET_URL + "/release/" + holdReference,
                    new HttpEntity<>(headers), String.class);
            System.out.println("🔓 Hold released: " + holdReference);
        } catch (Exception e) {
            System.out.println("⚠️ Failed to release hold: " + e.getMessage());
        }
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public List<Transaction> getTransactionsByUser(Long userId) {
        return repository.findBySenderIdOrReceiverId(userId, userId);
    }
}