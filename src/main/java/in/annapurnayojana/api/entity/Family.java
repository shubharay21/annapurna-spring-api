package in.annapurnayojana.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "families", schema = "dbt_apy")
@Getter
@Setter
public class Family {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID applicationId = UUID.randomUUID();

    // Basic Demographics
    private String address;
    
    @Column(name = "lgd_district_code")
    private Integer district;
    
    private String areaType;
    
    @Column(name = "lgd_block_mc_code")
    private Integer block;
    
    @Column(name = "lgd_gp_ward_code")
    private Integer gp;
    
    private Integer ulb;
    
    @Transient
    private Integer ward;

    @Column(nullable = false)
    private int totalFamilyMembers;
    private Integer noOfLiterateAdults;
    private Integer noOfIlliterateAdults;

    // Ration Card / Subsidy
    @Column(nullable = false)
    private boolean hasDigitalRationCard;
    private String rationCardHouseholdId;
    
    @Column(nullable = false)
    private boolean liftingMonthlyRation;

    // Electricity
    @Column(nullable = false)
    private boolean hasElectricityConnection;
    private String electricityConsumerId;
    private Integer powerUnitsConsumed;

    // Total Income
    private Integer totalAnnualFamilyIncome;

    // Declaration & Consent
    @Column(nullable = false)
    private boolean isAgreed;

    // Status
    @Column(name = "application_status", nullable = false)
    private String status = "DRAFT";

    public void setWard(Integer ward) {
        this.ward = ward;
        if (ward != null) {
            this.gp = ward; // Save to the shared lgd_gp_ward_code column
        }
    }

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Navigation
    @OneToMany(mappedBy = "family", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FamilyMember> members = new ArrayList<>();
}
