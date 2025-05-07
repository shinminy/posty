package com.posty.postingapi.dto;

import com.posty.postingapi.domain.account.Account;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AccountSummary {

    private Long id;

    private String name;

    public AccountSummary(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public AccountSummary(Account writer) {
        this(writer.getId(), writer.getName());
    }
}
