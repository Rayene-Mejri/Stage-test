package tn.esprit.stagetest.service;

import tn.esprit.stagetest.entity.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentService {
    List<Equipment> getAllEquipments();
    List<Equipment> searchEquipments(String keyword);
    Optional<Equipment> getEquipmentById(Long id);
    Equipment saveEquipment(Equipment equipment);
    void deleteEquipment(Long id);
    boolean serialNumberExists(String serialNumber);
    boolean serialNumberExistsForOther(String serialNumber, Long id);
    long count();
}
