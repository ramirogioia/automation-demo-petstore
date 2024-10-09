# -------------------------------
# Ramiro Alejandro Gioia

Steps to setup and run.

1- Clone the following repository.
git clone https://github.com/swagger-api/swagger-petstore

2- Run Maven to download all the corresponding dependencies.
mvn package jetty:run

(If you get any errors with the openapi.yaml file, try renaming it to: openapi-inflector.yaml)
When the microservice is running in your local end, you will be able to run the automated suite:

3- Prepare your local Java Environment: 
install JDK, Maven, etc.

4- Clone the following repository.
git clone https://github.com/ramirogioia/automation-demo-petstore

5- Compile the project:
mvn clean install

6- Run all the existing test cases:
mvn test

# -------------------------------
# Ramiro Alejandro Gioia

List of cases developed for this demo.
1- testCreatePetSuccessfully(): Creates a new pet and verifies that the status code is 200, the name is "Ramiro", and the status is "Available".
2- testGetPetById(): Retrieves a pet by ID and checks that the status code is 200, the ID matches, the name is "Ramiro", and the status is "Available".
3- testUpdatePet(): Updates the details of an existing pet and ensures that the status code is 200, the new name is "Marcos", the new status is "Retired", and the category is "Dogs".
4- testDeletePet(): Deletes a pet and verifies that the status code is 200, followed by a check confirming that the pet cannot be found (status code 404).
5- testUpdateUnexistingPet(): Attempts to update a non-existing pet and validates that the status code is 404.
6- testFindUnexistingTag(): Searches for pets by a tag that does not exist and checks that the status code is 200 and the response body is empty.
7- testFindByTwoTags(): Creates three pets with different tags and verifies that searching by two tags returns at least two pets with a status code of 200.
8- testFindByTagsWithNoTagsProvided(): Attempts to search for pets without providing any tags and validates that the status code is 400 with an appropriate error message.

9- (PERFORMANCE TEST - STRESS CASE): Tests the system's ability to handle concurrent requests by attempting to create multiple pets simultaneously until a server error (500) occurs or a maximum request limit is reached. Validates that all successful requests return a status code of 200, and reports the execution time and the request number at which the server error occurred.

Additional Information:
When we detect and we reach the limit and the server begins to return status 500, it goes down permanently and all the rest of the endpoints and controllers begin to fail, the entire service goes down after the performance test.

Output Examples (4 executions, sequential):
Execution time for creating up to 30 pets: 286 ms
All requests succeeded without hitting a server error.

Execution time for creating up to 50 pets: 390 ms
All requests succeeded without hitting a server error.

Execution time for creating up to 50 pets: 374 ms
All requests succeeded without hitting a server error.

Execution time for creating up to 110 pets: 238 ms
A server error (500) occurred after 4 requests.
