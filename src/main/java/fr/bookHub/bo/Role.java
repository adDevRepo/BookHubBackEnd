package fr.bookHub.bo;


import fr.bookHub.bo.enums.NomRole;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(of = { "id" })
@ToString

@Entity
@Table(name = "BOOKHUB_ROLE")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 20)
    private NomRole nom;

}
