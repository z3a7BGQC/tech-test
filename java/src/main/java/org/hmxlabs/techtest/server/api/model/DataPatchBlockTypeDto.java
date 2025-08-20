package org.hmxlabs.techtest.server.api.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import org.hmxlabs.techtest.server.validation.EnumValidator;

@Getter
@AllArgsConstructor
public class DataPatchBlockTypeDto {

    @NotNull
    @EnumValidator(enumClazz = BlockTypeEnum.class, message = "Invalid BlockType Enum")
    private String blockType;
}