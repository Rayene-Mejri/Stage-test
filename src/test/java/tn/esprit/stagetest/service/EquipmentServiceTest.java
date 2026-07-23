package tn.esprit.stagetest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.stagetest.entity.Equipment;
import tn.esprit.stagetest.repository.EquipmentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    private Equipment equipment;

    @BeforeEach
    void setUp() {
        equipment = Equipment.builder()
                .id(1L)
                .name("MacBook Pro")
                .serialNumber("SN-12345")
                .category("Laptop")
                .build();
    }

    @Test
    void testGetAllEquipments() {
        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));

        List<Equipment> result = equipmentService.getAllEquipments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSerialNumber()).isEqualTo("SN-12345");
    }

    @Test
    void testSaveEquipment() {
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment saved = equipmentService.saveEquipment(equipment);

        assertThat(saved).isNotNull();
        assertThat(saved.getCategory()).isEqualTo("Laptop");
    }
}
