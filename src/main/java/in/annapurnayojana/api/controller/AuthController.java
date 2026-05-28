package in.annapurnayojana.api.controller;

import in.annapurnayojana.api.dto.AuthRequests.*;
import in.annapurnayojana.api.service.CaptchaService;
import in.annapurnayojana.api.service.DraftService;
import in.annapurnayojana.api.service.JwtService;
import in.annapurnayojana.api.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CaptchaService captchaService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final DraftService draftService;

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    public AuthController(CaptchaService captchaService, OtpService otpService, JwtService jwtService, DraftService draftService) {
        this.captchaService = captchaService;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.draftService = draftService;
    }

    @GetMapping("/captcha")
    public ResponseEntity<?> getCaptcha() {
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@Valid @RequestBody SendOtpRequest req) {
        if (!captchaService.validateCaptcha(req.getCaptchaId(), req.getCaptchaAnswer())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired captcha. Please try again."));
        }

        if (!MOBILE_PATTERN.matcher(req.getMobileNumber()).matches()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid mobile number format."));
        }

        otpService.sendOtp(req.getMobileNumber());

        return ResponseEntity.ok(Map.of("message", "OTP sent to your registered mobile number."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest req) {
        boolean isValid = otpService.validateOtp(req.getMobileNumber(), req.getOtp());
        if (!isValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP."));
        }

        UUID applicationId = draftService.getApplicationId(req.getMobileNumber());
        JwtService.TokenPair tokens = jwtService.generateTokens(req.getMobileNumber(), applicationId);

        Map<String, Object> response = new HashMap<>();
        response.put("token", tokens.accessToken());
        response.put("refreshToken", tokens.refreshToken());
        response.put("applicationId", applicationId);
        response.put("message", "Authentication successful.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest req) {
        var claims = jwtService.validateToken(req.getRefreshToken());
        
        if (claims == null || !"refresh".equals(claims.get("token_type"))) {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired refresh token."));
        }

        String mobileNumber = claims.get("mobilephone", String.class);
        String appIdStr = claims.get("applicationId", String.class);
        UUID applicationId = (appIdStr != null && !appIdStr.isEmpty()) ? UUID.fromString(appIdStr) : null;

        JwtService.TokenPair tokens = jwtService.generateTokens(mobileNumber, applicationId);

        return ResponseEntity.ok(Map.of(
                "token", tokens.accessToken(),
                "refreshToken", tokens.refreshToken()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            // jti is typically extracted via filter or parsing here
            // This is a placeholder since actual JTI extraction will happen in JwtAuthenticationFilter
            // or from SecurityContextHolder
            String token = (String) authentication.getCredentials();
            var claims = jwtService.validateToken(token);
            if (claims != null) {
                jwtService.revokeToken(claims.getId());
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }
}
