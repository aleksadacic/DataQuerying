package dev.rosemarylab.dataquerying.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dto {
    private Integer age;
    private String name;
    private Boolean superuser;
}
