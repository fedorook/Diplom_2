package ru.yandex.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

import java.util.List;

import static io.restassured.RestAssured.given;
import ru.yandex.praktikum.model.Order;

public class OrderSteps {

    @Step("Get ingredients")
    public List<String> getIngredients() {
        return given()
                .when()
                .get("ingredients")
                .then()
                .statusCode(200)
                .extract()
                .path("data._id");
    }

    @Step("Create an order with authorization")
    public ValidatableResponse createOrderWithAuth(String token, List<String> ingredients) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(new Order(ingredients))
                .when()
                .post("orders")
                .then();
    }

    @Step("Create an order without authorization")
    public ValidatableResponse createOrderWithoutAuth(List<String> ingredients) {
        return given()
                .contentType(ContentType.JSON)
                .body(new Order(ingredients))
                .when()
                .post("orders")
                .then();
    }

    @Step("Create an order with invalid ingredient hash")
    public ValidatableResponse createOrderWithInvalidHash(String token) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(new Order(List.of("invalidHash")))
                .when()
                .post("orders")
                .then();
    }

    @Step("Get user's orders with authorization")
    public ValidatableResponse getUserOrdersWithAuth(String token) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .when()
                .get("orders")
                .then();
    }

    @Step("Get user's orders without authorization")
    public ValidatableResponse getUserOrdersWithoutAuth() {
        return given()
                .contentType(ContentType.JSON)
                .when()
                .get("orders")
                .then();
    }
}
