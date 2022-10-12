package fi.thl.termed.web;

import static fi.thl.termed.util.io.ResourceUtils.resourceToString;
import static fi.thl.termed.util.json.JsonElementFactory.array;
import static fi.thl.termed.util.json.JsonElementFactory.object;
import static fi.thl.termed.util.json.JsonElementFactory.primitive;
import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

import com.google.gson.JsonObject;
import java.util.UUID;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

class NodeChangesetApiIntegrationTest extends BaseApiIntegrationTest {

  @Test
  void shouldPostNodeChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();

    // save graph and type
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save first node (but not a second one)
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // post changeset deleting first node and inserting second node
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(object("id", primitive(secondNodeId))));
    given(adminAuthorizedJsonSaveRequest)
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_NO_CONTENT);

    // verify changes
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(secondNodeId));

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  void shouldRecoverFromPostingAnIllegalChangeset() {
    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String firstNodeId = UUID.randomUUID().toString();
    String secondNodeId = UUID.randomUUID().toString();
    String thirdNodeId = UUID.randomUUID().toString();

    // save graph and type
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + graphId + "'}")
        .post("/api/graphs?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + typeId + "'}")
        .post("/api/graphs/" + graphId + "/types");

    // save only the first node
    given(adminAuthorizedJsonSaveRequest)
        .body("{'id':'" + firstNodeId + "'}")
        .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
        .then()
        .statusCode(HttpStatus.SC_OK);

    // check preconditions
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(firstNodeId));
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + thirdNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // try posting a changeset with valid delete and broken insert
    JsonObject changeset = object(
        "delete", array(object("id", primitive(firstNodeId))),
        "save", array(
            object("id", primitive(thirdNodeId)),
            object("id", primitive(secondNodeId),
                "properties", object("badAttrId", array(object("value", primitive("foo")))))));
    given(adminAuthorizedJsonSaveRequest)
        .body(changeset.toString())
        .post("/api/graphs/" + graphId + "/types/Concept/nodes?changeset=true")
        .then()
        .statusCode(HttpStatus.SC_BAD_REQUEST);

    // verify that nothing is changed
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + firstNodeId)
        .then()
        .statusCode(HttpStatus.SC_OK)
        .body("id", equalTo(firstNodeId));
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + secondNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);
    given(adminAuthorizedJsonGetRequest)
        .get("/api/graphs/" + graphId + "/types/Concept/nodes/" + thirdNodeId)
        .then()
        .statusCode(HttpStatus.SC_NOT_FOUND);

    // clean up
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }

  @Test
  public void shouldPreservePropertiesIfPatched() {

    String graphId = UUID.randomUUID().toString();
    String typeId = "Concept";

    String nodeId = UUID.randomUUID().toString();

    // save graph and types
    given(adminAuthorizedJsonSaveRequest)
            .body(resourceToString("examples/termed/animals-graph.json"))
            .put("/api/graphs/" + graphId + "?mode=insert");
    given(adminAuthorizedJsonSaveRequest)
            .body(resourceToString("examples/termed/animals-types.json"))
            .post("/api/graphs/" + graphId + "/types?batch=true");

    // Save node with prefLabel
    JsonObject node = object("id", primitive(nodeId),
                    "properties", object(
                            "prefLabel", array(
                                    object("value", primitive("TestLabel"))
                            )
                    )
            );

    given(adminAuthorizedJsonSaveRequest)
            .body(node.toString())
            .post("/api/graphs/" + graphId + "/types/" + typeId + "/nodes")
            .then()
            .statusCode(HttpStatus.SC_OK);

    // Save changeset (patch) without prefLabel property
    JsonObject updatedNode =
            object(
                    "id", primitive(nodeId),
                    "type", object(
                            "id", primitive(typeId),
                            "graph", object(
                                    "id", primitive(graphId)
                            )
                    ),
                    "properties", new JsonObject()
            );

    JsonObject changeset = object(
            "delete", array(),
            "save", array(),
            "patch", array(updatedNode)
    );

    given(adminAuthorizedJsonSaveRequest)
            .body(changeset.toString())
            .post("/api/nodes?changeset=true")
            .then()
            .statusCode(HttpStatus.SC_NO_CONTENT);

    // Original prefLabel should exist
    given(adminAuthorizedJsonGetRequest)
            .get("/api/node-trees?select=*&where=id:" + nodeId)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("[0].properties.prefLabel[0].value", equalTo("TestLabel"));

    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/nodes");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId + "/types");
    given(adminAuthorizedRequest).delete("/api/graphs/" + graphId);
  }
}
