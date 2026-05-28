package in.annapurnayojana.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "family_members", schema = "dbt_apy")
@Getter
@Setter
public class FamilyMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "is_hof", nullable = false)
    private boolean isHoF;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_of_family_id")
    private FamilyMember headOfFamily;

    @OneToMany(mappedBy = "headOfFamily", cascade = CascadeType.ALL)
    private List<FamilyMember> dependents = new ArrayList<>();

    // Point 1: Basic Information
    @Column(nullable = false)
    private boolean isChild;
    private String relationWithHeadOfFamily;
    
    @Column(nullable = false)
    private String memberName = "";
    
    @Column(nullable = false)
    private String aadhaarNo = "";
    private String mobileNo;
    private String dateOfBirth;
    private String gender;
    private String digitalRationCardNo;
    private String digitalRationCardType;
    private String socialCategory;
    
    private Boolean hasDigitalRationCard;
    private Boolean liftingMonthlyRation;

    // Point 2: Bank
    private String bankAccountNo;
    private String ifscCode;
    private String bankName;

    // Point 3: Voter
    private String epicNo;
    private String assemblyConstituencyNo;
    private String partNo;

    // Point 4: CAA
    private String caaApplicationStatus;
    private String caaApplicationNo;
    private String caaCertificateNo;

    // Point 5: SIR 2026
    @Column(name = "sir2026tribunal_status")
    private String sir2026TribunalStatus;
    
    @Column(name = "sir2026case_details")
    private String sir2026CaseDetails;
    
    // Partition Keys copied from Family
    @Column(name = "lgd_district_code")
    private Integer district;
    
    @Column(name = "lgd_block_mc_code")
    private Integer block;
    
    @Column(name = "lgd_gp_ward_code")
    private Integer gp;
    
    // Assets / Immovable Property
    private Boolean hasThreePuccaRooms;
    private Boolean ownsLand;
    @Column(precision = 10, scale = 2)
    private BigDecimal landholdingSizeDecimals;

    // Point 6: Vehicles
    @Column(nullable = false)
    private boolean hasFourWheeler;
    private Integer vehicleCount;
    private String vehicleRegistrationNo;
    private String vehicleModel;

    // Point 7: Health Insurance
    @Column(nullable = false)
    private boolean hasHealthInsurance;
    private String healthInsuranceType;
    @Column(precision = 15, scale = 2)
    private BigDecimal healthInsuranceSumAssured;
    @Column(precision = 15, scale = 2)
    private BigDecimal healthInsuranceAnnualPremium;

    // Point 8: Education Status
    private String literacyStatus;
    private String highestEducationalQualifications;
    private String schoolName;
    private String schoolGrade;
    private String schoolType;
    private String schoolTypeOther;

    // Point 9: Income/Profession
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberEmploymentNature> employmentNatures = new ArrayList<>();
    
    private String govtEmploymentType;
    @Column(precision = 15, scale = 2)
    private BigDecimal grossAnnualIncome;
    
    @Column(nullable = false)
    private boolean paysIncomeOrProfessionalTax;
    private Boolean hasPanCard;
    private String panName;
    private String panNo;
    private String professionalTaxProfession;
    
    @Column(nullable = false)
    private boolean holdsConstitutionalPost;
    private String constitutionalPostMemberNo;
    
    @Column(nullable = false)
    private boolean isRegisteredGst;
    private String gstin;
    
    @Column(nullable = false)
    private boolean isGovtPensioner;
    private String govtPensionerMemberNo;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberGovtScheme> govtSchemes = new ArrayList<>();
    
    private Boolean applyingForAnnapurnaBhandar;

    // Point 10: Other Identity Documents
    private String disabilityIdNo;
    private String disabilityIssuedDate;
    @Column(precision = 5, scale = 2)
    private BigDecimal disabilityPercentage;
    private String disabilityIssuingAuthority;
    
    private String otherIdNo;
    private String otherIdIssuedDate;
    private String otherIdIssuingAuthority;
    
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberOtherId> otherIds = new ArrayList<>();

    private String vaccinationStatus;
    private String vaccinationCardId;
    private String vaccinationSkipReasonOrDate;
}
