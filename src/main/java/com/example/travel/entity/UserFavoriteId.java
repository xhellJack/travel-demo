package com.example.travel.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // Important for composite keys
public class UserFavoriteId implements Serializable {
    private static final long serialVersionUID = 1L; // Good practice for Serializable classes
    private Long user; // Corresponds to the type of User.id
    private Long attraction; // Corresponds to the type of Attraction.id
}