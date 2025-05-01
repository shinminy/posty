package com.posty.postingapi.dto;

import com.posty.postingapi.domain.account.Account;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SimpleAccount {

    private Long id;

    private String name;

    public SimpleAccount(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SimpleAccount(Account writer) {
        this(writer.getId(), writer.getName());
    }
}
