package ru.yandex.praktikum.steps;

import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import ru.yandex.praktikum.model.User;

import static io.restassured.RestAssured.given;

public class UserSteps {

    @Step("Create a user")
    public ValidatableResponse createUser(User user) {
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .post("/auth/register")
                .then();
    }

    @Step("Login with user credentials")
    public ValidatableResponse loginUser(String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .body(new User(email, password, null))
                .when()
                .post("/auth/login")
                .then();
    }

    @Step("Delete a user")
    public void deleteUser(String token) {
        given()
                .header("Authorization", token)
                .when()
                .delete("/auth/user")
                .then()
                .statusCode(202);  // Validate the response to ensure deletion was successful
    }

    @Step("Extract token from response")
    public String extractToken(ValidatableResponse response) {
        return response.extract().path("accessToken");
    }

    @Step("Update user data")
    public ValidatableResponse updateUser(String token, User user) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .patch("/auth/user")
                .then();
    }

    @Step("Update user data without authorization")
    public ValidatableResponse updateUserWithoutAuth(User user) {
        return given()
                .contentType(ContentType.JSON)
                .body(user)
                .when()
                .patch("/auth/user")
                .then();
    }

    @Step("Get user data")
    public ValidatableResponse getUserData(String token) {
        return given()
                .header("Authorization", token)
                .when()
                .get("/auth/user")
                .then();
    }
}
