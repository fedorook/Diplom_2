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

public class UserUpdateTests {

    private final UserSteps userSteps = new UserSteps();
    private final Faker faker = new Faker();
    private String existingUserToken;
    private String anotherUserToken;

    private User anotherUser;

    @Before
    public void setUp() {
        RestConfig.init();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Create an existing user for update tests
        User existingUser = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse existingUserResponse = userSteps.createUser(existingUser);
        existingUserToken = userSteps.extractToken(existingUserResponse);  // Extract token for user deletion after tests

        // Create another user to test unique email constraint
        anotherUser = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse anotherUserResponse = userSteps.createUser(anotherUser);
        anotherUserToken = userSteps.extractToken(anotherUserResponse);  // Extract token for user deletion after tests
    }

    @After
    public void tearDown() {
        if (existingUserToken != null) {
            userSteps.deleteUser(existingUserToken);
        }
        if (anotherUserToken != null) {
            userSteps.deleteUser(anotherUserToken);
        }
    }

    @Test
    @DisplayName("Update user data with authorization")
    public void updateUserWithAuth() {
        User updatedUser = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse response = userSteps.updateUser(existingUserToken, updatedUser);
        response.statusCode(200).body("success", equalTo(true));

        // Verify that the user data was updated
        response = userSteps.getUserData(existingUserToken);
        response.statusCode(200)
                .body("user.email", equalTo(updatedUser.getEmail()))
                .body("user.name", equalTo(updatedUser.getName()));

        // Verify that the new password works
        response = userSteps.loginUser(updatedUser.getEmail(), updatedUser.getPassword());
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Update user data without authorization")
    public void updateUserWithoutAuth() {
        User updatedUser = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse response = userSteps.updateUserWithoutAuth(updatedUser);
        response.statusCode(401).body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Update user data with existing email")
    public void updateUserWithExistingEmail() {
        // Try to update the existing user with the email of another user
        User updatedUser = new User(anotherUser.getEmail(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse response = userSteps.updateUser(existingUserToken, updatedUser);
        response.statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));
    }
}
