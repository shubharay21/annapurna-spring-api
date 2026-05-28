package in.annapurnayojana.api.repository;

import in.annapurnayojana.api.entity.Family;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByApplicationId(UUID applicationId);
    boolean existsByApplicationId(UUID applicationId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Family f JOIN f.members m WHERE m.isHoF = true AND m.mobileNo = :mobileNo")
    boolean existsByHoFMobileNo(@Param("mobileNo") String mobileNo);
}
