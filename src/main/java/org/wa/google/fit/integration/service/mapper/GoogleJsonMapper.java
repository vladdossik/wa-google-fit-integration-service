package org.wa.google.fit.integration.service.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleJsonMapper {

    private final ObjectMapper objectMapper;

    public int extractSteps(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            int totalSteps = 0;

            if (root.has("bucket")) {
                for (JsonNode bucket : root.get("bucket")) {
                    JsonNode datasets = bucket.path("dataset");
                    if (datasets.isArray() && !datasets.isEmpty()) {
                        JsonNode stepsDataset = datasets.get(0);
                        for (JsonNode point : stepsDataset.path("point")) {
                            for (JsonNode value : point.path("value")) {
                                if (value.has("intVal")) {
                                    totalSteps += value.get("intVal").asInt(0);
                                }
                            }
                        }
                    }
                }
            }
            return totalSteps;
        } catch (Exception e) {
            return 0;
        }
    }

    public double extractCalories(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            double totalCalories = 0.0;

            if (root.has("bucket")) {
                for (JsonNode bucket : root.get("bucket")) {
                    JsonNode datasets = bucket.path("dataset");
                    if (datasets.isArray() && datasets.size() > 1) {
                        JsonNode caloriesDataset = datasets.get(1);
                        for (JsonNode point : caloriesDataset.path("point")) {
                            for (JsonNode value : point.path("value")) {
                                if (value.has("fpVal")) {
                                    totalCalories += value.get("fpVal").asDouble(0.0);
                                }
                            }
                        }
                    }
                }
            }
            
            return totalCalories;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double extractDistance(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            double totalDistanceMeters = 0.0;

            if (root.has("bucket")) {
                for (JsonNode bucket : root.get("bucket")) {
                    JsonNode datasets = bucket.path("dataset");
                    if (datasets.isArray() && datasets.size() > 2) {
                        JsonNode distanceDataset = datasets.get(2);
                        for (JsonNode point : distanceDataset.path("point")) {
                            for (JsonNode value : point.path("value")) {
                                if (value.has("fpVal")) {
                                    totalDistanceMeters += value.get("fpVal").asDouble(0.0);
                                }
                            }
                        }
                    }
                }
            }
            
            return totalDistanceMeters / 1000.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Double> extractHeartRateSeries(String json) {
        List<Double> heartRateSeries = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(json);
            if (root.has("bucket")) {
                for (JsonNode bucket : root.get("bucket")) {
                    for (JsonNode dataset : bucket.path("dataset")) {
                        for (JsonNode point : dataset.path("point")) {
                            for (JsonNode value : point.path("value")) {
                                if (value.has("fpVal")) {
                                    heartRateSeries.add(value.get("fpVal").asDouble(0.0));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return List.of();
        }
        return heartRateSeries;
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
            JsonNode root = new ObjectMapper().readTree(json);
            JsonNode sessions = root.path("session");

            double totalMillis = 0;

            for (JsonNode session : sessions) {
                int activityType = session.path("activityType").asInt(0);
                if (activityType != 72) continue;

                long start = Long.parseLong(session.path("startTimeMillis").asText("0"));
                long end = Long.parseLong(session.path("endTimeMillis").asText("0"));

                if (start > 0 && end > start) {
                    totalMillis += (end - start);
                }
            }

            return totalMillis / (1000.0 * 60 * 60);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
