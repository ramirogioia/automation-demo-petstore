package ramiro;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class PetStoreClient {
    private static final String BASE_URL = "http://localhost:8080/api/v3";

    public Response createPet(String petData) {
        return RestAssured.given()
                          .contentType("application/json")
                          .body(petData)
                          .post(BASE_URL + "/pet");
    }

    public Response getPet(int petId) {
        return RestAssured.given()
                          .get(BASE_URL + "/pet/" + petId);
    }

    public Response deletePet(int petId) {
        return RestAssured.given()
                          .delete(BASE_URL + "/pet/" + petId);
    }

    public Response updatePet(String petJson) {
        return RestAssured.given()
            .contentType("application/json")
            .body(petJson)
            .put(BASE_URL + "/pet");
    }

    public Response findPetsByTags(String... tags) {
        return RestAssured.given()
                          .queryParam("tags", (Object[]) tags)
                          .get(BASE_URL + "/pet/findByTags");
    }
    
}