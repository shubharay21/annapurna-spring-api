package in.annapurnayojana.api.service;

import in.annapurnayojana.api.dto.FormSubmissionPayload;
import in.annapurnayojana.api.entity.*;
import in.annapurnayojana.api.repository.FamilyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.features.useRabbitMq", havingValue = "false", matchIfMissing = true)
public class DirectDbSubmissionStrategy implements FormSubmissionStrategy {

    private static final Logger logger = LoggerFactory.getLogger(DirectDbSubmissionStrategy.class);
    private final FamilyRepository familyRepository;

    public DirectDbSubmissionStrategy(FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
        logger.info(">>> [Feature] Form submission strategy: Direct DB (sync)");
    }

    @Override
    @Transactional
    public void submit(FormSubmissionPayload msg) {
        UUID appId = msg.getApplicationId();

        // Idempotency: skip if already persisted
        if (appId != null && familyRepository.existsByApplicationId(appId)) {
            logger.warn("Duplicate submission skipped (direct-db). ApplicationId={}", appId);
            return;
        }

        Family family = new Family();
        if (appId != null) {
            family.setApplicationId(appId);
        } else {
            family.setApplicationId(UUID.randomUUID());
        }
        
        family.setAddress(msg.getAddress());
        family.setDistrict(msg.getDistrict());
        family.setAreaType(msg.getAreaType());
        family.setBlock(msg.getBlock());
        family.setGp(msg.getGp());
        family.setUlb(msg.getUlb());
        family.setWard(msg.getWard());
        family.setTotalFamilyMembers(msg.getTotalFamilyMembers());
        family.setNoOfLiterateAdults(msg.getNoOfLiterateAdults());
        family.setNoOfIlliterateAdults(msg.getNoOfIlliterateAdults());
        family.setHasDigitalRationCard(msg.isHasDigitalRationCard());
        family.setRationCardHouseholdId(msg.getRationCardHouseholdId());
        family.setLiftingMonthlyRation(msg.isLiftingMonthlyRation());
        family.setHasElectricityConnection(msg.isHasElectricityConnection());
        family.setElectricityConsumerId(msg.getElectricityConsumerId());
        family.setPowerUnitsConsumed(msg.getPowerUnitsConsumed());
        family.setTotalAnnualFamilyIncome(msg.getTotalAnnualFamilyIncome());
        family.setAgreed(msg.isAgreed());
        family.setStatus("SUBMITTED");
        family.setCreatedAt(LocalDateTime.now());
        family.setUpdatedAt(LocalDateTime.now());

        if (msg.getMembers() != null) {
            for (var m : msg.getMembers()) {
                FamilyMember newMember = new FamilyMember();
                newMember.setFamily(family);
                
                // Copy Partition Keys from Family
                newMember.setDistrict(family.getDistrict());
                newMember.setBlock(family.getBlock());
                newMember.setGp(family.getGp());
                
                newMember.setChild(m.isChild());
                newMember.setRelationWithHeadOfFamily(m.getRelationWithHeadOfFamily());
                newMember.setHoF(m.isHoF());
                newMember.setMemberName(m.getMemberName());
                newMember.setAadhaarNo(m.getAadhaarNo() != null ? m.getAadhaarNo() : "");
                newMember.setMobileNo(m.getMobileNo());
                newMember.setDateOfBirth(m.getDateOfBirth());
                newMember.setGender(m.getGender());
                newMember.setDigitalRationCardNo(m.getDigitalRationCardNo());
                newMember.setDigitalRationCardType(m.getDigitalRationCardType());
                newMember.setHasDigitalRationCard(m.getHasDigitalRationCard());
                newMember.setLiftingMonthlyRation(m.getLiftingMonthlyRation());
                newMember.setSocialCategory(m.getSocialCategory());
                newMember.setBankAccountNo(m.getBankAccountNo());
                newMember.setIfscCode(m.getIfscCode());
                newMember.setBankName(m.getBankName());
                newMember.setEpicNo(m.getEpicNo());
                newMember.setAssemblyConstituencyNo(m.getAssemblyConstituencyNo());
                newMember.setPartNo(m.getPartNo());
                newMember.setCaaApplicationStatus(m.getCaaApplicationStatus());
                newMember.setCaaApplicationNo(m.getCaaApplicationNo());
                newMember.setCaaCertificateNo(m.getCaaCertificateNo());
                newMember.setSir2026TribunalStatus(m.getSir2026TribunalStatus());
                newMember.setSir2026CaseDetails(m.getSir2026CaseDetails());
                newMember.setHasThreePuccaRooms(m.getHasThreePuccaRooms());
                newMember.setOwnsLand(m.getOwnsLand());
                newMember.setLandholdingSizeDecimals(m.getLandholdingSizeDecimals());
                newMember.setHasFourWheeler(m.isHasFourWheeler());
                newMember.setVehicleCount(m.getVehicleCount());
                newMember.setVehicleRegistrationNo(m.getVehicleRegistrationNo());
                newMember.setVehicleModel(m.getVehicleModel());
                newMember.setHasHealthInsurance(m.isHasHealthInsurance());
                newMember.setHealthInsuranceType(m.getHealthInsuranceType());
                newMember.setHealthInsuranceSumAssured(m.getHealthInsuranceSumAssured());
                newMember.setHealthInsuranceAnnualPremium(m.getHealthInsuranceAnnualPremium());
                newMember.setLiteracyStatus(m.getLiteracyStatus());
                newMember.setHighestEducationalQualifications(m.getHighestEducationalQualifications());
                newMember.setSchoolName(m.getSchoolName());
                newMember.setSchoolGrade(m.getSchoolGrade());
                newMember.setSchoolType(m.getSchoolType());
                newMember.setSchoolTypeOther(m.getSchoolTypeOther());
                newMember.setGovtEmploymentType(m.getGovtEmploymentType());
                newMember.setGrossAnnualIncome(m.getGrossAnnualIncome());
                newMember.setPaysIncomeOrProfessionalTax(m.isPaysIncomeOrProfessionalTax());
                newMember.setHasPanCard(m.getHasPanCard());
                newMember.setPanName(m.getPanName());
                newMember.setPanNo(m.getPanNo());
                newMember.setProfessionalTaxProfession(m.getProfessionalTaxProfession());
                newMember.setHoldsConstitutionalPost(m.isHoldsConstitutionalPost());
                newMember.setConstitutionalPostMemberNo(m.getConstitutionalPostMemberNo());
                newMember.setRegisteredGst(m.isRegisteredGst());
                newMember.setGstin(m.getGstin());
                newMember.setGovtPensioner(m.isGovtPensioner());
                newMember.setGovtPensionerMemberNo(m.getGovtPensionerMemberNo());
                newMember.setApplyingForAnnapurnaBhandar(m.getApplyingForAnnapurnaBhandar());
                newMember.setDisabilityIdNo(m.getDisabilityIdNo());
                newMember.setDisabilityIssuedDate(m.getDisabilityIssuedDate());
                newMember.setDisabilityPercentage(m.getDisabilityPercentage());
                newMember.setDisabilityIssuingAuthority(m.getDisabilityIssuingAuthority());
                newMember.setOtherIdNo(m.getOtherIdNo());
                newMember.setOtherIdIssuedDate(m.getOtherIdIssuedDate());
                newMember.setOtherIdIssuingAuthority(m.getOtherIdIssuingAuthority());
                newMember.setVaccinationStatus(m.getVaccinationStatus());
                newMember.setVaccinationCardId(m.getVaccinationCardId());
                newMember.setVaccinationSkipReasonOrDate(m.getVaccinationSkipReasonOrDate());

                if (m.getNatureOfEmployment() != null) {
                    for (String nature : m.getNatureOfEmployment()) {
                        MemberEmploymentNature emp = new MemberEmploymentNature();
                        emp.setEmploymentType(nature);
                        emp.setMember(newMember);
                        emp.setDistrict(family.getDistrict());
                        newMember.getEmploymentNatures().add(emp);
                    }
                }

                if (m.getGovtSchemeBenefits() != null) {
                    for (var scheme : m.getGovtSchemeBenefits()) {
                        MemberGovtScheme s = new MemberGovtScheme();
                        s.setSchemeName(scheme.getSchemeName());
                        s.setOptOut(scheme.isOptOut());
                        s.setMember(newMember);
                        s.setDistrict(family.getDistrict());
                        newMember.getGovtSchemes().add(s);
                    }
                }

                if (m.getOtherSpecificIds() != null) {
                    for (var id : m.getOtherSpecificIds()) {
                        MemberOtherId oid = new MemberOtherId();
                        oid.setIdType(id.getIdType());
                        oid.setIssueDate(id.getIssueDate());
                        oid.setMember(newMember);
                        oid.setDistrict(family.getDistrict());
                        newMember.getOtherIds().add(oid);
                    }
                }

                family.getMembers().add(newMember);
            }
        }

        // Establish HoF → Member relationship
        FamilyMember hof = family.getMembers().stream().filter(FamilyMember::isHoF).findFirst().orElse(null);
        if (hof != null) {
            for (FamilyMember member : family.getMembers()) {
                if (!member.isHoF()) {
                    member.setHeadOfFamily(hof);
                }
            }
        }

        Family savedFamily = familyRepository.save(family);
        logger.info("[DirectDb] Form persisted. ApplicationId={}, FamilyId={}", family.getApplicationId(), savedFamily.getId());
    }
}
