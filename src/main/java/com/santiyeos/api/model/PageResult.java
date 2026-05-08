package com.santiyeos.api.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult <T>{
    //generic class for pagination
    private List<T> items;
    private Integer total;
    private Integer limit;
    private Integer offset;

}
