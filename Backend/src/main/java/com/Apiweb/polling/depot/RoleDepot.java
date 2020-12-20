package com.Apiweb.polling.depot;
import com.Apiweb.polling.model.Role;
import com.Apiweb.polling.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface RoleDepot extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
}
