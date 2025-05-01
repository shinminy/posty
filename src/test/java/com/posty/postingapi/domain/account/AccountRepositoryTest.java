package com.posty.postingapi.domain.account;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findAccount() {
        String email = "abc@gmail.com";

        Optional<Account> accountOptional = accountRepository.findByEmail(email);

        assertThat(accountOptional).isPresent();
        assertThat(accountOptional.get().getEmail()).isEqualTo(email);
    }
}
