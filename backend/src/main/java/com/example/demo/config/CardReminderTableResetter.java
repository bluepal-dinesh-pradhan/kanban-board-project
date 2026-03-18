package com.example.demo.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.reminders.reset-table-on-startup", havingValue = "true")
public class CardReminderTableResetter implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        log.warn("Resetting data in card_reminders due to app.reminders.reset-table-on-startup=true");
        jdbcTemplate.execute("TRUNCATE TABLE IF EXISTS card_reminders RESTART IDENTITY");
    }
}
