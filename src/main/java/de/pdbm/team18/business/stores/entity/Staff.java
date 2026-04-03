package de.pdbm.team18.business.stores.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "staffs")
public class Staff {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column
    private String email;

    @Column
    private int active;

    @Column
    private String phone;


    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Staff managerId;

    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store storeId;

}
