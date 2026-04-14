package nhom17.OneShop.service;

import nhom17.OneShop.dto.ChatMessageDTO;
import nhom17.OneShop.dto.ConversationDTO;

import java.util.List;

public interface ChatService {

    String getOrCreateSessionId(String customerName, String customerEmail);

    /**
     * Gửi tin nhắn
     * @param sessionId - Session ID của phiên chat
     * @param content - Nội dung tin nhắn
     * @param userId - ID người gửi (null nếu là admin/guest)
     * @return ChatMessageDTO đã lưu
     */
    ChatMessageDTO sendMessage(String sessionId, String content, Integer userId);

    List<ChatMessageDTO> getChatHistory(String sessionId);

    List<ConversationDTO> getAllConversations();

    List<ConversationDTO> getUnreadConversations();

    void markConversationAsRead(String sessionId);

    void closeConversation(String sessionId);

    long getTotalUnreadCount();
}

