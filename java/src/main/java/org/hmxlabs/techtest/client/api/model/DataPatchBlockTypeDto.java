package org.hmxlabs.techtest.client.api.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;

@JsonSerialize(as = DataPatchBlockTypeDto.class)
@AllArgsConstructor
@Getter
public class DataPatchBlockTypeDto {

    private String blockType;
}
