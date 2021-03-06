package com.hotel.service;

import com.hotel.dto.reservation.ReservationMainDto;
import com.hotel.dto.room.RoomFormDto;
import com.hotel.dto.room.RoomImgDto;
import com.hotel.dto.room.RoomSearchDto;
import com.hotel.entity.Reservation;
import com.hotel.entity.Room;
import com.hotel.entity.RoomImg;
import com.hotel.repository.reservation.ReservationRepository;
import com.hotel.repository.room.RoomImgRepository;
import com.hotel.repository.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomImgService roomImgService;
    private final RoomImgRepository roomImgRepository;
    private final ReservationRepository reservationRepository;


    /**
     * 객실 등록
     * @param roomFormDto
     * @param roomImgFileList
     * @return
     * @throws Exception
     */
    public Long saveRoom(RoomFormDto roomFormDto, List<MultipartFile> roomImgFileList) throws Exception{

        //객실 저장
        Room room = roomFormDto.createRoom();
        roomRepository.save(room);

        //객실 이미지 저장
        for(int i =0; i< roomImgFileList.size();i++){
            RoomImg roomImg = new RoomImg();
            roomImg.setRoom(room);
            if(i == 0){
                roomImg.setRepimgYn("Y");
            }else{
                roomImg.setRepimgYn("N");
            }
            roomImgService.saveRoomImg(roomImg, roomImgFileList.get(i));
        }

        return room.getId();
    }

    /**
     * 객실 전체 조회
     * @param roomSearchDto
     * @param pageable
     * @return
     */
    @Transactional(readOnly=true)
    public Page<Room> getAdminRoomPage(RoomSearchDto roomSearchDto, Pageable pageable){
        return roomRepository.getAdminRoomPage(roomSearchDto, pageable);
    }

    /**
     * 객실 정보 상세보기
     *
     * @param roomId
     * @return
     */
    @Transactional(readOnly = true)
    public RoomFormDto getRoomDtl(Long roomId){

        //객실 이미지 조회
        List<RoomImg> roomImgList = roomImgRepository.findByRoomIdOrderByIdAsc(roomId);
        List<RoomImgDto> roomImgDtoList = new ArrayList<>();
        for(RoomImg roomImg: roomImgList){
            RoomImgDto roomImgDto = RoomImgDto.of(roomImg);
            roomImgDtoList.add(roomImgDto);
        }

        //객실 정보 조회
        Room room = roomRepository.findById(roomId).orElseThrow(EntityNotFoundException::new);
        RoomFormDto roomFormDto = RoomFormDto.of(room);
        roomFormDto.setRoomImgDtoList(roomImgDtoList);
        return roomFormDto;

    }

    /**
     * 객실 수정
     * @param roomFormDto
     * @param roomImgFileList
     * @return
     * @throws Exception
     */
    public Long updateRoom(RoomFormDto roomFormDto, List<MultipartFile> roomImgFileList) throws Exception{
        //객실 수정
        Room room = roomRepository.findById(roomFormDto.getId())
                .orElseThrow(EntityNotFoundException::new);
        room.updateRoom(roomFormDto);
        List<Long> roomImgIds = roomFormDto.getRoomImgIds();

        //객실 이미지 수젇
        for(int i=0; i < roomImgFileList.size(); i++){
            roomImgService.updateRoomImg(roomImgIds.get(i), roomImgFileList.get(i));
        }

        return room.getId();
    }


    /**
     * 객실 삭제
     * @param roomId
     */
    public void deleteRoom(Long roomId){

        //객실 이미지 삭제
        List<RoomImg> roomImgList = roomImgRepository.findByRoomIdOrderByIdAsc(roomId);
        for(RoomImg roomImg: roomImgList){
            roomImgRepository.delete(roomImg);
        }

        //객실 예약 삭제
        List<Reservation> reservationList = reservationRepository.findByRoomIdOrderByIdAsc(roomId);
        for(Reservation reservation: reservationList){
            reservationRepository.delete(reservation);
        }

        //객실 삭제
        Room room = roomRepository.findById(roomId).orElseThrow(EntityNotFoundException::new);
        roomRepository.delete(room);
    }

    /**
     * (관리자 회원) 예약 가능한 객실 조회
     * @param roomSearchDto
     * @param pageable
     * @return
     */
    @Transactional(readOnly=true)
    public Page<ReservationMainDto> getReserveRoomPage(RoomSearchDto roomSearchDto, Pageable pageable){
        return roomRepository.getReserveRoomPage(roomSearchDto, pageable);
    }


}
