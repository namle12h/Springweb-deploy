/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import java.util.List;

import com.spring.Springweb.Entity.Staff;

/**
 *
 * @author ADMIN
 */
public interface StaffService {

    public List<Staff> getAllStaff();

    public Staff getByID(Integer id);

    public Staff createStaff(Staff staff);

    public Staff updateStaff(Integer id, Staff staff);

    public Staff deleteStaff(Integer id);

}
