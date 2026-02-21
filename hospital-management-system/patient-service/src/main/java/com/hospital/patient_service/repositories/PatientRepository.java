package com.hospital.patient_service.repositories;

import com.hospital.patient_service.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
