package de.maxhenkel.modupdateserver.entities;

import lombok.*;

@Data
@NoArgsConstructor
public class Error {

    private int statusCode;
    private String message;

}
