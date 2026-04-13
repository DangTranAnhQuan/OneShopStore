package nhom17.OneShop.service.returnrequest;

import nhom17.OneShop.entity.AuditLog;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ReturnSecurityAuditProxy implements IReturnRequestService {

    @Autowired
    @Qualifier("coreReturnRequestService")
    private IReturnRequestService coreService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void processRequest(Long requestId, String action, String adminNotes, User admin) {
        String actionName = action != null ? action.toUpperCase() + "_RETURN" : "UNKNOWN_ACTION";
        String targetIdStr = String.valueOf(requestId);

        // Trường hợp 1: Chặn quyền (Trước khi gọi Core)
        if (admin == null || admin.getRole() == null || admin.getRole().getRoleId() != 1) {
            String errorMsg = "Access Denied: User lacks Admin role";
            saveAuditLog(admin, actionName, targetIdStr, "FAILED", errorMsg);
            throw new AccessDeniedException(errorMsg);
        }

        try {
            // Gọi Core Service
            coreService.processRequest(requestId, action, adminNotes, admin);
            
            // Ghi Log thành công
            saveAuditLog(admin, actionName, targetIdStr, "SUCCESS", null);
            
        } catch (Exception e) {
            // Trường hợp 2: Lỗi phát sinh từ Core (Trong khi gọi Core)
            saveAuditLog(admin, actionName, targetIdStr, "FAILED", e.getMessage());
            throw e; // Ném Exception tiếp để tầng giao diện bắt được
        }
    }

    private void saveAuditLog(User admin, String actionName, String targetId, String status, String errorMessage) {
        try {
            AuditLog log = new AuditLog(admin, actionName, targetId, status, errorMessage);
            auditLogRepository.save(log);
        } catch (Exception ex) {
            // Đảm bảo lỗi khi ghi log không làm vỡ luồng chính (tùy chọn theo dự án, nhưng best practice)
            System.err.println("Không thể ghi AuditLog: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
