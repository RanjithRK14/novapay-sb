package com.paypal.user_service.service;

import com.paypal.user_service.client.WalletClient;
import com.paypal.user_service.dto.CreateWalletRequest;
import com.paypal.user_service.model.User;
import com.paypal.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final WalletClient walletClient;

    @Override
    public User createUser(User user) {

        User savedUser = userRepository.save(user);

        try {
            CreateWalletRequest request = new CreateWalletRequest();
            request.setUserId(savedUser.getId());
            request.setCurrency("INR");

            walletClient.createWallet(request);
            System.out.println("💰 Wallet created for user: " + savedUser.getId());

        } catch (Exception ex) {
            // Log the wallet error but DO NOT roll back the user or throw exception.
            // Wallet creation may fail if wallet-service is sleeping (Render free tier cold start).
            // The user is already registered — wallet will be created on first login or wallet access.
            System.err.println("⚠️ Wallet creation failed for user " + savedUser.getId()
                    + " — user registration still successful. Error: " + ex.getMessage());
        }

        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
