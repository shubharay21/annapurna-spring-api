package in.annapurnayojana.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class DraftService {

    private static final Logger logger = LoggerFactory.getLogger(DraftService.class);
    private static final long DRAFT_TTL_HOURS = 24;

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public DraftService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UUID saveDraft(String mobileNumber, String stepKey, Object stepData) {
        String appIdKey = "draft:" + mobileNumber + ":appId";
        String existing = cache.get(appIdKey);

        UUID applicationId;
        boolean isNew = false;

        if (existing == null) {
            applicationId = UUID.randomUUID();
            cache.put(appIdKey, applicationId.toString());
            isNew = true;
            logger.info("New draft started for {}. ApplicationId: {}", mobileNumber, applicationId);
        } else {
            applicationId = UUID.fromString(existing);
        }

        try {
            String stepJson = objectMapper.writeValueAsString(stepData);
            cache.put("draft:" + mobileNumber + ":step:" + stepKey, stepJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing step data", e);
        }

        // No-op for TTL in memory map

        if (isNew) {
            logger.warn("=== [MOCK SMS] Dear Applicant, your Annapurna Yojana Application ID is: {}. Use this to resume your application. ===", applicationId);
        }

        return applicationId;
    }

    public String getDraft(String mobileNumber, String stepKey) {
        return cache.get("draft:" + mobileNumber + ":step:" + stepKey);
    }

    public UUID getApplicationId(String mobileNumber) {
        String val = cache.get("draft:" + mobileNumber + ":appId");
        if (val == null) {
            return null;
        }
        try {
            return UUID.fromString(val);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
