package com.github.ngodat0103.se347_backend.service.account;

import com.github.ngodat0103.se347_backend.dto.account.AccountDto;
import com.github.ngodat0103.se347_backend.service.BaseService;
import jakarta.servlet.http.HttpServletRequest;

public interface AccountService extends BaseService<AccountDto> {
  AccountDto create(AccountDto accountDto);

  AccountDto create(AccountDto accountDto, HttpServletRequest request);

  AccountDto update(AccountDto accountDto);

  AccountDto getMe();
}
