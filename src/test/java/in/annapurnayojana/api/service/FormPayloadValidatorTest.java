package in.annapurnayojana.api.service;

import in.annapurnayojana.api.dto.FamilyMemberDto;
import in.annapurnayojana.api.dto.FormSubmissionPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FormPayloadValidatorTest {

    private FormPayloadValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FormPayloadValidator();
    }

    @Test
    void testEmptyPayload() {
        Map<String, List<String>> errors = validator.validate(null);
        assertTrue(errors.containsKey("global"));
        assertEquals("Payload is empty.", errors.get("global").get(0));
    }

    @Test
    void testInvalidFamilyFields() {
        FormSubmissionPayload payload = new FormSubmissionPayload();
        payload.setAddress("");
        payload.setDistrict(null);
        payload.setAreaType("Rural");
        payload.setBlock(null);
        payload.setGp(null);
        payload.setTotalAnnualFamilyIncome(null);
        payload.setAgreed(false);

        Map<String, List<String>> errors = validator.validate(payload);
        assertTrue(errors.containsKey("family"));
        List<String> familyErrors = errors.get("family");
        assertTrue(familyErrors.contains("Permanent Address is required in Common Details."));
        assertTrue(familyErrors.contains("District is required in Common Details."));
        assertTrue(familyErrors.contains("Block is required for Rural area in Common Details."));
        assertTrue(familyErrors.contains("Gram Panchayat is required for Rural area in Common Details."));
        assertTrue(familyErrors.contains("Total annual family income is required in Common Details."));
        assertTrue(familyErrors.contains("You must agree to the declaration in the Common Details Tab."));
    }

    @Test
    void testAadhaarValidation() {
        FormSubmissionPayload payload = new FormSubmissionPayload();
        payload.setAddress("Test Address");
        payload.setDistrict(1);
        payload.setAreaType("Urban");
        payload.setUlb(1);
        payload.setWard(1);
        payload.setTotalAnnualFamilyIncome(100000);
        payload.setAgreed(true);
        payload.setTotalFamilyMembers(1);

        FamilyMemberDto hof = new FamilyMemberDto();
        hof.setHoF(true);
        hof.setChild(false);
        hof.setMemberName("HOF Name");
        hof.setRelationWithHeadOfFamily("Self");
        hof.setDateOfBirth("1980-01-01");
        hof.setGender("Male");
        hof.setSocialCategory("General");
        hof.setAadhaarNo("123456789012"); // Invalid Aadhaar checksum
        hof.setMobileNo("9876543210");
        hof.setEpicNo("ABC1234567");
        hof.setAssemblyConstituencyNo("1");
        hof.setPartNo("1");
        hof.setBankAccountNo("1234567890");
        hof.setIfscCode("SBIN0001234");
        hof.setBankName("State Bank of India");
        hof.setHasPanCard(false);
        hof.setHasDigitalRationCard(false);
        hof.setLiftingMonthlyRation(false);
        hof.setHasThreePuccaRooms(false);
        hof.setOwnsLand(false);
        hof.setNatureOfEmployment(List.of("Self Employed"));
        hof.setLiteracyStatus("Literate");
        hof.setHighestEducationalQualifications("Graduate");

        List<FamilyMemberDto> members = new ArrayList<>();
        members.add(hof);
        payload.setMembers(members);
        payload.setNoOfLiterateAdults(1);
        payload.setNoOfIlliterateAdults(0);

        Map<String, List<String>> errors = validator.validate(payload);
        assertTrue(errors.containsKey("members[0]"));
        List<String> memberErrors = errors.get("members[0]");
        assertTrue(memberErrors.contains("Head of Family: Aadhaar number is invalid."));
    }
}
