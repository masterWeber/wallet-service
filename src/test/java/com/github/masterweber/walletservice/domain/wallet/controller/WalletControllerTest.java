package com.github.masterweber.walletservice.domain.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.masterweber.walletservice.domain.wallet.model.OperationType;
import com.github.masterweber.walletservice.domain.wallet.model.request.WalletCreateRequest;
import com.github.masterweber.walletservice.domain.wallet.model.request.WalletOperationRequest;
import com.github.masterweber.walletservice.domain.wallet.model.response.WalletResponse;
import com.github.masterweber.walletservice.domain.wallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WalletControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WalletRepository walletRepository;

    @AfterEach
    void tearDown() {
        walletRepository.deleteAll();
    }

    @Test
    void createWallet_ShouldReturnNewWallet() throws Exception {
        WalletCreateRequest request = WalletCreateRequest.builder()
                .balance(1000L)
                .build();

        mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", notNullValue()))
                .andExpect(jsonPath("$.balance", is(1000)));
    }

    @Test
    void getBalance_ShouldReturnCurrentBalance() throws Exception {
        // First create a wallet
        WalletCreateRequest createRequest = WalletCreateRequest.builder()
                .balance(5000L)
                .build();

        MvcResult createResult = mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn();

        WalletResponse walletResponse = objectMapper.readValue(createResult.getResponse().getContentAsString(), WalletResponse.class);
        UUID walletId = walletResponse.getWalletId();

        // Then get balance
        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId", is(walletId.toString())))
                .andExpect(jsonPath("$.balance", is(5000)));
    }

    @Test
    void getBalance_NotFound_ShouldReturn404() throws Exception {
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/wallets/{walletId}", randomId))
                .andExpect(status().isNotFound());
    }

    @Test
    void processOperation_Deposit_ShouldIncreaseBalance() throws Exception {
        // Create wallet
        WalletCreateRequest createRequest = WalletCreateRequest.builder()
                .balance(1000L)
                .build();
        MvcResult createResult = mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        UUID walletId = objectMapper.readValue(createResult.getResponse().getContentAsString(), WalletResponse.class).getWalletId();

        // Deposit
        WalletOperationRequest depositRequest = new WalletOperationRequest();
        depositRequest.setWalletId(walletId);
        depositRequest.setOperationType(OperationType.DEPOSIT);
        depositRequest.setAmount(500L);

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        // Check balance
        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(1500)));
    }

    @Test
    void processOperation_Withdraw_ShouldDecreaseBalance() throws Exception {
        // Create wallet
        WalletCreateRequest createRequest = WalletCreateRequest.builder()
                .balance(1000L)
                .build();
        MvcResult createResult = mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        UUID walletId = objectMapper.readValue(createResult.getResponse().getContentAsString(), WalletResponse.class).getWalletId();

        // Withdraw
        WalletOperationRequest withdrawRequest = new WalletOperationRequest();
        withdrawRequest.setWalletId(walletId);
        withdrawRequest.setOperationType(OperationType.WITHDRAW);
        withdrawRequest.setAmount(400L);

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SUCCESS")));

        // Check balance
        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(600)));
    }

    @Test
    void processOperation_WithdrawInsufficientFunds_ShouldReturn400() throws Exception {
        // Create wallet
        WalletCreateRequest createRequest = WalletCreateRequest.builder()
                .balance(100L)
                .build();
        MvcResult createResult = mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        UUID walletId = objectMapper.readValue(createResult.getResponse().getContentAsString(), WalletResponse.class).getWalletId();

        // Withdraw too much
        WalletOperationRequest withdrawRequest = new WalletOperationRequest();
        withdrawRequest.setWalletId(walletId);
        withdrawRequest.setOperationType(OperationType.WITHDRAW);
        withdrawRequest.setAmount(500L);

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(withdrawRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void processOperation_InvalidRequest_ShouldReturn400() throws Exception {
        // Amount is required and must be positive
        WalletOperationRequest invalidRequest = new WalletOperationRequest();
        invalidRequest.setWalletId(UUID.randomUUID());
        invalidRequest.setOperationType(OperationType.DEPOSIT);
        invalidRequest.setAmount(-100L);

        mockMvc.perform(post("/api/v1/wallet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processOperation_HighConcurrency_ShouldHandle1000Requests() throws Exception {
        // Create wallet
        WalletCreateRequest createRequest = WalletCreateRequest.builder()
                .balance(0L)
                .build();
        MvcResult createResult = mockMvc.perform(post("/api/v1/wallets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn();
        UUID walletId = objectMapper.readValue(createResult.getResponse().getContentAsString(), WalletResponse.class).getWalletId();

        int totalOperations = 1000;
        int numberOfThreads = 200; // More aggressive concurrency
        long amountPerOperation = 1L;

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(totalOperations);

        for (int i = 0; i < totalOperations; i++) {
            executor.submit(() -> {
                try {
                    WalletOperationRequest depositRequest = new WalletOperationRequest();
                    depositRequest.setWalletId(walletId);
                    depositRequest.setOperationType(OperationType.DEPOSIT);
                    depositRequest.setAmount(amountPerOperation);

                    mockMvc.perform(post("/api/v1/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(depositRequest)))
                            .andExpect(status().isOk());
                } catch (Exception e) {
                    System.err.println("[DEBUG_LOG] Request failed: " + e.getMessage());
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finished = latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        
        if (!finished) {
            System.err.println("[DEBUG_LOG] Test timed out");
        }

        // Check final balance: 1000 ops * 1 amount = 1000
        mockMvc.perform(get("/api/v1/wallets/{walletId}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(1000)));
    }
}
