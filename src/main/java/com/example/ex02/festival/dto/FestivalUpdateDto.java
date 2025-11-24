package com.example.ex02.festival.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FestivalUpdateDto {
    private Long festivalNo;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private String addr;
    private String festivalFee;
    private String host;
    private String hostTel;
    private String homepage;
    private String infotext1;
    private String infotext2; // 위에서 내용 textarea도 name="infotext2"로 맞춰두면 같이 수정 가능
}
