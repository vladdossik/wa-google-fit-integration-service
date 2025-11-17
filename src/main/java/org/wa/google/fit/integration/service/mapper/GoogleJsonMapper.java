package org.wa.google.fit.integration.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.wa.google.fit.integration.service.dto.AggregateResponseDto;
import org.wa.google.fit.integration.service.dto.SleepSessionDto;
import org.wa.google.fit.integration.service.dto.SleepSessionsResponseDto;
import org.wa.google.fit.integration.service.dto.jsonTree.BucketDto;
import org.wa.google.fit.integration.service.dto.jsonTree.DataSetDto;
import org.wa.google.fit.integration.service.dto.jsonTree.PointDto;
import org.wa.google.fit.integration.service.dto.jsonTree.ValueDto;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleJsonMapper {

    private final ObjectMapper objectMapper;

    public int extractSteps(String json) {
        try {
            AggregateResponseDto response = objectMapper.readValue(json, AggregateResponseDto.class);
            DataSetDto dataSets = getDataset(response, 0);
            return sumInt(dataSets);
        } catch (Exception e) {
            return 0;
        }
    }

    public double extractCalories(String json) {
        try {
            AggregateResponseDto response = objectMapper.readValue(json, AggregateResponseDto.class);
            DataSetDto dataSets = getDataset(response, 1);
            return sumFp(dataSets);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double extractDistance(String json) {
        try {
            AggregateResponseDto response = objectMapper.readValue(json, AggregateResponseDto.class);
            DataSetDto dataSets = getDataset(response, 2);
            return sumFp(dataSets) / 1000.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Double> extractHeartRateSeries(String json) {
        try {
            AggregateResponseDto root = objectMapper.readValue(json, AggregateResponseDto.class);
            DataSetDto dataSets = getDataset(root, 0);
            List<Double> heartRateSeries = new ArrayList<>();
            if (dataSets != null && dataSets.getPoint() != null) {
                for (PointDto p : dataSets.getPoint()) {
                    if (p.getValue() == null) continue;
                    for (ValueDto v : p.getValue()) {
                        if (v.getFpVal() != null) heartRateSeries.add(v.getFpVal());
                    }
                }
            }

            return heartRateSeries;
        } catch (Exception e) {
            return List.of();
        }
    }

    public double extractAverageBpm(String json) {
        List<Double> bpmSeries = extractHeartRateSeries(json);
        return bpmSeries.isEmpty() ? 0.0 :
                bpmSeries.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    public double extractRestingBpm(String json) {
        List<Double> bpmSeries = extractHeartRateSeries(json);
        return bpmSeries.isEmpty() ? 0.0 :
                bpmSeries.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
    }

    public double extractTotalSleepHoursFromSessions(String json) {
        try {
            SleepSessionsResponseDto response = objectMapper.readValue(json, SleepSessionsResponseDto.class);

            if (response.getSession() == null) return 0.0;

            double totalMillis = 0.0;

            for (SleepSessionDto session : response.getSession()) {
                if (session.getActivityType() == null || session.getActivityType() != 72) continue;

                long start = parseLongSleepHours(session.getStartTimeMillis());
                long end = parseLongSleepHours(session.getEndTimeMillis());

                if (start > 0 && end > start) {
                    totalMillis += (end - start);
                }
            }

            return totalMillis / (1000.0 * 60 * 60);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private DataSetDto getDataset(AggregateResponseDto response, int index) {
        if (response.getBucket() == null) return null;

        for (BucketDto bucket : response.getBucket()) {
            if (bucket.getDataset() == null || bucket.getDataset().size() <= index) {
                continue;
            }
            return bucket.getDataset().get(index);
        }

        return null;
    }

    private double sumFp(DataSetDto ds) {
        if (ds == null || ds.getPoint() == null) return 0;

        double sum = 0;

        for (PointDto p : ds.getPoint()) {
            if (p.getValue() == null) continue;
            for (ValueDto v : p.getValue()) {
                if (v.getFpVal() != null) sum += v.getFpVal();
            }
        }
        return sum;
    }

    private int sumInt(DataSetDto ds) {
        if (ds == null || ds.getPoint() == null) return 0;

        int sum = 0;

        for (PointDto p : ds.getPoint()) {
            if (p.getValue() == null) continue;
            for (ValueDto v : p.getValue()) {
                if (v.getIntVal() != null) sum += v.getIntVal();
            }
        }
        return sum;
    }

    private long parseLongSleepHours(String value) {
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }
}
