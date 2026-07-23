package tn.esprit.stagetest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.stagetest.entity.Equipment;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    List<Equipment> findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String serialNumber, String category);
}
