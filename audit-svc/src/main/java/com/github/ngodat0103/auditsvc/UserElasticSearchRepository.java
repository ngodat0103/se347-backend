package com.github.ngodat0103.auditsvc;

import com.github.ngodat0103.auditsvc.dto.AccountDto;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;

public interface UserElasticSearchRepository
    extends ReactiveElasticsearchRepository<AccountDto, String> {}
