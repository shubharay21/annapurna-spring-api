package in.annapurnayojana.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthRequests {

    @Data
    public static class SendOtpRequest {
        @NotBlank(message = "Mobile number is required.")
        private String mobileNumber;
        
        @NotBlank(message = "Captcha ID is required.")
        private String captchaId;
        
        @NotBlank(message = "Captcha Answer is required.")
        private String captchaAnswer;
    }

    @Data
    public static class VerifyOtpRequest {
        @NotBlank(message = "Mobile number is required.")
        private String mobileNumber;
        
        @NotBlank(message = "OTP is required.")
        @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits.")
        private String otp;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required.")
        private String refreshToken;
    }
}
