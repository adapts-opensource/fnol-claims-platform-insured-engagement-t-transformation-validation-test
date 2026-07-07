package app.integration.external;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ClaimDataStandardizationDecisionValidationTest {

    private static final String BASE_URL = System.getenv("APP_BASE_URL") != null
            ? System.getenv("APP_BASE_URL") : "http://localhost:8080";
    private static final String FEATURE_SLUG = "claim-data-standardization-decision-validation";

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = BASE_URL;
    }

    @Test
    void validate_date_of_loss_outside_policy_period() {
        String payload = """
                {
                  "policy_number": "POL-HO3-002",
                  "policy_effective": "2024-01-01",
                  "policy_expiration": "2024-04-30",
                  "loss_date": "2024-05-15",
                  "product_form": "HO3",
                  "cause_of_loss": "Fire",
                  "severity": "Medium"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/" + FEATURE_SLUG)
                .then()
                .statusCode(200)
                .body("claim_state", equalTo("Coverage Triage"))
                .body("coverage_review_task", is(true))
                .body("claim_type", equalTo("Coverage review claim"))
                .body("policy_match_status", equalTo("Matched"))
                .body("policy_period_valid", is(false))
                .body("tasks_generated", hasItems("Review Coverage", "Acknowledge Claim"))
                .body("validation_note", equalTo("Loss date outside policy period"));
    }
}
