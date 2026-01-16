package com.example.be.dto.commission;

import lombok.Data;
import java.util.List;

@Data
public class CommissionRequestDTO {

    private String title;
    private String description;

    private String contactMethod;
    private String contactValue;

    private List<CommissionItemDTO> items;
}
