package in.annapurnayojana.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();
    private final int expirySeconds;
    private final Random rng = new Random();

    public CaptchaService(@Value("${app.captcha.expirySeconds:120}") int expirySeconds) {
        this.expirySeconds = expirySeconds;
    }

    public CaptchaResult generateCaptcha() {
        int a = rng.nextInt(41) + 10; // 10 to 50
        int b = rng.nextInt(20) + 1;  // 1 to 20
        String question = a + " + " + b;
        String answer = String.valueOf(a + b);

        String captchaId = UUID.randomUUID().toString().replace("-", "");

        cache.put("captcha:" + captchaId, answer);

        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"200\" height=\"60\">\n" +
                "  <rect width=\"200\" height=\"60\" fill=\"#1e293b\" rx=\"8\"/>\n" +
                "  <text x=\"50%\" y=\"50%\" dominant-baseline=\"middle\" text-anchor=\"middle\"\n" +
                "        font-size=\"26\" font-family=\"monospace\" fill=\"#38bdf8\" letter-spacing=\"4\">\n" +
                "    " + question + " = ?\n" +
                "  </text>\n" +
                "</svg>";

        return new CaptchaResult(captchaId, svg);
    }

    public boolean validateCaptcha(String captchaId, String userAnswer) {
        String key = "captcha:" + captchaId;
        String stored = cache.get(key);
        if (stored == null) {
            return false;
        }
        cache.remove(key);
        return stored.trim().equals(userAnswer.trim());
    }

    public record CaptchaResult(String captchaId, String svgContent) {}
}
