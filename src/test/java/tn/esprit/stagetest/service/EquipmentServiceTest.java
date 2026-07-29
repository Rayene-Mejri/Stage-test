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
import static org.mockito.ArgumentMatchers.anyString;
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
    void testSearchEquipments_NullKeyword() {
        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        List<Equipment> result = equipmentService.searchEquipments(null);
        assertThat(result).hasSize(1);
        verify(equipmentRepository, never()).findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrCategoryContainingIgnoreCase(anyString(), anyString(), anyString());
    }

    @Test
    void testSearchEquipments_EmptyKeyword() {
        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));
        List<Equipment> result = equipmentService.searchEquipments("   ");
        assertThat(result).hasSize(1);
        verify(equipmentRepository, never()).findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrCategoryContainingIgnoreCase(anyString(), anyString(), anyString());
    }

    @Test
    void testSearchEquipments_ValidKeyword() {
        when(equipmentRepository.findByNameContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrCategoryContainingIgnoreCase("MacBook", "MacBook", "MacBook"))
                .thenReturn(List.of(equipment));
        
        List<Equipment> result = equipmentService.searchEquipments(" MacBook ");
        assertThat(result).hasSize(1);
    }

    @Test
    void testGetEquipmentById_Found() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        Optional<Equipment> result = equipmentService.getEquipmentById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("MacBook Pro");
    }

    @Test
    void testGetEquipmentById_NotFound() {
        when(equipmentRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<Equipment> result = equipmentService.getEquipmentById(2L);
        assertThat(result).isEmpty();
    }

    @Test
    void testSaveEquipment() {
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);

        Equipment saved = equipmentService.saveEquipment(equipment);

        assertThat(saved).isNotNull();
        assertThat(saved.getCategory()).isEqualTo("Laptop");
    }

    @Test
    void testDeleteEquipment() {
        doNothing().when(equipmentRepository).deleteById(1L);
        equipmentService.deleteEquipment(1L);
        verify(equipmentRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSerialNumberExists() {
        when(equipmentRepository.existsBySerialNumber("SN-12345")).thenReturn(true);
        boolean exists = equipmentService.serialNumberExists("SN-12345");
        assertThat(exists).isTrue();
    }

    @Test
    void testSerialNumberExistsForOther() {
        when(equipmentRepository.existsBySerialNumberAndIdNot("SN-12345", 2L)).thenReturn(false);
        boolean exists = equipmentService.serialNumberExistsForOther("SN-12345", 2L);
        assertThat(exists).isFalse();
    }

    @Test
    void testCount() {
        when(equipmentRepository.count()).thenReturn(3L);
        long count = equipmentService.count();
        assertThat(count).isEqualTo(3L);
    }
}
