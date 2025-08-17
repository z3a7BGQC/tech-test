package org.hmxlabs.techtest.server.api.model;

import lombok.*;
import org.hmxlabs.techtest.server.persistence.BlockTypeEnum;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;


@JsonSerialize(as = DataHeader.class)
@JsonDeserialize(as = DataHeader.class)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataHeader {

    @NotBlank
    private String name;

    private BlockTypeEnum blockType;

    private String md5Checksum;

}
