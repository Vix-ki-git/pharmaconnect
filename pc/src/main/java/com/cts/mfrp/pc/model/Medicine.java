package com.cts.mfrp.pc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "medicine")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false, columnDefinition = "VARCHAR(36)")
    private String id;

    private String name;

    @Column(name = "generic_name")
    private String genericName;

    private String category;
    private String manufacturer;

    @Column(name = "dosage_form")
    private String dosageForm;

    private String strength;
}