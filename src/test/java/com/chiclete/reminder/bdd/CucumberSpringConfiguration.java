package com.chiclete.reminder.bdd;

import com.chiclete.reminder.ReminderApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@CucumberContextConfiguration
@SpringBootTest(
        classes = ReminderApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(ReminderApiSteps.class)
public class CucumberSpringConfiguration {
}
