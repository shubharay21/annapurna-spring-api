package in.annapurnayojana.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FormSubmissionPayload {

    private UUID applicationId;
    
    // Demographics
    private String address;
    private Integer district;
    private String areaType;
    private Integer block;
    private Integer gp;
    private Integer ulb;
    private Integer ward;
    
    @Range(min = 1, max = 50, message = "Total Family Members must be between 1 and 50.")
    private int totalFamilyMembers;
    
    private Integer noOfLiterateAdults;
    private Integer noOfIlliterateAdults;
    
    // Ration
    private boolean hasDigitalRationCard;
    private String rationCardHouseholdId;
    private boolean liftingMonthlyRation;
    
    // Electricity
    private boolean hasElectricityConnection;
    private String electricityConsumerId;
    private Integer powerUnitsConsumed;
    
    // Total Income
    private Integer totalAnnualFamilyIncome;
    
    // Declaration
    @com.fasterxml.jackson.annotation.JsonProperty("isAgreed")
    private boolean isAgreed;
    
    @NotEmpty(message = "At least one family member is required.")
    @Valid
    private List<FamilyMemberDto> members;

    // Custom validation logic can be checked in the controller or a custom constraint validator.
    public boolean isValidDemographics() {
        int literate = noOfLiterateAdults != null ? noOfLiterateAdults : 0;
        int illiterate = noOfIlliterateAdults != null ? noOfIlliterateAdults : 0;
        return (literate + illiterate) == totalFamilyMembers;
    }
}
