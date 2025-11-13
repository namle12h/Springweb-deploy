/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.DTO.AppointmentRequest;
import com.spring.Springweb.DTO.AppointmentResponse;
import java.util.List;

import com.spring.Springweb.Entity.Appointment;
import com.spring.Springweb.Entity.User;
import org.springframework.data.domain.Page;

public interface AppointmentService {

    AppointmentResponse createAppointment(AppointmentRequest req);

//    Optional<Appointment> getAppointmentbyId(Integer customerId);
    public AppointmentResponse getAppointmentbyId(Integer id);

    List<Appointment> getAllAppointments();

    public Page<AppointmentResponse> getAllAppointments(int page, int limit);

//    public Appointment updateAppointment(Integer id, Appointment appointmentDetails);
    public AppointmentResponse updateAppointment(Integer id, Appointment appointmentDetails);

    public void deleteAppointment(Integer id);
    List<AppointmentResponse> getAppointmentsByCustomer(String email);
    public AppointmentResponse cancelAppointment(Integer id, User currentUser);

}
