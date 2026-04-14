package nhom17.OneShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho một conversation trong Admin Dashboard
 * Hiển thị thông tin tóm tắt của phiên chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {

    private String sessionId;
    private String customerName;
    private String customerEmail;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCount;
    private String status;

    private String avatarUrl;
}