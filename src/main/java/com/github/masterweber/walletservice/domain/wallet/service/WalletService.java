package com.github.masterweber.walletservice.domain.wallet.service;

import com.github.masterweber.walletservice.domain.wallet.model.OperationType;
import com.github.masterweber.walletservice.domain.wallet.model.request.WalletOperationRequest;
import com.github.masterweber.walletservice.domain.wallet.exception.InsufficientFundsException;
import com.github.masterweber.walletservice.domain.wallet.exception.WalletNotFoundException;
import com.github.masterweber.walletservice.domain.wallet.entity.Wallet;
import com.github.masterweber.walletservice.domain.wallet.repository.WalletRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletService {

    WalletRepository walletRepository;

    @Transactional
    public Wallet createWallet(Long initialBalance) {
        Wallet wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .balance(initialBalance != null ? initialBalance : 0L)
                .build();
        return walletRepository.save(wallet);
    }

    @Transactional
    public void processOperation(WalletOperationRequest request) {
        Wallet wallet = walletRepository.findWalletById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + request.getWalletId()));

        if (request.getOperationType() == OperationType.DEPOSIT) {
            wallet.setBalance(wallet.getBalance() + request.getAmount());
        } else {
            if (wallet.getBalance() < request.getAmount()) {
                throw new InsufficientFundsException("Insufficient funds in wallet: " + request.getWalletId());
            }
            wallet.setBalance(wallet.getBalance() - request.getAmount());
        }

        walletRepository.save(wallet);
    }

    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        return walletRepository.findById(walletId)
                .map(Wallet::getBalance)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " + walletId));
    }

}
