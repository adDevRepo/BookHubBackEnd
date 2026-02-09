package fr.bookHub.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotNull(message = "L'ID du livre est obligatoire")
        Integer livreId
) {}