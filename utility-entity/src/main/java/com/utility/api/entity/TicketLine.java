package com.utility.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketLine {

    private String name;
    private Integer quantity;
    private Float price;
    private Integer position;
}
