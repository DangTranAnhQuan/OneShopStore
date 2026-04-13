package nhom17.OneShop.service.returnrequest;

import nhom17.OneShop.entity.User;

public interface IReturnRequestService {
    void processRequest(Long requestId, String action, String adminNotes, User admin);
}
