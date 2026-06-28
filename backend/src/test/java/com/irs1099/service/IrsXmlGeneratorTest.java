package com.irs1099.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.irs1099.entity.BusinessProfile;
import com.irs1099.entity.FormRecord;
import com.irs1099.entity.Submission;
import com.irs1099.service.iris.IrsXmlGenerator;
import com.irs1099.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class IrsXmlGeneratorTest {

    private IrsXmlGenerator xmlGenerator;
    private EncryptionUtil encryptionUtil;

    @BeforeEach
    void setUp() {
        encryptionUtil = new EncryptionUtil("test-encryption-key-32-chars-ok!");
        xmlGenerator = new IrsXmlGenerator(encryptionUtil, new ObjectMapper());
    }

    @Test
    void generateUtid_matchesFormat() {
        String utid = xmlGenerator.generateUtid("DTEST");
        // Format: UUID:IRIS:TCC::A
        assertTrue(utid.matches("^[a-f0-9-]+:IRIS:DTEST::A$"));
    }

    @Test
    void generateTransmission_withValidData_producesXml() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        BusinessProfile profile = createTestProfile();

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertTrue(result.isSuccess());
        assertNotNull(result.getXml());
        assertNotNull(result.getUtid());
        assertNotNull(result.getSubmissionId());

        String xml = result.getXml();
        assertTrue(xml.contains("IRTransmission"));
        assertTrue(xml.contains("TransmissionManifest"));
        assertTrue(xml.contains("IRSubmission1Grp"));
        assertTrue(xml.contains("Form1099NECDetail"));
        assertTrue(xml.contains("NonemployeeCompensationAmt"));
        assertTrue(xml.contains("5000.00"));
        assertTrue(xml.contains("DTEST"));
        assertTrue(xml.contains("Test Company"));
    }

    @Test
    void generateTransmission_withoutTcc_returnsError() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        BusinessProfile profile = createTestProfile();
        profile.setTcc(null);

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("TCC"));
    }

    @Test
    void generateTransmission_sanitizesDoubleDash() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        record.setIssuerAddressLine1("123 Main St--Suite 4");
        BusinessProfile profile = createTestProfile();

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertTrue(result.isSuccess());
        assertFalse(result.getXml().contains("--"));
        assertTrue(result.getXml().contains("123 Main St-Suite 4"));
    }

    @Test
    void generateTransmission_sanitizesNameSpecialChars() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        record.setRecipientLastName("O'Malley");
        BusinessProfile profile = createTestProfile();

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertTrue(result.isSuccess());
        // Apostrophe should be stripped from person names
        assertTrue(result.getXml().contains("OMalley"));
    }

    @Test
    void generateTransmission_includesStateTaxInfo() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        record.setFormDataJson("{\"box1NonemployeeCompensation\":\"5000\",\"box5StateTaxWithheld\":\"250\",\"stateCode\":\"CA\",\"box6StatePayersNo\":\"12345\",\"box7StateIncome\":\"5000\"}");
        BusinessProfile profile = createTestProfile();

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertTrue(result.isSuccess());
        assertTrue(result.getXml().contains("StateTaxGrp"));
        assertTrue(result.getXml().contains("<StateAbbreviationCd>CA</StateAbbreviationCd>"));
        assertTrue(result.getXml().contains("250.00"));
    }

    @Test
    void generateTransmission_xmlIsUtf8() {
        Submission submission = createTestSubmission();
        FormRecord record = createTestRecord();
        BusinessProfile profile = createTestProfile();

        IrsXmlGenerator.XmlGenerationResult result =
                xmlGenerator.generateTransmission(submission, Collections.singletonList(record), profile);

        assertTrue(result.isSuccess());
        assertTrue(result.getXml().contains("encoding=\"UTF-8\""));
    }

    // === Test Helpers ===

    private Submission createTestSubmission() {
        Submission s = new Submission();
        s.setId(1L);
        s.setFormType("1099-NEC");
        s.setTaxYear(2025);
        s.setTransmissionType(Submission.TransmissionType.ORIGINAL);
        s.setStatus(Submission.SubmissionStatus.DRAFT);
        return s;
    }

    private FormRecord createTestRecord() {
        FormRecord r = new FormRecord();
        r.setId(1L);
        r.setRecordId("REC001");
        r.setFormType("1099-NEC");
        r.setIssuerEinEncrypted(encryptionUtil.encrypt("12-3456789"));
        r.setIssuerName("Test Company");
        r.setIssuerAddressLine1("123 Main St");
        r.setIssuerCity("New York");
        r.setIssuerState("NY");
        r.setIssuerZipCode("10001");
        r.setRecipientTinEncrypted(encryptionUtil.encrypt("987-65-4321"));
        r.setRecipientTinType(FormRecord.TinType.SSN);
        r.setRecipientFirstName("John");
        r.setRecipientLastName("Doe");
        r.setRecipientAddressLine1("456 Oak Ave");
        r.setRecipientCity("Los Angeles");
        r.setRecipientState("CA");
        r.setRecipientZipCode("90001");
        r.setFormDataJson("{\"box1NonemployeeCompensation\":\"5000\",\"box2PayerMadeDirectSales\":false,\"box4FederalTaxWithheld\":\"0\"}");
        return r;
    }

    private BusinessProfile createTestProfile() {
        BusinessProfile p = new BusinessProfile();
        p.setId(1L);
        p.setBusinessName("Test Company LLC");
        p.setEinEncrypted(encryptionUtil.encrypt("12-3456789"));
        p.setTcc("DTEST");
        p.setAddressLine1("789 Business Blvd");
        p.setCity("Chicago");
        p.setState("IL");
        p.setZipCode("60601");
        return p;
    }
}
