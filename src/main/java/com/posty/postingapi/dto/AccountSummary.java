package com.posty.postingapi.dto;

import com.posty.postingapi.domain.account.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class AccountSummary {

    private Long id;

    private String name;

    public AccountSummary(Account writer) {
        this(writer.getId(), writer.getName());
    }
}
