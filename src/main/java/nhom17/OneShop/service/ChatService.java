package nhom17.OneShop.service;

import nhom17.OneShop.dto.ChatMessageDTO;
import nhom17.OneShop.dto.ConversationDTO;
import nhom17.OneShop.entity.enums.MessageSenderType;

import java.util.List;

public interface ChatService {

    String getOrCreateSessionId(Integer userId, String customerName, String customerEmail);

    /**
     * Gửi tin nhắn
     * @param sessionId - Session ID của phiên chat
     * @param content - Nội dung tin nhắn
     * @param senderType - "CUSTOMER" hoặc "ADMIN"
     * @param userId - ID người gửi (null nếu chưa đăng nhập)
     * @return ChatMessageDTO đã lưu
     */
    ChatMessageDTO sendMessage(String sessionId, String content, MessageSenderType senderType, Integer userId);

    List<ChatMessageDTO> getChatHistory(String sessionId);

    List<ConversationDTO> getAllConversations();

    List<ConversationDTO> getUnreadConversations();

    void markConversationAsRead(String sessionId);

    void closeConversation(String sessionId);

    long getTotalUnreadCount();
}

