package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.persistence.document.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
@Configuration
public class AuditService implements CommandLineRunner {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
//  @Qualifier(SimpleAsyncTaskExecutor.class)
  private final TaskExecutor taskExecutor;

  public AuditService(ReactiveMongoTemplate reactiveMongoTemplate) {
    this.reactiveMongoTemplate = reactiveMongoTemplate;
    this.taskExecutor = new SimpleAsyncTaskExecutor();
  }

  public void listenToChanges() {
    reactiveMongoTemplate
        .changeStream(Account.class)
        .watchCollection("account")
        .listen()
            .doOnSubscribe(s -> log.info("Subscribed to changes on 'account' collection..."))
        .doOnNext(
            change -> {
              Account account = change.getBody();
                Assert.notNull(account, "Account must not be null");
                account.setPassword("********");
              log.info("Change event: {}", change);
            })
            .doOnTerminate(() -> Thread.currentThread().interrupt())
            .blockLast();
  }

    @Override
    public void run(String... args) {
        taskExecutor.execute(this::listenToChanges);
    }
}
