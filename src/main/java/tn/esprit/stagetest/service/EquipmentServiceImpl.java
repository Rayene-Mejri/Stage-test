package tn.esprit.stagetest.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.stagetest.entity.Equipment;
import tn.esprit.stagetest.repository.EquipmentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> getAllEquipments() {
        return equipmentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipment> searchEquipments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEquipments();
        }
        String kw = keyword.trim();
        return equipmentRepository.findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrCategoryContainingIgnoreCase(kw, kw, kw);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Override
    public Equipment saveEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Override
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean serialNumberExists(String serialNumber) {
        return equipmentRepository.existsBySerialNumber(serialNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean serialNumberExistsForOther(String serialNumber, Long id) {
        return equipmentRepository.existsBySerialNumberAndIdNot(serialNumber, id);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return equipmentRepository.count();
    }
}
