package com.github.ngodat0103.se347_backend.dto.mapper;

import com.github.ngodat0103.se347_backend.dto.account.AccountDto;
import com.github.ngodat0103.se347_backend.persistence.document.account.Account;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
  AccountDto toDto(Account account);

  Account toDocument(AccountDto dto);
}
