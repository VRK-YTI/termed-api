package fi.thl.termed.web;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

import com.google.gson.JsonObject;
import fi.thl.termed.util.json.JsonUtils;
import java.io.IOException;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.Test;

public class GraphApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  public void shouldSaveTrivialGraph() {
    String graphId = UUID.randomUUID().toString();

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .body("{'id':'" + graphId + "'}")
        .when()
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));
  }

  @Test
  public void shouldSaveAndGetGraph() throws IOException {
    String graphId = UUID.randomUUID().toString();

    JsonObject skosGraph = JsonUtils.getJsonResource("examples/skos/example-skos-graph.json")
        .getAsJsonObject();
    skosGraph.addProperty("id", graphId);

    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .contentType("application/json")
        .body(skosGraph.toString())
        .when()
        .post("/api/graphs")
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(graphId));

    // get the persisted graph and compare to original allowing extra fields such as timestamps
    given()
        .auth().basic(testAdminUsername, testAdminPassword)
        .accept("application/json")
        .when()
        .get("/api/graphs/" + graphId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body(sameJSONAs(skosGraph.toString()).allowingExtraUnexpectedFields());
  }

}
