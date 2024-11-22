package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.persistence.document.Account;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
@Configuration
public class AuditService implements CommandLineRunner {
  private final ReactiveMongoTemplate reactiveMongoTemplate;
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
              System.out.println("Change event: " + change);
              int stop = 0;
            })
            .blockLast();
  }

    @Override
    public void run(String... args) throws Exception {
        taskExecutor.execute(this::listenToChanges);
    }
}
