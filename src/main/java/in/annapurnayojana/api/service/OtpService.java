package in.annapurnayojana.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final int expirySeconds;
    private final Random random = new Random();

    public OtpService(@Value("${app.otp.expirySeconds:300}") int expirySeconds) {
        this.expirySeconds = expirySeconds;
    }

    public String sendOtp(String mobileNumber) {
        String otp = String.valueOf(100000 + random.nextInt(900000));
        
        cache.put("otp:" + mobileNumber, otp);

        // MOCK: log OTP
        logger.warn("=== [MOCK SMS] OTP for {}: {} (expires in {}s) ===", mobileNumber, otp, expirySeconds);

        return otp;
    }

    public boolean validateOtp(String mobileNumber, String otp) {
        String key = "otp:" + mobileNumber;
        String stored = cache.get(key);
        
        if (stored == null) {
            logger.warn("OTP not found or expired for {}", mobileNumber);
            return false;
        }
        
        cache.remove(key);
        return stored.trim().equals(otp.trim());
    }
}
