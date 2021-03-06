package com.hotel.controller;


import com.hotel.dto.room.RoomFormDto;
import com.hotel.dto.room.RoomSearchDto;
import com.hotel.entity.Room;
import com.hotel.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /**
     * (관리자) 객실 추가 페이지
     */
    @GetMapping(value = "/admin/room/new")
    public String roomForm(Model model){
        model.addAttribute("roomFormDto", new RoomFormDto());
        return "room/roomForm";
    }

    /**
     * (관리자) 객실 추가
     */
    @PostMapping(value = "/admin/room/new")
    public String roomNew(@Valid RoomFormDto roomFormDto, BindingResult bindingResult, Model model,
                          @RequestParam("roomImgFile") List<MultipartFile> roomImgFileList){

        if(bindingResult.hasErrors()){
            return "room/roomForm";
        }

        //객실 이미지 확인
        if(roomImgFileList.get(0).isEmpty() && roomFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 객실 이미지는 필수 입력 값 입니다");
            return "room/roomForm";
        }

        // 객실 추가
        try{
            roomService.saveRoom(roomFormDto, roomImgFileList);
        }catch (Exception e){
            model.addAttribute("errorMessage", "객실 등록 중 에러가 발생하였습니다");
            return "room/roomForm";
        }

        return "redirect:/admin/rooms";
    }

    /**
     * (관리자) 객실 전체 조회
     */
    @GetMapping(value = {"/admin/rooms", "/admin/rooms/{page}"})
    public String roomManage(RoomSearchDto roomSearchDto, @PathVariable("page") Optional<Integer> page, Model model){

        Pageable pageable = PageRequest.of(page.isPresent()? page.get() : 0, 4);
        Page<Room> rooms = roomService.getAdminRoomPage(roomSearchDto, pageable);
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomSearchDto", roomSearchDto);
        model.addAttribute("maxPage", 5);
        return "room/rooms";
    }

    /**
     * (관리자) 객실 정보 상세보기
     */
    @GetMapping(value = "/admin/room/{roomId}")
    public String roomDtl(@PathVariable("roomId") Long roomId, Model model){

        try{
            RoomFormDto roomFormDto = roomService.getRoomDtl(roomId);
            model.addAttribute("roomFormDto", roomFormDto);
        }catch (EntityNotFoundException e){
            model.addAttribute("errorMessage", "존재하지 않는 객실입니다");
            model.addAttribute("roomFormDto", new RoomFormDto());
            return "room/roomForm";
        }

        return "room/roomForm";
    }

    /**
     * (관리자) 객실 정보 수정
     */
    @PostMapping(value = "/admin/room/{roomId}")
    public String roomUpdate(@Valid RoomFormDto roomFormDto, BindingResult bindingResult, Model model,
                             @RequestParam("roomImgFile") List<MultipartFile> roomImgFileList){

        if(bindingResult.hasErrors()){
            return "room/roomForm";
        }

        //객실 이미지 확인
        if(roomImgFileList.get(0).isEmpty() && roomFormDto.getId() == null){
            model.addAttribute("errorMessage", "첫번째 상품 이미지는 필수 입력 값 입니다.");
            return "room/roomForm";
        }

        // 객실 업데이트
        try {
            roomService.updateRoom(roomFormDto, roomImgFileList);
        } catch (Exception e){
            model.addAttribute("errorMessage", "상품 수정 중 에러가 발생하였습니다.");
            return "room/roomForm";

        }

        return "redirect:/admin/rooms";
    }


    /**
     *  (관리자) 객실 삭제
     */
    @DeleteMapping(value= "/admin/room/delete/{roomId}")
    public @ResponseBody
    ResponseEntity roomDelete(@PathVariable("roomId") Long roomId){
        roomService.deleteRoom(roomId);
        return new ResponseEntity<Long>(roomId, HttpStatus.OK);
    }



}
