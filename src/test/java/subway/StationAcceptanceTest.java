package subway;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import subway.dto.StationRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철역 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationAcceptanceTest {
    /**
     * When 지하철역을 생성하면
     * Then 지하철역이 생성된다
     * Then 지하철역 목록 조회 시 생성한 역을 찾을 수 있다
     */
    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {

        // given
        String gasan = "가산디지털단지역";

        // when
        createStationRequest(gasan);

        // then
        assertThat(findStationBy("name")).containsAnyOf(gasan);
    }

    /**
     * Given 2개의 지하철역을 생성하고
     * When 지하철역 목록을 조회하면
     * Then 2개의 지하철역을 응답 받는다
     */
    @Test
    @DisplayName("지하철역 2개를 생성하고, 목록을 조회한다.")
    void showStations() {
        //given
        String gasan = "가산디지털단지역";
        String guro = "구로디지털단지역";

        createStationRequest(gasan);
        createStationRequest(guro);

        // when
        List<String> stationNames = findStationBy("name");

        // then
        assertThat(stationNames).containsAnyOf(gasan, guro);
    }

    /**
     * Given 지하철역을 생성하고
     * When 그 지하철역을 삭제하면
     * Then 그 지하철역 목록 조회 시 생성한 역을 찾을 수 없다
     */
    @Test
    @DisplayName("지하철역이 생성되고, 삭제된다.")
    void deleteStation() {
        //given
        String gasan = "가산디지털단지역";

        ExtractableResponse<Response> createResponse = createStationRequest(gasan);

        // when
        RestAssured
                .when()
                    .delete("/stations/" + getCreatedLocationId(createResponse))
                .then().log().all()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        // then
        assertThat(findStationBy("name")).hasSize(0);

    }

    /**
     * 주어진 지하철역 이름으로 지하철역 생성 요청 및 상태 코드 검증
     *
     * @param stationName 지하철역 이름
     */
    private ExtractableResponse<Response> createStationRequest(String stationName) {
        return RestAssured
                    .given()
                        .body(new StationRequest(stationName))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .when()
                        .post("/stations")
                    .then()
                        .statusCode(HttpStatus.CREATED.value())
                    .extract();
    }

    /**
     * 주어진 응답값으로부터 추출된 Location 속성에서 ID를 반환
     *
     * @param createResponse 응답값
     * @return 추출된 Location 속성의 ID
     */
    private int getCreatedLocationId(ExtractableResponse<Response> createResponse) {
        return Integer
                .parseInt(createResponse.header(HttpHeaders.LOCATION)
                .substring(createResponse.header(HttpHeaders.LOCATION).lastIndexOf('/') + 1));
    }

    /**
     * 지하철역 목록을 요청하고, 주어진 추출 대상 이름에 해당하는 값을 추출 후 리스트 반환
     *
     * @param extractionTargetName 추출 대상 이름
     * @return 추출된 값들의 리스트
     */
    private List<String> findStationBy(String extractionTargetName) {
        return RestAssured.given()
                .when()
                    .get("/stations")
                .then()
                .extract().jsonPath().getList(extractionTargetName , String.class);
    }
}