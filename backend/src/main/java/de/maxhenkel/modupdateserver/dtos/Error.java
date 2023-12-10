package de.maxhenkel.modupdateserver.dtos;

import lombok.*;

@Data
@NoArgsConstructor
public class Error {

    private int statusCode;
    private String message;

}
