/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.spring.Springweb.Service;

import com.spring.Springweb.Entity.Room;
import com.spring.Springweb.Repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author ADMIN
 */
@Service
@RequiredArgsConstructor
public class RoomsServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<Room> getAllRoom() {
        return roomRepository.findAll();
    }

}
