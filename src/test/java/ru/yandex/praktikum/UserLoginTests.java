package ru.yandex.praktikum;

import com.github.javafaker.Faker;
import io.qameta.allure.junit4.DisplayName;
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

public class UserLoginTests {

    private final UserSteps userSteps = new UserSteps();
    private final Faker faker = new Faker();
    private String token;

    private User existingUser;

    @Before
    public void setUp() {
        RestConfig.init();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter()); // Can omit if logging is not needed

        // Create an existing user for login tests
        existingUser = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse response = userSteps.createUser(existingUser);
        token = userSteps.extractToken(response);  // Extract token for user deletion after tests
    }

    @After
    public void tearDown() {
        if (token != null) {
            userSteps.deleteUser(token);
        }
    }

    @Test
    @DisplayName("Login with existing user")
    public void loginWithExistingUser() {
        ValidatableResponse response = userSteps.loginUser(existingUser.getEmail(), existingUser.getPassword());
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Login with invalid password")
    public void loginWithInvalidPassword() {
        ValidatableResponse response = userSteps.loginUser(existingUser.getEmail(), faker.internet().password());
        response.statusCode(401).body("message", equalTo("email or password are incorrect"));
    }
}
