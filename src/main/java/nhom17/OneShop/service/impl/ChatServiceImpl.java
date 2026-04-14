package nhom17.OneShop.service.impl;

import nhom17.OneShop.dto.ChatMessageDTO;
import nhom17.OneShop.dto.ConversationDTO;
import nhom17.OneShop.entity.SessionChat;
import nhom17.OneShop.entity.MessageChat;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.SessionStatus;
import nhom17.OneShop.repository.UserRepository;
import nhom17.OneShop.repository.SessionChatRepository;
import nhom17.OneShop.repository.MessageChatRepository;
import nhom17.OneShop.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final SessionChatRepository sessionChatRepository;
    private final MessageChatRepository messageChatRepository;
    private final UserRepository userRepository;

    public ChatServiceImpl(SessionChatRepository sessionChatRepository,
                           MessageChatRepository messageChatRepository,
                           UserRepository userRepository) {
        this.sessionChatRepository = sessionChatRepository;
        this.messageChatRepository = messageChatRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String getOrCreateSessionId(String customerName, String customerEmail) {
        if (customerName != null && !customerName.isBlank()) {
            Optional<SessionChat> existingSession = (customerEmail != null && !customerEmail.isBlank())
                    ? sessionChatRepository.findByCustomerNameAndCustomerEmail(customerName, customerEmail)
                    : sessionChatRepository.findByCustomerName(customerName);

            if (existingSession.isPresent()) {
                SessionChat session = existingSession.get();
                if (SessionStatus.CLOSED.equals(session.getStatus())) {
                    session.reopen();
                    sessionChatRepository.save(session);
                }
                return session.getSessionId();
            }
        }

        String sessionId = "session_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
        SessionChat session = new SessionChat(sessionId, customerName, customerEmail);

        sessionChatRepository.save(session);
        return sessionId;
    }

    @Override
    public ChatMessageDTO sendMessage(String sessionId, String content, Integer userId) {
        SessionChat chatSession = sessionChatRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat với ID: " + sessionId));

        User sender = null;
        if (userId != null) {
            sender = userRepository.findById(userId).orElse(null);
        }

        MessageChat message = new MessageChat(sender, content);
        chatSession.addMessage(message);
        chatSession.markNewMessage(message.getSentAt(), !isAdminUser(sender));
        sessionChatRepository.save(chatSession);

        return convertToMessageDTO(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getChatHistory(String sessionId) {
        SessionChat session = sessionChatRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat với ID: " + sessionId));
        return session.getMessages().stream()
                .sorted(Comparator.comparing(MessageChat::getSentAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::convertToMessageDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getAllConversations() {
        return sessionChatRepository.findAllByOrderByLastMessageAtDesc().stream()
                .map(this::convertToConversationDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDTO> getUnreadConversations() {
        return sessionChatRepository.findByUnreadCountGreaterThanOrderByLastMessageAtDesc(0).stream()
                .map(this::convertToConversationDTO).collect(Collectors.toList());
    }

    @Override
    public void markConversationAsRead(String sessionId) {
        messageChatRepository.markAllAsReadBySessionId(sessionId);
        sessionChatRepository.findById(sessionId).ifPresent(session -> {
            session.markAllRead();
            sessionChatRepository.save(session);
        });
    }

    @Override
    public void closeConversation(String sessionId) {
        sessionChatRepository.findById(sessionId).ifPresent(session -> {
            session.close();
            sessionChatRepository.save(session);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnreadCount() {
        return sessionChatRepository.findByUnreadCountGreaterThanOrderByLastMessageAtDesc(0)
                .stream().mapToInt(session -> session.getUnreadCount() == null ? 0 : session.getUnreadCount()).sum();
    }

    private ChatMessageDTO convertToMessageDTO(MessageChat entity) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setMessageId(entity.getMessageId());
        dto.setSessionId(entity.getSession().getSessionId());
        dto.setContent(entity.getContent());
        dto.setSenderRole(resolveSenderRole(entity));
        dto.setSentAt(entity.getSentAt());
        dto.setSeen(entity.getSeen());
        dto.setAvatarUrl(entity.getUser() != null ? entity.getUser().getAvatarUrl() : null);

        if (entity.getUser() != null) {
            dto.setSenderName(entity.getUser().getFullName());
        } else if (isAdminMessage(entity)) {
            dto.setSenderName("Admin OneShop");
        } else {
            dto.setSenderName(entity.getSession().getCustomerName());
        }
        return dto;
    }

    private String resolveSenderRole(MessageChat entity) {
        return isAdminMessage(entity) ? "ADMIN" : "CUSTOMER";
    }

    private boolean isAdminMessage(MessageChat entity) {
        return isAdminUser(entity.getUser());
    }

    private boolean isAdminUser(User user) {
        if (user == null || user.getRole() == null || user.getRole().getRoleName() == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(user.getRole().getRoleName())
                || "ROLE_ADMIN".equalsIgnoreCase(user.getRole().getRoleName());
    }

    private ConversationDTO convertToConversationDTO(SessionChat entity) {
        ConversationDTO dto = new ConversationDTO();
        dto.setSessionId(entity.getSessionId());
        dto.setCustomerName(entity.getCustomerName());
        dto.setCustomerEmail(entity.getCustomerEmail());
        dto.setLastMessageAt(entity.getLastMessageAt());
        dto.setUnreadCount(entity.getUnreadCount());
        SessionStatus status = entity.getStatus();
        dto.setStatus(status != null ? status.getLabel() : null);


        entity.getMessages().stream()
                .max(Comparator.comparing(MessageChat::getSentAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .ifPresent(lastMessage -> dto.setLastMessage(lastMessage.getContent()));
        return dto;
    }
}

