package subway;

import config.fixtures.subway.StationLineMockData;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import subway.dto.StationLineRequest;
import subway.entity.StationLine;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static subway.StationLineSteps.*;
import static utils.HttpResponseUtils.getCreatedLocationId;

@DisplayName("지하철 노선 관련 기능")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StationLineAcceptanceTest {

    public static final String NAME_KEY = "name";
    public static final String COLOR_KEY = "color";
    public static final String UP_STATION_ID_KEY = "upStationId";
    public static final String DOWN_STATION_ID_KEY = "downStationId";
    public static final String DISTANCE_KEY = "distance";

    /**
     * When 지하철 노선을 생성하면
     * When  지하철 노선이 생성된다
     * Then  지하철 노선 목록 조회 시 생성된 노선을 찾을 수 있다.
     */
    @DisplayName("지하철 노선을 생성한다.")
    @Test
    void createStationLine() {
        // given
        StationLineRequest 신분당선 = StationLineMockData.신분당선;

        // when
        지하철_노선_생성_요청_검증_포함(신분당선);

        // then
        assertThat(convertStationLines(모든_지하철_노선_조회_요청())).usingRecursiveComparison()
                .ignoringFields("id", "sections")
                .isEqualTo(List.of(신분당선));
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When  지하철 노선 목록을 조회하면
     * Then  지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @DisplayName("지하철 노선 목록을 조회한다.")
    @Test
    void findAllStationLine() {
        // given
        StationLineRequest 신분당선 = StationLineMockData.신분당선;
        StationLineRequest 분당선 = StationLineMockData.분당선;

        지하철_노선_생성_요청_검증_포함(신분당선);
        지하철_노선_생성_요청_검증_포함(분당선);

        // when
        assertThat(convertStationLines(모든_지하철_노선_조회_요청())).usingRecursiveComparison()
                .ignoringFields("id", "sections")
                .isEqualTo(List.of(신분당선, 분당선));
    }

    /**
     * Given 지하철 노선을 생성하고
     * When  생성한 지하철 노선을 조회하면
     * Then  생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @DisplayName("지하철 노선을 조회한다.")
    @Test
    void findStationLine() {
        // given
        StationLineRequest 신분당선 = StationLineMockData.신분당선;
        ExtractableResponse<Response> response = 지하철_노선_생성_요청_검증_포함(신분당선);

        // when, then
        assertThat(convertStationLine(지하철_노선_조회_요청(getCreatedLocationId(response)))).usingRecursiveComparison()
                .ignoringFields("id", "sections")
                .isEqualTo(신분당선);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When  생성한 지하철 노선을 수정하면
     * Then  해당 지하철 노선 정보는 수정된다.
     */
    @DisplayName("지하철 노선을 수정한다.")
    @Test
    void updateStationLine() {
        // given
        StationLineRequest 신분당선 = StationLineMockData.신분당선;
        StationLineRequest 수정된_신분당선 = StationLineMockData.수정된_신분당선;

        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청_검증_포함(신분당선);

        // when
        지하철_노선_수정_요청(수정된_신분당선, getCreatedLocationId(createResponse));

        // then
        assertThat(convertStationLine(지하철_노선_조회_요청(getCreatedLocationId(createResponse)))).usingRecursiveComparison()
                .ignoringFields("id", "sections")
                .isEqualTo(수정된_신분당선);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When  생성한 지하철 노선을 삭제하면
     * Then  해당 지하철 노선 정보는 삭제된다.
     */
    @DisplayName("지하철 노선을 삭제한다.")
    @Test
    void deleteStationLine() {
        // given
        StationLineRequest 신분당선 = StationLineMockData.신분당선;
        StationLineRequest 분당선 = StationLineMockData.분당선;

        ExtractableResponse<Response> createResponse = 지하철_노선_생성_요청_검증_포함(신분당선);
        지하철_노선_생성_요청_검증_포함(분당선);

        // when
        지하철_노선_삭제_요청(getCreatedLocationId(createResponse));

        // then
        assertThat(convertStationLines(모든_지하철_노선_조회_요청())).usingRecursiveComparison()
                .ignoringFields("id", "sections")
                .isEqualTo(List.of(분당선));
    }

    /**
     * 주어진 JsonPath로 부터 StationLine 객체 목록 만들어서 반환
     *
     * @param jsonPath JSON 응답 객체
     * @return StationLine 객체 목록
     */
    private List<StationLine> convertStationLines(JsonPath jsonPath) {
        List<String> names = jsonPath.getList(NAME_KEY, String.class);
        List<String> colors = jsonPath.getList(COLOR_KEY, String.class);
        List<Long> upStationIds = jsonPath.getList(UP_STATION_ID_KEY, Long.class);
        List<Long> downStationIds = jsonPath.getList(DOWN_STATION_ID_KEY, Long.class);
        List<Integer> distances = jsonPath.getList(DISTANCE_KEY, Integer.class);

        return IntStream.range(0, names.size())
                .mapToObj(i -> new StationLine(
                        names.get(i),
                        colors.get(i),
                        upStationIds.get(i),
                        downStationIds.get(i),
                        distances.get(i)
                ))
                .collect(Collectors.toList());
    }

    /**
     * 주어진 JsonPath로 부터 StationLine 객체를 만들어서 반환
     *
     * @param jsonPath JSON 응답 객체
     * @return StationLine 객체
     */
    private StationLine convertStationLine(JsonPath jsonPath) {
        return new StationLine(
                jsonPath.get(NAME_KEY).toString(),
                jsonPath.get(COLOR_KEY).toString(),
                Long.parseLong(jsonPath.get(UP_STATION_ID_KEY).toString()),
                Long.parseLong(jsonPath.get(DOWN_STATION_ID_KEY).toString()),
                Integer.parseInt(jsonPath.get(DISTANCE_KEY).toString())
        );
    }
}
