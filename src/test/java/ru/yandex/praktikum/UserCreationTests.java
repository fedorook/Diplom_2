package ru.yandex.praktikum;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.praktikum.config.RestConfig;
import ru.yandex.praktikum.model.User;
import ru.yandex.praktikum.steps.UserSteps;

import static org.hamcrest.Matchers.equalTo;

public class UserCreationTests {

    private UserSteps userSteps = new UserSteps();
    private Faker faker = new Faker();
    private String token;

    @Before
    public void setUp() {
        RestConfig.init();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @After
    public void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @Test
    @Step("Create a unique user")
    public void createUniqueUser() {
        User user = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());

        ValidatableResponse response = userSteps.createUser(user);
        token = userSteps.extractToken(response);  // Extract token using UserSteps method
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @Step("Create a user that is already registered")
    public void createUserAlreadyRegistered() {
        User user = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());

        // Create the user first
        userSteps.createUser(user);

        // Try to create the same user again
        ValidatableResponse response = userSteps.createUser(user);
        response.statusCode(403).body("message", equalTo("User already exists"));
    }

    @Test
    @Step("Create a user without a required field")
    public void createUserWithoutRequiredField() {
        User user = new User("", faker.internet().password(), "");

        ValidatableResponse response = userSteps.createUser(user);
        response.statusCode(403).body("message", equalTo("Email, password and name are required fields"));
    }
}
