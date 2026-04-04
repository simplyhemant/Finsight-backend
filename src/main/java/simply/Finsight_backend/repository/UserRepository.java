package simply.Finsight_backend.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import simply.Finsight_backend.entity.User;
import simply.Finsight_backend.enums.Role;
import simply.Finsight_backend.enums.UserStatus;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

//    Page<User> findByRoleAndStatus(
//            Role role,
//            UserStatus status,
//            Pageable pageable
//    );

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(u.name)  LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<User> searchByNameOrEmail(
            @Param("keyword") String keyword,
            Pageable pageable
    );

//    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
//    long countActiveUsers();
//
//    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'INACTIVE'")
//    long countInactiveUsers();
}