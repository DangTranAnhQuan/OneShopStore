package nhom17.OneShop.controller.admin;

import nhom17.OneShop.dto.ChatMessageDTO;
import nhom17.OneShop.dto.ConversationDTO;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import nhom17.OneShop.repository.UserRepository;

import java.util.List;
import java.util.Map;

/**
 * Controller cho Admin Chat Dashboard
 * Kết hợp cả View và REST API
 */
@Controller
@RequestMapping("/admin/chat")
public class AdminChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Trang Admin Chat Dashboard
     * GET /admin/chat/dashboard
     */
    @GetMapping("/dashboard")
    public String chatDashboard(Model model) {
        List<ConversationDTO> conversations = chatService.getAllConversations();
        long unreadCount = chatService.getTotalUnreadCount();

        model.addAttribute("conversations", conversations);
        model.addAttribute("unreadCount", unreadCount);

        return "admin/chat/chat-dashboard";
    }

    /**
     * API: Lấy danh sách conversations
     * GET /admin/chat/conversations
     */
    @GetMapping("/conversations")
    @ResponseBody
    public ResponseEntity<?> getConversations() {
        try {
            List<ConversationDTO> conversations = chatService.getAllConversations();
            return ResponseEntity.ok(conversations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Lấy lịch sử chat của một conversation
     * GET /admin/chat/messages?sessionId=xxx
     */
    @GetMapping("/messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(@RequestParam String sessionId) {
        try {
            List<ChatMessageDTO> messages = chatService.getChatHistory(sessionId);

            // Đánh dấu đã đọc
            chatService.markConversationAsRead(sessionId);

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Admin gửi tin nhắn
     * POST /admin/chat/reply
     *
     * Body:
     * {
     *   "sessionId": "session_xxx",
     *   "content": "Xin chao, toi co the giup gi?"
     * }
     */
    @PostMapping("/reply")
    @ResponseBody
    public ResponseEntity<?> replyMessage(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            String content = request.get("content");

            if (sessionId == null || content == null || content.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
            }

            User currentAdmin = getCurrentUser();
            Integer userId = currentAdmin != null ? currentAdmin.getUserId() : null;
            ChatMessageDTO message = chatService.sendMessage(sessionId, content, userId);

            return ResponseEntity.ok(message);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Đóng conversation
     * POST /admin/chat/close
     */
    @PostMapping("/close")
    @ResponseBody
    public ResponseEntity<?> closeConversation(@RequestBody Map<String, String> request) {
        try {
            String sessionId = request.get("sessionId");
            chatService.closeConversation(sessionId);
            return ResponseEntity.ok(Map.of("message", "Conversation closed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API: Lấy số tin chưa đọc
     * GET /admin/chat/unread-count
     */
    @GetMapping("/unread-count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount() {
        try {
            long count = chatService.getTotalUnreadCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private User getCurrentUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails) {
                String username = ((UserDetails) auth.getPrincipal()).getUsername();
                return userRepository.findByEmail(username).orElse(null);
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }
}