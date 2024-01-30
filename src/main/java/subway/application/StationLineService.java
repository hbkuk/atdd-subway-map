package subway.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dto.StationLineRequest;
import subway.dto.StationLineResponse;
import subway.entity.StationLine;
import subway.entity.StationLineRepository;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class StationLineService {

    private final StationLineRepository stationLineRepository;

    public StationLineService(StationLineRepository stationLineRepository) {
        this.stationLineRepository = stationLineRepository;
    }

    @Transactional
    public StationLineResponse saveStationLine(StationLineRequest request) {
        return convertToResponse(stationLineRepository.save(convertToEntity(request)));
    }

    public List<StationLineResponse> findAllStationLines() {
        return stationLineRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public StationLineResponse findStationLineById(Long stationLineId) {
        return convertToResponse(stationLineRepository.findById(stationLineId)
                .orElseThrow(EntityNotFoundException::new)); // TODO: Throw Custom Exception?
    }

    @Transactional
    public void updateStationLine(Long stationLineId, StationLineRequest request) {
        StationLine stationLine = stationLineRepository.findById(stationLineId)
                .orElseThrow(EntityNotFoundException::new);
        convertToResponse(stationLineRepository.save(updateStationLine(request, stationLine)));
    }

    @Transactional
    public void deleteStationLine(Long stationLineId) {
        stationLineRepository.deleteById(stationLineId);
    }

    private StationLine updateStationLine(StationLineRequest request, StationLine stationLine) {
        return stationLine.update(
                request.getName(),
                request.getColor()
        );
    }

    private StationLine convertToEntity(StationLineRequest request) {
        return new StationLine(
                request.getName(),
                request.getColor(),
                request.getUpStationId(),
                request.getDownStationId(),
                request.getDistance());
    }

    private StationLineResponse convertToResponse(StationLine stationLine) {
        return new StationLineResponse(
                stationLine.getId(),
                stationLine.getName(),
                stationLine.getColor(),
                stationLine.getUpStationId(),
                stationLine.getDownStationId(),
                stationLine.getDistance());
    }
}
