package tn.esprit.stagetest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Equipment name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Serial number is required")
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    private String category;

    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private Employee employee;
}
