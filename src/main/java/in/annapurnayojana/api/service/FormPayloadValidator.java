package in.annapurnayojana.api.service;

import in.annapurnayojana.api.dto.FamilyMemberDto;
import in.annapurnayojana.api.dto.FormSubmissionPayload;
import in.annapurnayojana.api.dto.GovtSchemeDto;
import in.annapurnayojana.api.dto.OtherIdDto;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Service
public class FormPayloadValidator {

    private static final int[][] VERHOEFF_D = {
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 2, 3, 4, 0, 6, 7, 8, 9, 5},
        {2, 3, 4, 0, 1, 7, 8, 9, 5, 6},
        {3, 4, 0, 1, 2, 8, 9, 5, 6, 7},
        {4, 0, 1, 2, 3, 9, 5, 6, 7, 8},
        {5, 9, 8, 7, 6, 0, 4, 3, 2, 1},
        {6, 5, 9, 8, 7, 1, 0, 4, 3, 2},
        {7, 6, 5, 9, 8, 2, 1, 0, 4, 3},
        {8, 7, 6, 5, 9, 3, 2, 1, 0, 4},
        {9, 8, 7, 6, 5, 4, 3, 2, 1, 0}
    };

    private static final int[][] VERHOEFF_P = {
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9},
        {1, 5, 7, 6, 2, 8, 3, 0, 9, 4},
        {5, 8, 0, 3, 7, 9, 6, 1, 4, 2},
        {8, 9, 1, 6, 0, 4, 3, 5, 2, 7},
        {9, 4, 5, 3, 1, 2, 6, 8, 7, 0},
        {4, 2, 8, 6, 5, 7, 3, 9, 0, 1},
        {2, 7, 9, 3, 8, 0, 6, 4, 1, 5},
        {7, 0, 4, 6, 9, 1, 3, 2, 5, 8}
    };

    public Map<String, List<String>> validate(FormSubmissionPayload payload) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        if (payload == null) {
            errors.put("global", List.of("Payload is empty."));
            return errors;
        }

        // 1. Family Level Validations
        List<String> familyErrors = new ArrayList<>();
        if (payload.getAddress() == null || payload.getAddress().trim().isEmpty()) {
            familyErrors.add("Permanent Address is required in Common Details.");
        }
        if (payload.getDistrict() == null) {
            familyErrors.add("District is required in Common Details.");
        }
        
        String areaType = payload.getAreaType();
        if (areaType == null || areaType.trim().isEmpty()) {
            familyErrors.add("Area Type (Rural/Urban) is required in Common Details.");
        } else if ("Rural".equalsIgnoreCase(areaType)) {
            if (payload.getBlock() == null) {
                familyErrors.add("Block is required for Rural area in Common Details.");
            }
            if (payload.getGp() == null) {
                familyErrors.add("Gram Panchayat is required for Rural area in Common Details.");
            }
        } else if ("Urban".equalsIgnoreCase(areaType)) {
            if (payload.getUlb() == null) {
                familyErrors.add("ULB / Municipality is required for Urban area in Common Details.");
            }
            if (payload.getWard() == null) {
                familyErrors.add("Ward is required for Urban area in Common Details.");
            }
        }

        if (payload.getTotalAnnualFamilyIncome() == null) {
            familyErrors.add("Total annual family income is required in Common Details.");
        }

        if (!payload.isAgreed()) {
            familyErrors.add("You must agree to the declaration in the Common Details Tab.");
        }

        if (payload.getTotalFamilyMembers() < 1 || payload.getTotalFamilyMembers() > 50) {
            familyErrors.add("Total Family Members must be between 1 and 50.");
        }

        List<FamilyMemberDto> members = payload.getMembers();
        if (members == null || members.isEmpty()) {
            familyErrors.add("At least one family member is required.");
        } else {
            long adultCount = members.stream().filter(m -> !m.isChild() || m.isHoF()).count();
            int literate = payload.getNoOfLiterateAdults() != null ? payload.getNoOfLiterateAdults() : 0;
            int illiterate = payload.getNoOfIlliterateAdults() != null ? payload.getNoOfIlliterateAdults() : 0;
            if ((literate + illiterate) != adultCount) {
                familyErrors.add("Please ensure all Adult family members have their Literacy Status explicitly selected in their Income / Profession tab.");
            }
        }

        if (!familyErrors.isEmpty()) {
            errors.put("family", familyErrors);
        }

        // 2. Member Level Validations
        if (members != null) {
            for (int i = 0; i < members.size(); i++) {
                FamilyMemberDto m = members.get(i);
                List<String> mErrors = new ArrayList<>();
                String prefix = m.isHoF() ? "Head of Family" : "Member " + (i + 1);

                if (m.getMemberName() == null || m.getMemberName().trim().isEmpty()) {
                    mErrors.add(prefix + ": Name is required.");
                }

                if (!m.isHoF() && (m.getRelationWithHeadOfFamily() == null || m.getRelationWithHeadOfFamily().trim().isEmpty())) {
                    mErrors.add(prefix + ": Relation with Head of Family is required.");
                }

                if (m.getDateOfBirth() == null || m.getDateOfBirth().trim().isEmpty()) {
                    mErrors.add(prefix + ": Date of Birth is required.");
                }

                if (m.getGender() == null || m.getGender().trim().isEmpty()) {
                    mErrors.add(prefix + ": Gender is required.");
                }

                if (m.getSocialCategory() == null || m.getSocialCategory().trim().isEmpty()) {
                    mErrors.add(prefix + ": Social Category is required.");
                }

                // Aadhaar checks
                String aadhaar = m.getAadhaarNo();
                if (aadhaar != null && !aadhaar.trim().isEmpty() && !"N/A".equalsIgnoreCase(aadhaar)) {
                    if (!validateAadhaar(aadhaar)) {
                        mErrors.add(prefix + ": Aadhaar number is invalid.");
                    }
                } else if (!m.isChild()) {
                    mErrors.add(prefix + ": Aadhaar number is required for adults.");
                } else if ("N/A".equalsIgnoreCase(aadhaar)) {
                    int age = calculateAge(m.getDateOfBirth());
                    if (age >= 5) {
                        mErrors.add(prefix + ": 'N/A' for Aadhaar is only allowed for children under 5 years of age.");
                    }
                }

                // Contact number checks
                String mobile = m.getMobileNo();
                if (m.isHoF()) {
                    if (mobile == null || mobile.trim().isEmpty()) {
                        mErrors.add(prefix + ": Contact number is required for Head of Family.");
                    } else if (!validateMobile(mobile)) {
                        mErrors.add(prefix + ": Contact number must be 10 digits.");
                    }
                } else if (mobile != null && !mobile.trim().isEmpty() && !validateMobile(mobile)) {
                    mErrors.add(prefix + ": Contact number must be 10 digits.");
                }

                // Annapurna Yojana eligibility (Adult Female checks)
                if ("Female".equalsIgnoreCase(m.getGender()) && !m.isChild()) {
                    if (m.getApplyingForAnnapurnaBhandar() == null) {
                        mErrors.add(prefix + ": Applying for Annapurna Yojana is a mandatory selection for adult females.");
                    }
                }

                // Adult vs Child specifics
                if (!m.isChild()) {
                    // EPIC
                    String epic = m.getEpicNo();
                    if (epic == null || epic.trim().isEmpty()) {
                        mErrors.add(prefix + ": EPIC (Voter ID) is required for adults.");
                    } else if (!validateEpic(epic)) {
                        mErrors.add(prefix + ": EPIC (Voter ID) number is invalid.");
                    }

                    if (m.getAssemblyConstituencyNo() == null || m.getAssemblyConstituencyNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": Assembly Constituency No. is required for adults.");
                    }
                    if (m.getPartNo() == null || m.getPartNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": Part No. is required for adults.");
                    }

                    // Bank Accounts
                    String bankAcc = m.getBankAccountNo();
                    if (bankAcc == null || bankAcc.trim().isEmpty()) {
                        mErrors.add(prefix + ": Bank Account number is required for adults.");
                    } else if (!validateBankAccount(bankAcc)) {
                        mErrors.add(prefix + ": Bank Account number must be between 9 and 18 digits.");
                    }

                    String ifsc = m.getIfscCode();
                    if (ifsc == null || ifsc.trim().isEmpty()) {
                        mErrors.add(prefix + ": IFSC Code is required for adults.");
                    } else if (!validateIfsc(ifsc)) {
                        mErrors.add(prefix + ": IFSC Code format is invalid.");
                    } else if (m.getBankName() == null || m.getBankName().trim().isEmpty() || "Invalid IFSC".equalsIgnoreCase(m.getBankName())) {
                        mErrors.add(prefix + ": Valid Bank Name is required.");
                    }

                    // Income / Tax / PAN details
                    if (m.getHasPanCard() == null) {
                        mErrors.add(prefix + ": Please select whether you have a PAN Card.");
                    } else if (m.getHasPanCard()) {
                        if (m.getPanName() == null || m.getPanName().trim().isEmpty()) {
                            mErrors.add(prefix + ": Name on PAN Card is required.");
                        }
                        if (m.getPanNo() == null || m.getPanNo().trim().isEmpty()) {
                            mErrors.add(prefix + ": PAN No. is required.");
                        } else if (!validatePan(m.getPanNo())) {
                            mErrors.add(prefix + ": PAN number is invalid.");
                        }
                    }

                    if (m.getNatureOfEmployment() == null || m.getNatureOfEmployment().isEmpty()) {
                        mErrors.add(prefix + ": Nature of Employment is required.");
                    }

                    String literacy = m.getLiteracyStatus();
                    if (literacy == null || literacy.trim().isEmpty()) {
                        mErrors.add(prefix + ": Literacy Status is required.");
                    } else if ("Literate".equalsIgnoreCase(literacy) && (m.getHighestEducationalQualifications() == null || m.getHighestEducationalQualifications().trim().isEmpty())) {
                        mErrors.add(prefix + ": Highest Educational Qualification is required.");
                    }

                    // Constitutional Post check
                    if (m.isHoldsConstitutionalPost() && (m.getConstitutionalPostMemberNo() == null || m.getConstitutionalPostMemberNo().trim().isEmpty())) {
                        mErrors.add(prefix + ": Member No. for constitutional post is required.");
                    }

                    // Govt Pension check
                    if (m.isGovtPensioner() && (m.getGovtPensionerMemberNo() == null || m.getGovtPensionerMemberNo().trim().isEmpty())) {
                        mErrors.add(prefix + ": Member No. for government pensioner is required.");
                    }

                    // GSTIN check
                    if (m.isRegisteredGst() && (m.getGstin() == null || m.getGstin().trim().isEmpty())) {
                        mErrors.add(prefix + ": GSTIN is required.");
                    }
                } else {
                    // Child member specific validations
                    if (m.getEpicNo() != null && !m.getEpicNo().trim().isEmpty() && !validateEpic(m.getEpicNo())) {
                        mErrors.add(prefix + ": EPIC (Voter ID) number is invalid.");
                    }
                    if (m.getBankAccountNo() != null && !m.getBankAccountNo().trim().isEmpty() && !validateBankAccount(m.getBankAccountNo())) {
                        mErrors.add(prefix + ": Bank Account number must be between 9 and 18 digits.");
                    }
                    if (m.getIfscCode() != null && !m.getIfscCode().trim().isEmpty() && !validateIfsc(m.getIfscCode())) {
                        mErrors.add(prefix + ": IFSC Code format is invalid.");
                    }
                }

                // Digital Ration Card Details
                if (m.getHasDigitalRationCard() == null) {
                    mErrors.add(prefix + ": Please select whether you have a Digital Ration Card.");
                } else if (m.getHasDigitalRationCard()) {
                    if (m.getDigitalRationCardType() == null || m.getDigitalRationCardType().trim().isEmpty()) {
                        mErrors.add(prefix + ": Digital Ration Card Type is required.");
                    }
                    if (m.getDigitalRationCardNo() == null || m.getDigitalRationCardNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": Ration Card No. is required.");
                    }
                }

                if (m.getLiftingMonthlyRation() == null) {
                    mErrors.add(prefix + ": Please select whether family is lifting monthly ration.");
                }

                // Member Assets details
                if (m.getHasThreePuccaRooms() == null) {
                    mErrors.add(prefix + ": Please select if the house has ≥3 pucca rooms.");
                }

                if (m.getOwnsLand() == null) {
                    mErrors.add(prefix + ": Please select whether the family owns any land.");
                } else if (m.getOwnsLand() && m.getLandholdingSizeDecimals() == null) {
                    mErrors.add(prefix + ": Landholding size is required when owning land.");
                }

                if (m.isHasFourWheeler()) {
                    if (m.getVehicleCount() == null) {
                        mErrors.add(prefix + ": Number of Vehicles is required.");
                    }
                    if (m.getVehicleModel() == null || m.getVehicleModel().trim().isEmpty()) {
                        mErrors.add(prefix + ": Vehicle Model is required.");
                    }
                    if (m.getVehicleRegistrationNo() == null || m.getVehicleRegistrationNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": Vehicle Registration No(s). is required.");
                    }
                }

                if (m.isHasHealthInsurance()) {
                    if (m.getHealthInsuranceType() == null || m.getHealthInsuranceType().trim().isEmpty()) {
                        mErrors.add(prefix + ": Insurance Type is required.");
                    }
                    if (m.getHealthInsuranceSumAssured() == null) {
                        mErrors.add(prefix + ": Insurance Sum Assured is required.");
                    }
                    if (m.getHealthInsuranceAnnualPremium() == null) {
                        mErrors.add(prefix + ": Insurance Annual Premium is required.");
                    }
                }

                // Identity / CAA & SIR Details
                String caaStatus = m.getCaaApplicationStatus();
                if ("Applied".equalsIgnoreCase(caaStatus)) {
                    if (m.getCaaApplicationNo() == null || m.getCaaApplicationNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": CAA Application No. is required when status is Applied.");
                    }
                } else if ("Issued".equalsIgnoreCase(caaStatus)) {
                    if (m.getCaaCertificateNo() == null || m.getCaaCertificateNo().trim().isEmpty()) {
                        mErrors.add(prefix + ": CAA Certificate No. is required when status is Issued.");
                    }
                }

                List<OtherIdDto> otherIds = m.getOtherSpecificIds();
                if (otherIds != null && !otherIds.isEmpty()) {
                    for (int k = 0; k < otherIds.size(); k++) {
                        OtherIdDto oId = otherIds.get(k);
                        if (oId.getIdType() == null || oId.getIdType().trim().isEmpty()) {
                            mErrors.add(prefix + ": ID Type is required for Other Specific ID #" + (k + 1) + ".");
                        }
                        if (oId.getIssueDate() == null || oId.getIssueDate().trim().isEmpty()) {
                            mErrors.add(prefix + ": Date of issue is required for Other Specific ID #" + (k + 1) + ".");
                        }
                    }
                }

                if ("Yes".equalsIgnoreCase(m.getSir2026TribunalStatus())) {
                    if (m.getSir2026CaseDetails() == null || m.getSir2026CaseDetails().trim().isEmpty()) {
                        mErrors.add(prefix + ": Case Details are required for SIR 2026 pending cases.");
                    }
                }

                // Govt Schemes
                if (m.getGovtSchemeBenefits() != null && !m.getGovtSchemeBenefits().isEmpty()) {
                    List<GovtSchemeDto> schemes = m.getGovtSchemeBenefits();
                    for (int k = 0; k < schemes.size(); k++) {
                        GovtSchemeDto sch = schemes.get(k);
                        if (sch == null || sch.getSchemeName() == null || sch.getSchemeName().trim().isEmpty()) {
                            mErrors.add(prefix + ": Scheme Name is required for Scheme " + (k + 1) + ".");
                        }
                    }
                }

                // Social Status (Child only)
                if (m.isChild()) {
                    // School details
                    boolean hasSchoolField = (m.getSchoolGrade() != null && !m.getSchoolGrade().trim().isEmpty()) ||
                                            (m.getSchoolName() != null && !m.getSchoolName().trim().isEmpty()) ||
                                            (m.getSchoolType() != null && !m.getSchoolType().trim().isEmpty());
                    if (hasSchoolField) {
                        if (m.getSchoolGrade() == null || m.getSchoolGrade().trim().isEmpty()) {
                            mErrors.add(prefix + ": Grade is required if attending school.");
                        }
                        if (m.getSchoolName() == null || m.getSchoolName().trim().isEmpty()) {
                            mErrors.add(prefix + ": School Name is required if attending school.");
                        }
                        if (m.getSchoolType() == null || m.getSchoolType().trim().isEmpty()) {
                            mErrors.add(prefix + ": School Type is required if attending school.");
                        } else if ("Others".equalsIgnoreCase(m.getSchoolType())) {
                            if (m.getSchoolTypeOther() == null || m.getSchoolTypeOther().trim().isEmpty()) {
                                mErrors.add(prefix + ": Please specify the Other School Type.");
                            }
                        }
                    }

                    // Vaccination
                    String vacStatus = m.getVaccinationStatus();
                    if (vacStatus == null || vacStatus.trim().isEmpty()) {
                        mErrors.add(prefix + ": Vaccination Status is required.");
                    } else if ("Yes".equalsIgnoreCase(vacStatus)) {
                        if (m.getVaccinationCardId() == null || m.getVaccinationCardId().trim().isEmpty()) {
                            mErrors.add(prefix + ": Vaccination Card ID is required.");
                        }
                    } else if ("No".equalsIgnoreCase(vacStatus) || "Not Vaccinated".equalsIgnoreCase(vacStatus)) {
                        if (m.getVaccinationSkipReasonOrDate() == null || m.getVaccinationSkipReasonOrDate().trim().isEmpty()) {
                            mErrors.add(prefix + ": Last vaccination date / reason for skip is required.");
                        }
                    }
                }

                if (!mErrors.isEmpty()) {
                    errors.put("members[" + i + "]", mErrors);
                }
            }
        }

        return errors;
    }

    private boolean validateAadhaar(String aadhaar) {
        if (aadhaar == null || !aadhaar.matches("^\\d{12}$")) {
            return false;
        }
        int c = 0;
        for (int i = 0; i < aadhaar.length(); i++) {
            int digit = Character.getNumericValue(aadhaar.charAt(aadhaar.length() - 1 - i));
            c = VERHOEFF_D[c][VERHOEFF_P[i % 8][digit]];
        }
        return c == 0;
    }

    private boolean validateMobile(String mobile) {
        return mobile != null && mobile.matches("^\\d{10}$");
    }

    private boolean validateEpic(String epic) {
        return epic != null && epic.matches("^[A-Z]{3}[0-9]{7}$");
    }

    private boolean validateBankAccount(String account) {
        return account != null && account.matches("^\\d{9,18}$");
    }

    private boolean validateIfsc(String ifsc) {
        return ifsc != null && ifsc.matches("^[A-Z]{4}0[A-Z0-9]{6}$");
    }

    private boolean validatePan(String pan) {
        return pan != null && pan.matches("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    }

    private int calculateAge(String dobString) {
        if (dobString == null || dobString.trim().isEmpty()) {
            return 999;
        }
        try {
            LocalDate dob = LocalDate.parse(dobString);
            LocalDate today = LocalDate.now();
            return Period.between(dob, today).getYears();
        } catch (Exception e) {
            return 999;
        }
    }
}
