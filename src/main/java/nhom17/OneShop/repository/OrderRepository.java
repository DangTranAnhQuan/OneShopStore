package nhom17.OneShop.repository;

import nhom17.OneShop.entity.Order;
import nhom17.OneShop.entity.User;
import nhom17.OneShop.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    Page<Order> findByUser(User user, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.user " +
            "LEFT JOIN FETCH o.orderDetails od " +
            "LEFT JOIN FETCH od.product " +
            "LEFT JOIN FETCH o.voucher " +
            "LEFT JOIN FETCH o.shipping " +
            "WHERE o.orderId = :orderId")
    Optional<Order> findByIdWithDetails(@Param("orderId") Long orderId);

    boolean existsByUser_UserId(Integer userId);
    long countByVoucher_VoucherCodeAndOrderStatusNotIn(String voucherCode, List<OrderStatus> invalidStates);
    long countByUserAndVoucher_VoucherCodeAndOrderStatusNotIn(User user, String voucherCode, List<OrderStatus> invalidStates);
    boolean existsByVoucher_VoucherCode(String voucherCode);

    @Query(value = "SELECT SUM(o.TotalAmount) AS TotalRevenue, COUNT(o.OrderId) AS TotalOrders " +
            "FROM Orders o " +
            "JOIN Shipments s ON o.OrderId = s.OrderId " +
            "WHERE o.OrderStatus = N'DELIVERED' " +
            "AND s.DeliveredAt BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    List<Object[]> findKpiDataBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT COALESCE(SUM(od.Quantity), 0) AS TotalProductsSold, COALESCE(SUM(od.Quantity * ctn.AverageImportPrice), 0) AS TotalCOGS " +
            "FROM Orders o " +
            "JOIN Shipments s ON o.OrderId = s.OrderId " +
            "JOIN OrderDetails od ON o.OrderId = od.OrderId " +
            "LEFT JOIN ( " +
            "    SELECT ProductId, AVG(ImportPrice) AS AverageImportPrice " +
            "    FROM ImportDetails " +
            "    GROUP BY ProductId " +
            ") ctn ON od.ProductId = ctn.ProductId " +
            "WHERE o.OrderStatus = N'DELIVERED' " +
            "AND s.DeliveredAt BETWEEN :startDate AND :endDate",
            nativeQuery = true)
    List<Object[]> findProductsAndCogsDataBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);


    @Query(value = "SELECT CAST(s.DeliveredAt AS DATE) AS RevenueDate, SUM(o.TotalAmount) AS Revenue " +
            "FROM Orders o " +
            "JOIN Shipments s ON o.OrderId = s.OrderId " +
            "WHERE o.OrderStatus = N'DELIVERED' " +
            "AND s.DeliveredAt BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(s.DeliveredAt AS DATE) " +
            "ORDER BY RevenueDate",
            nativeQuery = true)
    List<Object[]> findRevenueByDayBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT p.Name, p.ImageUrl, SUM(od.Quantity) AS TotalQuantity, SUM(od.UnitPrice * od.Quantity) AS TotalRevenue " +
            "FROM Orders o " +
            "JOIN Shipments s ON o.OrderId = s.OrderId " +
            "JOIN OrderDetails od ON o.OrderId = od.OrderId " +
            "JOIN Products p ON od.ProductId = p.ProductId " +
            "WHERE o.OrderStatus = N'DELIVERED' " +
            "AND s.DeliveredAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p.ProductId, p.Name, p.ImageUrl " +
            "ORDER BY TotalQuantity DESC " +
            "OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY",
            nativeQuery = true)
    List<Object[]> findTopSellingProductsBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate, @Param("limit") int limit);

    @Query(value = "SELECT c.CategoryName, SUM(od.UnitPrice * od.Quantity) AS TotalRevenue " +
            "FROM Orders o " +
            "JOIN Shipments s ON o.OrderId = s.OrderId " +
            "JOIN OrderDetails od ON o.OrderId = od.OrderId " +
            "JOIN Products p ON od.ProductId = p.ProductId " +
            "JOIN Categories c ON p.CategoryId = c.CategoryId " +
            "WHERE o.OrderStatus = N'DELIVERED' " +
            "AND s.DeliveredAt BETWEEN :startDate AND :endDate " +
            "GROUP BY c.CategoryId, c.CategoryName " +
            "ORDER BY TotalRevenue DESC",
            nativeQuery = true)
    List<Object[]> findRevenueByCategoryBetween(@Param("startDate") Timestamp startDate, @Param("endDate") Timestamp endDate);

    @Query(value = "SELECT CAST(CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END AS BIT) " +
            "FROM Orders o " +
            "JOIN OrderDetails od ON o.OrderId = od.OrderId " +
            "WHERE o.UserId = :userId AND od.ProductId = :productId AND o.OrderStatus = N'DELIVERED'",
            nativeQuery = true)
    boolean hasCompletedPurchase(@Param("userId") Integer userId, @Param("productId") Integer productId);

    @Query(value = "SELECT * FROM Orders o WHERE o.UserId = :userId " +
            "AND NOT (o.PaymentMethod = N'VN_PAY' AND o.PaymentStatus = N'UNPAID') /*#pageable*/",
            countQuery = "SELECT count(*) FROM Orders o WHERE o.UserId = :userId " +
                    "AND NOT (o.PaymentMethod = N'VN_PAY' AND o.PaymentStatus = N'UNPAID')",
            nativeQuery = true)
    Page<Order> findOrderHistoryForUserNative(@Param("userId") Integer userId, Pageable pageable);
}