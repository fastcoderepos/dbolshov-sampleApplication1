package com.fastcode.dvdrental.application.core.store.dto;

import java.time.*;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStoreInput {

    @NotNull(message = "storeId Should not be null")
    private Integer storeId;

    private Integer addressId;
    private Long versiono;
}