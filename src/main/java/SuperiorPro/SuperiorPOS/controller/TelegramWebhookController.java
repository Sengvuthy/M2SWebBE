package SuperiorPro.SuperiorPOS.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import SuperiorPro.SuperiorPOS.service.CustomerService;
import SuperiorPro.SuperiorPOS.serviceimpl.TelegramBotServiceImpl;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

    private final CustomerService customerService;
    private final TelegramBotServiceImpl telegramBotService;

    public TelegramWebhookController(CustomerService customerService,
                                     TelegramBotServiceImpl telegramBotService) {
        this.customerService = customerService;
        this.telegramBotService = telegramBotService; // ✅ inject
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> handleUpdate(@RequestBody Map<String, Object> update) {
        System.out.println("✅ Telegram webhook called: " + update);

        Map<String, Object> message = (Map<String, Object>) update.get("message");
        if (message != null) {
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            Long chatId = ((Number) chat.get("id")).longValue();
            String text = (String) message.get("text");

            if (text != null) {
                if (text.startsWith("/bind")) {
                    String phone = text.replace("/bind", "").trim();
                    customerService.bindTelegramByPhone(phone, chatId);
                    telegramBotService.sendMessage(chatId, "✅ Your account is now linked!");
                    telegramBotService.sendMessage(chatId, "✅ អាខោនរបស់អ្នកត្រូវបានភ្ជាប់");
//                } else if (text.startsWith("/start")) {
//                    String phone = text.replace("/start", "").trim();
//                    customerService.bindTelegramByPhone(phone, chatId);
//                    telegramBotService.sendMessage(chatId, 
////                    		"✅ Your account is now linked!");
//                    		"អាខោនរបស់អ្នកត្រូវបានភ្ជាប់");
//                } else {
//                    telegramBotService.sendMessage(chatId,
////                        "👋 Hello! Type /bind <phone> e.g. /bin 012..... then send.");
//                    		"👋 ជម្រាបសួរ! សូមសរសេរ /bind និងលេខទូរស័ព្ទ ឧ. /bin 012345678 រួចចុចបញ្ជូនដើម្បីដំណើរការ.");
                }
            }
        }
        return ResponseEntity.ok().build();
    }
}
