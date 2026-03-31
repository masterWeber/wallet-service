package com.github.masterweber.walletservice.domain.wallet.controller;

import com.github.masterweber.walletservice.domain.wallet.entity.Wallet;
import com.github.masterweber.walletservice.domain.wallet.mapper.WalletMapper;
import com.github.masterweber.walletservice.domain.wallet.model.OperationStatus;
import com.github.masterweber.walletservice.domain.wallet.model.request.WalletCreateRequest;
import com.github.masterweber.walletservice.domain.wallet.model.request.WalletOperationRequest;
import com.github.masterweber.walletservice.domain.wallet.model.response.OperationResponse;
import com.github.masterweber.walletservice.domain.wallet.model.response.WalletResponse;
import com.github.masterweber.walletservice.domain.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WalletController {

    WalletService walletService;
    WalletMapper walletMapper;

    @PostMapping("/wallet")
    public ResponseEntity<OperationResponse> processOperation(@Valid @RequestBody WalletOperationRequest request) {
        walletService.processOperation(request);
        return ResponseEntity.ok(new OperationResponse(OperationStatus.SUCCESS));
    }

    @PostMapping("/wallets")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody WalletCreateRequest request) {
        Wallet wallet = walletService.createWallet(request.getBalance());
        return ResponseEntity.ok(walletMapper.toResponse(wallet));
    }

    @GetMapping("/wallets/{walletId}")
    public ResponseEntity<WalletResponse> getBalance(@PathVariable UUID walletId) {
        Long balance = walletService.getBalance(walletId);
        return ResponseEntity.ok(WalletResponse.builder()
                .walletId(walletId)
                .balance(balance)
                .build());
    }

}
