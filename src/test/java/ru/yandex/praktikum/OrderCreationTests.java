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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class OrderCreationTests {

    private final UserSteps userSteps = new UserSteps();
    private final OrderSteps orderSteps = new OrderSteps();
    private final Faker faker = new Faker();
    private String userToken;
    private List<String> ingredients;

    @Before
    public void setUp() {
        RestConfig.init();
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());

        // Create a user for order tests
        User user = new User(faker.internet().emailAddress(), faker.internet().password(), faker.name().fullName());
        ValidatableResponse userResponse = userSteps.createUser(user);
        userToken = userSteps.extractToken(userResponse);

        // Get ingredients
        ingredients = orderSteps.getIngredients();
    }

    @After
    public void tearDown() {
        if (userToken != null) {
            userSteps.deleteUser(userToken);
        }
    }

    @Test
    @DisplayName("Create an order with authorization")
    public void createOrderWithAuth() {
        Collections.shuffle(ingredients);
        ValidatableResponse response = orderSteps.createOrderWithAuth(userToken, ingredients.subList(0, 2));
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Create an order without authorization")
    public void createOrderWithoutAuth() {
        Collections.shuffle(ingredients);
        ValidatableResponse response = orderSteps.createOrderWithoutAuth(ingredients.subList(0, 2));
        response.statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Create an order without ingredients")
    public void createOrderWithoutIngredients() {
        ValidatableResponse response = orderSteps.createOrderWithAuth(userToken, Collections.emptyList());
        response.statusCode(400).body("success", equalTo(false)).body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Create an order with invalid ingredient hash")
    public void createOrderWithInvalidHash() {
        ValidatableResponse response = orderSteps.createOrderWithInvalidHash(userToken);
        response.statusCode(500).body(containsString("Internal Server Error"));
    }
}
