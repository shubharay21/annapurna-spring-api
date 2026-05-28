package in.annapurnayojana.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class FamilyMemberDto {
    
    @com.fasterxml.jackson.annotation.JsonProperty("isHoF")
    private boolean isHoF;
    @com.fasterxml.jackson.annotation.JsonProperty("isChild")
    private boolean isChild;
    private String relationWithHeadOfFamily;
    
    @NotEmpty(message = "Member Name is required.")
    private String memberName;
    
    // Using simple regex for Aadhaar since custom validator would require extra class
    @Pattern(regexp = "^(N/A|\\d{12})$", message = "Invalid Aadhaar number.")
    private String aadhaarNo;
    
    @Pattern(regexp = "^\\d{10}$", message = "Mobile number must be 10 digits.")
    private String mobileNo;
    
    private String dateOfBirth;
    private String gender;
    private String digitalRationCardNo;
    private String digitalRationCardType;
    private Boolean hasDigitalRationCard;
    private Boolean liftingMonthlyRation;
    private String socialCategory;
    
    @Pattern(regexp = "^\\d{9,18}$", message = "Bank Account number must be between 9 and 18 digits.")
    private String bankAccountNo;
    
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC Code format.")
    private String ifscCode;
    
    private String bankName;
    
    @Pattern(regexp = "^[A-Z]{3}[0-9]{7}$", message = "Invalid EPIC number format.")
    private String epicNo;
    
    private String assemblyConstituencyNo;
    private String partNo;
    
    private String caaApplicationStatus;
    private String caaApplicationNo;
    private String caaCertificateNo;
    
    private String sir2026TribunalStatus;
    private String sir2026CaseDetails;
    
    private Boolean hasThreePuccaRooms;
    private Boolean ownsLand;
    private BigDecimal landholdingSizeDecimals;
    
    private boolean hasFourWheeler;
    private Integer vehicleCount;
    private String vehicleRegistrationNo;
    private String vehicleModel;
    
    private boolean hasHealthInsurance;
    private String healthInsuranceType;
    private BigDecimal healthInsuranceSumAssured;
    private BigDecimal healthInsuranceAnnualPremium;
    
    private String literacyStatus;
    private String highestEducationalQualifications;
    private String schoolName;
    private String schoolGrade;
    private String schoolType;
    private String schoolTypeOther;
    
    private List<String> natureOfEmployment;
    private String govtEmploymentType;
    private BigDecimal grossAnnualIncome;
    
    private boolean paysIncomeOrProfessionalTax;
    private Boolean hasPanCard;
    private String panName;
    
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format.")
    private String panNo;
    
    private String professionalTaxProfession;
    
    private boolean holdsConstitutionalPost;
    private String constitutionalPostMemberNo;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isRegisteredGst")
    private boolean isRegisteredGst;
    private String gstin;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isGovtPensioner")
    private boolean isGovtPensioner;
    private String govtPensionerMemberNo;
    
    private List<GovtSchemeDto> govtSchemeBenefits;
    private Boolean applyingForAnnapurnaBhandar;
    
    private String disabilityIdNo;
    private String disabilityIssuedDate;
    private BigDecimal disabilityPercentage;
    private String disabilityIssuingAuthority;
    
    private String otherIdNo;
    private String otherIdIssuedDate;
    private String otherIdIssuingAuthority;
    
    private List<OtherIdDto> otherSpecificIds;
    
    private String vaccinationStatus;
    private String vaccinationCardId;
    private String vaccinationSkipReasonOrDate;
}
