package ramiro;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.restassured.response.Response;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestPetStore {

    private final PetStoreClient petStoreClient = new PetStoreClient();
    private final int petId = 10;
    private final int basePetId = 100;
    private final int maxRequests = 90;
    private final List<Response> responses = new ArrayList<>();

    @Test
    @Order(1)
    public void testCreatePetSuccessfully() {
        System.out.println("1");
        String newPetJson = "{"
            + "\"id\": " + this.petId + ", "
            + "\"name\": \"Ramiro\", "
            + "\"category\": { \"id\": 0, \"name\": \"Cats\" }, "
            + "\"photoUrls\": [\"https://example.com/photos/ramiro1.jpg\", \"https://example.com/photos/ramiro2.jpg\"], "
            + "\"tags\": ["
            + "  { \"id\": 1, \"name\": \"rama\" }"
            + "], "
            + "\"status\": \"Available\""
            + "}";

        Response response = petStoreClient.createPet(newPetJson);
        
        assertEquals(200, response.getStatusCode(), "The status code is not as expected");
        
        String createdPetName = response.jsonPath().getString("name");
        String createdPetStatus = response.jsonPath().getString("status");
        assertEquals("Ramiro", createdPetName);
        assertEquals("Available", createdPetStatus);
    }

    @Test
    @Order(2)
    public void testGetPetById() {        
        System.out.println("2");
        Response response = petStoreClient.getPet(this.petId);
        
        assertEquals(200, response.getStatusCode(), "The pet with the specified ID was not found");
        
        assertEquals(this.petId, response.jsonPath().getInt("id"));
        assertEquals("Ramiro", response.jsonPath().getString("name"));
        assertEquals("Available", response.jsonPath().getString("status"));
    }

    @Test
    @Order(3)
    public void testUpdatePet() {
        System.out.println("3");
        Response response = petStoreClient.getPet(this.petId);
        
        assertEquals(200, response.getStatusCode(), "The pet with the specified ID was not found");
        
        assertEquals(this.petId, response.jsonPath().getInt("id"));
        assertEquals("Ramiro", response.jsonPath().getString("name"));
        assertEquals("Available", response.jsonPath().getString("status"));
        assertEquals("Cats", response.jsonPath().getString("category.name"), "Category name does not match");

        String updatedPetJson = "{"
            + "\"id\": " + this.petId + ", "
            + "\"name\": \"Marcos\", "
            + "\"category\": { \"id\": 0, \"name\": \"Dogs\" }, "
            + "\"photoUrls\": [\"https://example.com/photos/ramiro1.jpg\", \"https://example.com/photos/ramiro2.jpg\"], "
            + "\"tags\": ["
            + "  { \"id\": 1, \"name\": \"rama\" }"
            + "], "
            + "\"status\": \"Retired\""
            + "}";

        Response updateResponse = petStoreClient.updatePet(updatedPetJson);
        
        assertEquals(200, updateResponse.getStatusCode(), "Failed to update the pet");

        Response getResponse = petStoreClient.getPet(this.petId);
        
        assertEquals(200, getResponse.getStatusCode(), "The pet with the specified ID was not found");
        assertEquals("Marcos", getResponse.jsonPath().getString("name"));
        assertEquals("Retired", getResponse.jsonPath().getString("status"));
        assertEquals("Dogs", getResponse.jsonPath().getString("category.name"));
    }

    @Test
    @Order(4)
    public void testDeletePet() {
        System.out.println("4");
        Response response = petStoreClient.deletePet(this.petId);
        
        assertEquals(200, response.getStatusCode(), "Failed to delete the pet");
        
        response = petStoreClient.getPet(this.petId);
        assertEquals(404, response.getStatusCode(), "The pet was not deleted properly");
    }

    @Test
    @Order(5)
    public void testUpdateUnexistingPet() {
        System.out.println("5");
        String updatedPetJson = "{"
            + "\"id\": " + this.petId + ", "
            + "\"name\": \"Marcos\", "
            + "\"category\": { \"id\": 0, \"name\": \"Dogs\" }, "
            + "\"photoUrls\": [\"https://example.com/photos/ramiro1.jpg\", \"https://example.com/photos/ramiro2.jpg\"], "
            + "\"tags\": ["
            + "  { \"id\": 1, \"name\": \"rama\" }"
            + "], "
            + "\"status\": \"Retired\""
            + "}";

        Response updateResponse = petStoreClient.updatePet(updatedPetJson);
    
        assertEquals(404, updateResponse.getStatusCode(), "Updated a pet that does not exist");
        
        Response getResponse = petStoreClient.getPet(this.petId);
        
        assertEquals(404, getResponse.getStatusCode(), "The pet should not be found");

    }

    @Test
    @Order(6)
    public void testFindUnexistingTag() {
        System.out.println("6");
        Response response = petStoreClient.findPetsByTags("non-existentTag");
        
        assertEquals(200, response.getStatusCode(), "The status code is not as expected");
        
        assertEquals(0, response.jsonPath().getList("").size(), "Expected no pets to be found");
    }

    @Test
    @Order(7)
    public void testFindByTwoTags() {
        System.out.println("7");

        createPetWithTag("Pet1", "tag1");
        createPetWithTag("Pet2", "tag2");
        createPetWithTag("Pet3", "tag3");

        Response response = petStoreClient.findPetsByTags("tag3", "tag1");
        
        assertEquals(200, response.getStatusCode(), "The status code is not as expected");
        
        assertEquals(true, response.jsonPath().getList("").size() >= 2, "Expected at least two pets to be found");
    }

    @Test
    @Order(8)
    public void testFindByTagsWithNoTagsProvided() {
        System.out.println("8");
        
        Response response = petStoreClient.findPetsByTags();
        
        assertEquals(400, response.getStatusCode(), "Expected status code 400 for bad request");
        
        String errorMessage = response.getBody().asString();
        assertEquals("No tags provided. Try again?", errorMessage, "Expected error message not found");
    }

    @Test
    @Order(9)
    public void testConcurrentCreatePets() {
        System.out.println("9");
    
        long startTime = System.currentTimeMillis();
        List<CompletableFuture<Response>> futures = new ArrayList<>();
    
        List<Response> responses = new ArrayList<>(this.maxRequests);
    
        int requestCount = 0;
        boolean serverErrorOccurred = false;
        int failedRequestIndex = -1; 
    
        while (!serverErrorOccurred && requestCount < this.maxRequests) {
            int uniquePetId = this.basePetId + requestCount;
            String newPetJson = "{"
                + "\"id\": " + uniquePetId + ", "
                + "\"name\": \"Pet" + uniquePetId + "\", "
                + "\"category\": { \"id\": 0, \"name\": \"Cats\" }, "
                + "\"photoUrls\": [\"https://example.com/photos/pet" + uniquePetId + "1.jpg\", \"https://example.com/photos/pet" + uniquePetId + "2.jpg\"], "
                + "\"tags\": [" 
                + "  { \"id\": 1, \"name\": \"tag" + uniquePetId + "\" }" 
                + "], "
                + "\"status\": \"Available\""
                + "}";
    
            int currentRequestIndex = requestCount;
            CompletableFuture<Response> future = CompletableFuture.supplyAsync(() -> {
                Response response = petStoreClient.createPet(newPetJson);
                responses.add(response);
                return response;
            });
    
            futures.add(future);
            requestCount++;
        }
    
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    
        for (int i = 0; i < futures.size(); i++) {
            Response response = futures.get(i).join();
            if (response.getStatusCode() == 500) {
                serverErrorOccurred = true;
                failedRequestIndex = i; 
                break; 
            } else {
                assertEquals(200, response.getStatusCode(), "Failed to create pet: " + response.jsonPath().getString("name"));
            }
        }
    
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
    
        System.out.println("Execution time for creating up to " + requestCount + " pets: " + duration + " ms");
        if (serverErrorOccurred) {
            System.out.println("A server error (500) occurred after " + (failedRequestIndex + 1) + " requests.");
        } else {
            System.out.println("All requests succeeded without hitting a server error.");
        }
    }


    private void createPetWithTag(String petName, String tag) {
        int uniqueId = (int) (Math.random() * 10000);
        String petJson = "{"
            + "\"id\": " + uniqueId + ", "
            + "\"name\": \"" + petName + "\", "
            + "\"category\": { \"id\": 0, \"name\": \"Cats\" }, "
            + "\"photoUrls\": [\"https://example.com/photos/" + petName + "1.jpg\"], "
            + "\"tags\": [{ \"id\": 0, \"name\": \"" + tag + "\" }], "
            + "\"status\": \"available\""
            + "}";
        petStoreClient.createPet(petJson);
    }

}