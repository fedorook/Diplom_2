package ru.yandex.praktikum.config;

import io.restassured.RestAssured;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RestConfig {
    public static final String HOST = "https://stellarburgers.nomoreparties.site/api";

    public static void init() {
        RestAssured.baseURI = HOST;
    }
}
