package com.eswar.userservice.entity;


import com.eswar.userservice.audit.BaseEntity;
import com.eswar.userservice.constants.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true,callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ToString.Include
    @EqualsAndHashCode.Include
    private UUID id;




    @Column(name = "first_name", nullable = false,length = 100)
    @ToString.Include
    private String firstName;



    @Column(name = "last_name", nullable = false,length = 100)
    @ToString.Include
    private String lastName;


    @Column(name = "email", nullable = false, unique = true,length = 150)
    @ToString.Include
    private String email;



    @Column(name = "country_code", nullable = false,length = 5)
    @ToString.Include
    private String countryCode;


    @Column(name = "phone_number", nullable = false, unique = true,length = 15)
    @ToString.Include
    private String phoneNumber;


    @Column(name = "address_street",length = 100)
    @ToString.Include
    private String addressStreet;



    @Column(name = "address_city",length = 100)
    @ToString.Include
    private String addressCity;

    @Size(max = 100)
    @Column(name = "address_country")
    @ToString.Include
    private String addressCountry;

    @Column(name = "address_zip_code",length = 100)
    @ToString.Include
    private String addressZipCode;

    @Column(name = "last_seen")
    @ToString.Include
    private Instant lastSeen;

    @Column(name = "password")
    @ToString.Exclude
    String password;

    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"})
    )
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = new HashSet<>();

}
