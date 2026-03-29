package com.paypal.reward_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paypal.reward_service.model.Reward;
import com.paypal.reward_service.model.Transaction;
import com.paypal.reward_service.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RewardConsumer {

    private final RewardRepository rewardRepository;
    private final ObjectMapper objectMapper;

    // @KafkaListener temporarily disabled — Kafka not available on free tier
    // Re-enable when Kafka provider is configured:
    // @KafkaListener(topics = "txn-initiated", groupId = "reward-group")
    public void consumerTransaction(byte[] message) {
        // Kafka disabled — rewards are calculated via direct API calls instead
    }
}