package com.github.ngodat0103.usersvc.service;

import com.github.ngodat0103.usersvc.persistence.document.Account;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuditService implements ApplicationListener<ApplicationReadyEvent> {
  private ReactiveMongoTemplate reactiveMongoTemplate;
  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    listenToChanges();
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
              System.out.println("Change event: " + change);
              int stop = 0;
            })
        .blockLast();
  }
}
