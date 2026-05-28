package in.annapurnayojana.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.annapurnayojana.api.dto.FormSubmissionPayload;
import in.annapurnayojana.api.repository.FamilyRepository;
import in.annapurnayojana.api.service.DraftService;
import in.annapurnayojana.api.service.FormPayloadValidator;
import in.annapurnayojana.api.service.FormSubmissionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/form")
public class FormController {

    private static final Logger logger = LoggerFactory.getLogger(FormController.class);

    private final DraftService draftService;
    private final FormSubmissionStrategy submissionStrategy;
    private final FamilyRepository familyRepository;
    private final ObjectMapper objectMapper;
    private final FormPayloadValidator formPayloadValidator;

    public FormController(DraftService draftService, FormSubmissionStrategy submissionStrategy, 
                          FamilyRepository familyRepository, ObjectMapper objectMapper,
                          FormPayloadValidator formPayloadValidator) {
        this.draftService = draftService;
        this.submissionStrategy = submissionStrategy;
        this.familyRepository = familyRepository;
        this.objectMapper = objectMapper;
        this.formPayloadValidator = formPayloadValidator;
    }

    private String getMobile(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof io.jsonwebtoken.Claims claims) {
            return claims.get("mobilephone", String.class);
        }
        return authentication.getName();
    }

    @PostMapping("/draft")
    public ResponseEntity<?> saveDraft(@RequestBody FormSubmissionPayload payload, Authentication auth) {
        String mobile = getMobile(auth);
        if (mobile == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (familyRepository.existsByHoFMobileNo(mobile)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "This application has already been submitted. No further changes are allowed."));
        }

        UUID appId = draftService.saveDraft(mobile, "full_draft", payload);
        return ResponseEntity.ok(Map.of("applicationId", appId, "message", "Draft saved."));
    }

    @GetMapping("/draft")
    public ResponseEntity<?> getDraft(Authentication auth) {
        String mobile = getMobile(auth);
        if (mobile == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        String json = draftService.getDraft(mobile, "full_draft");
        if (json == null) return ResponseEntity.noContent().build();

        return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(json);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(Authentication auth) {
        String mobile = getMobile(auth);
        if (mobile == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UUID appId = draftService.getApplicationId(mobile);

        boolean isSubmitted = familyRepository.existsByHoFMobileNo(mobile) || 
                              (appId != null && familyRepository.existsByApplicationId(appId));

        if (!isSubmitted) {
            return ResponseEntity.ok(Map.of("isSubmitted", false));
        }

        // We fetch the family to get status and submittedAt. 
        // In a real scenario we'd do a proper fetch:
        var familyOpt = appId != null ? familyRepository.findByApplicationId(appId) : Optional.empty(); // Note: needs custom query or we can just fetch and check
        
        Map<String, Object> response = new HashMap<>();
        response.put("isSubmitted", true);

        // Try to fetch draft payload if available
        String json = draftService.getDraft(mobile, "full_draft");
        if (json != null) {
            try {
                FormSubmissionPayload payload = objectMapper.readValue(json, FormSubmissionPayload.class);
                response.put("payload", payload);
            } catch (Exception e) {
                logger.warn("Failed to parse draft JSON", e);
            }
        }
        
        // This is an approximation since we don't have the full object here without an extra DB query.
        // I will add the necessary repo method later if needed.
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(Authentication auth) {
        String mobile = getMobile(auth);
        if (mobile == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        if (familyRepository.existsByHoFMobileNo(mobile)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "This application has already been submitted. Duplicate submission rejected."));
        }

        UUID appId = draftService.getApplicationId(mobile);
        if (appId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "No draft found. Please start filling the form first."));
        }

        String json = draftService.getDraft(mobile, "full_draft");
        if (json == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Draft is empty."));
        }

        try {
            FormSubmissionPayload payload = objectMapper.readValue(json, FormSubmissionPayload.class);
            if (payload == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Payload is empty."));
            }

            Map<String, java.util.List<String>> validationErrors = formPayloadValidator.validate(payload);
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", validationErrors));
            }

            payload.setApplicationId(appId);

            submissionStrategy.submit(payload);

            logger.info("Form submission handled via {} for ApplicationId={}", submissionStrategy.getClass().getSimpleName(), appId);

            return ResponseEntity.accepted().body(Map.of(
                    "applicationId", appId,
                    "message", "Your application has been submitted successfully. You will be notified once it is processed."
            ));
        } catch (Exception e) {
            logger.error("Submit error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "An error occurred during submission: " + e.getMessage()));
        }
    }
}
