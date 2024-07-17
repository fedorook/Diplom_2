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
import ru.yandex.praktikum.steps.OrderSteps;
import ru.yandex.praktikum.steps.UserSteps;

import static org.hamcrest.Matchers.equalTo;

public class OrderGetTests {

    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private final Faker faker = new Faker();
    private String userToken;

    @Before
    public void setUp() {
        RestConfig.init();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Create a user for order tests
        User user = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse userResponse = userSteps.createUser(user);
        userToken = userSteps.extractToken(userResponse);
    }

    @After
    public void tearDown() {
        if (userToken != null) {
            userSteps.deleteUser(userToken);
        }
    }

    @Test
    @DisplayName("Get orders of an authorized user")
    public void getOrdersWithAuth() {
        ValidatableResponse response = orderSteps.getUserOrdersWithAuth(userToken);
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Get orders of an unauthorized user")
    public void getOrdersWithoutAuth() {
        ValidatableResponse response = orderSteps.getUserOrdersWithoutAuth();
        response.statusCode(401).body("success", equalTo(false));
    }
}
