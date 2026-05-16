package SuperiorPro.SuperiorPOS.serviceimpl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TelegramBotServiceImpl {
    private final String botToken = "8788530771:AAG2bVAvhZc1kKq_D85Hvat5kzZGEW9GqW4"; // ✅ your real token
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(Long telegramId, String message) {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        Map<String, Object> payload = Map.of(
            "chat_id", telegramId,
            "text", message
        );
        restTemplate.postForObject(url, payload, String.class);
    }

    // Temporary stub until you wire up real chat_id capture
    public Long verifyAndGetTelegramId(String phone) {
        // TODO: Replace with actual logic
        return 123456789L;
    }
}
