package nhom17.OneShop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO để truyền dữ liệu tin nhắn giữa Frontend và Backend
 * Không expose toàn bộ Entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {

    private Long messageId;
    private String sessionId;
    private String content;
    private String senderRole;
    private LocalDateTime sentAt;
    private Boolean seen;

    private String senderName;
    private String avatarUrl;
}