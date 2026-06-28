package com.irs1099.service.iris;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.irs1099.entity.BusinessProfile;
import com.irs1099.entity.FormRecord;
import com.irs1099.entity.Submission;
import com.irs1099.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.UUID;

/**
 * Generates IRS IRIS A2A XML payloads per Publication 5718 specification.
 *
 * XML Structure (3 levels):
 * 1. Transmission - manifest with transmitter info, contains submissions
 * 2. Submission - form type, header (1096 data), contains records
 * 3. Record - individual form data (1099-NEC, etc.)
 *
 * Requirements:
 * - UTF-8 encoding, no BOM
 * - Special characters escaped (&amp; &apos; &lt; &quot;)
 * - Double dash (--) prohibited in data
 * - Max 100MB payload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IrsXmlGenerator {

    private final EncryptionUtil encryptionUtil;
    private final ObjectMapper objectMapper;

    private static final String SCHEMA_VERSION = "2.0";
    private static final String IRIS_APP_ID = "IRIS";
    private static final String REQUEST_TYPE = "A";

    /**
     * Generate a complete IRIS A2A XML transmission.
     */
    public XmlGenerationResult generateTransmission(
            Submission submission,
            List<FormRecord> records,
            BusinessProfile transmitterProfile) {

        String tcc = transmitterProfile.getTcc();
        if (tcc == null || tcc.isEmpty()) {
            return XmlGenerationResult.error("Transmitter Control Code (TCC) is required");
        }

        String transmitterEin = encryptionUtil.decrypt(transmitterProfile.getEinEncrypted());
        String utid = generateUtid(tcc);
        String submissionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        try {
            StringWriter sw = new StringWriter();
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            XMLStreamWriter xml = factory.createXMLStreamWriter(sw);

            xml.writeStartDocument("UTF-8", "1.0");
            xml.writeStartElement("IRTransmission");
            xml.writeDefaultNamespace("urn:us:gov:treasury:irs:ir");

            // === Transmission Manifest ===
            writeTransmissionManifest(xml, utid, tcc, transmitterEin, transmitterProfile, submission);

            // === Submission Group ===
            writeSubmissionGroup(xml, submission, records, submissionId, transmitterEin, transmitterProfile);

            xml.writeEndElement(); // IRTransmission
            xml.writeEndDocument();
            xml.flush();

            String xmlContent = sw.toString();

            // Validate size (100MB max)
            if (xmlContent.getBytes("UTF-8").length > 100 * 1024 * 1024) {
                return XmlGenerationResult.error("Transmission exceeds 100MB size limit");
            }

            return XmlGenerationResult.success(xmlContent, utid, submissionId);

        } catch (Exception e) {
            log.error("Failed to generate IRIS XML", e);
            return XmlGenerationResult.error("XML generation failed: " + e.getMessage());
        }
    }

    private void writeTransmissionManifest(XMLStreamWriter xml, String utid, String tcc,
                                            String transmitterEin, BusinessProfile profile,
                                            Submission submission) throws XMLStreamException {

        xml.writeStartElement("TransmissionManifest");

        writeElement(xml, "UniqueTransmissionId", utid);
        writeElement(xml, "VersionNum", SCHEMA_VERSION);
        writeElement(xml, "TransmitterTCC", tcc);
        writeElement(xml, "TransmitterEIN", transmitterEin);
        writeElement(xml, "TransmitterBusinessNm", sanitize(profile.getBusinessName()));

        // Transmitter address
        xml.writeStartElement("TransmitterAddress");
        writeElement(xml, "AddressLine1Txt", sanitize(profile.getAddressLine1()));
        if (profile.getAddressLine2() != null && !profile.getAddressLine2().isEmpty()) {
            writeElement(xml, "AddressLine2Txt", sanitize(profile.getAddressLine2()));
        }
        writeElement(xml, "CityNm", sanitize(profile.getCity()));
        writeElement(xml, "StateAbbreviationCd", profile.getState());
        writeElement(xml, "ZIPCd", profile.getZipCode());
        xml.writeEndElement(); // TransmitterAddress

        // Transmission type: O=Original, C=Correction, R=Replacement
        String typeCd = "O";
        switch (submission.getTransmissionType()) {
            case CORRECTION: typeCd = "C"; break;
            case REPLACEMENT: typeCd = "R"; break;
            default: typeCd = "O";
        }
        writeElement(xml, "TransmissionTypeCd", typeCd);

        xml.writeEndElement(); // TransmissionManifest
    }

    private void writeSubmissionGroup(XMLStreamWriter xml, Submission submission,
                                       List<FormRecord> records, String submissionId,
                                       String transmitterEin, BusinessProfile profile)
            throws XMLStreamException {

        // 1099 forms use IRSubmission1Grp
        xml.writeStartElement("IRSubmission1Grp");

        // Submission header (like Form 1096 data)
        writeSubmissionHeader(xml, submission, records, submissionId, transmitterEin, profile);

        // Individual form records
        for (FormRecord record : records) {
            writeFormRecord(xml, record, submission.getFormType());
        }

        xml.writeEndElement(); // IRSubmission1Grp
    }

    private void writeSubmissionHeader(XMLStreamWriter xml, Submission submission,
                                        List<FormRecord> records, String submissionId,
                                        String transmitterEin, BusinessProfile profile)
            throws XMLStreamException {

        xml.writeStartElement("IRSubmission1Header");

        writeElement(xml, "SubmissionId", submissionId);
        writeElement(xml, "TaxYr", String.valueOf(submission.getTaxYear()));

        // Issuer (the business filing the forms)
        writeElement(xml, "IssuerEIN", transmitterEin);
        writeElement(xml, "IssuerNm", sanitize(profile.getBusinessName()));

        xml.writeStartElement("IssuerAddress");
        writeElement(xml, "AddressLine1Txt", sanitize(profile.getAddressLine1()));
        if (profile.getAddressLine2() != null && !profile.getAddressLine2().isEmpty()) {
            writeElement(xml, "AddressLine2Txt", sanitize(profile.getAddressLine2()));
        }
        writeElement(xml, "CityNm", sanitize(profile.getCity()));
        writeElement(xml, "StateAbbreviationCd", profile.getState());
        writeElement(xml, "ZIPCd", profile.getZipCode());
        xml.writeEndElement(); // IssuerAddress

        if (profile.getBusinessPhone() != null && !profile.getBusinessPhone().isEmpty()) {
            writeElement(xml, "ContactPhoneNum", profile.getBusinessPhone().replaceAll("[^0-9]", ""));
        }

        // Form type and count
        String formCode = getFormCode(submission.getFormType());
        writeElement(xml, "FormTypeCd", formCode);
        writeElement(xml, "NumberOfPayees", String.valueOf(records.size()));

        if (submission.isCfsfFiling()) {
            writeElement(xml, "CombinedFederalStateCd", "1");
        }

        xml.writeEndElement(); // IRSubmission1Header
    }

    private void writeFormRecord(XMLStreamWriter xml, FormRecord record, String formType)
            throws XMLStreamException {

        if ("1099-NEC".equals(formType)) {
            writeForm1099Nec(xml, record);
        }
        // Future: add other form types here
    }

    private void writeForm1099Nec(XMLStreamWriter xml, FormRecord record) throws XMLStreamException {
        xml.writeStartElement("Form1099NECDetail");

        writeElement(xml, "RecordId", record.getRecordId());

        // Payer (issuer) info
        String issuerEin = encryptionUtil.decrypt(record.getIssuerEinEncrypted());
        writeElement(xml, "PayerEIN", issuerEin);
        writeElement(xml, "PayerNm", sanitize(record.getIssuerName()));

        xml.writeStartElement("PayerAddress");
        writeElement(xml, "AddressLine1Txt", sanitize(record.getIssuerAddressLine1()));
        if (record.getIssuerAddressLine2() != null && !record.getIssuerAddressLine2().isEmpty()) {
            writeElement(xml, "AddressLine2Txt", sanitize(record.getIssuerAddressLine2()));
        }
        writeElement(xml, "CityNm", sanitize(record.getIssuerCity()));
        writeElement(xml, "StateAbbreviationCd", record.getIssuerState());
        writeElement(xml, "ZIPCd", record.getIssuerZipCode());
        xml.writeEndElement(); // PayerAddress

        if (record.getIssuerPhone() != null && !record.getIssuerPhone().isEmpty()) {
            writeElement(xml, "PayerPhoneNum", record.getIssuerPhone().replaceAll("[^0-9]", ""));
        }

        // Recipient info
        String recipientTin = encryptionUtil.decrypt(record.getRecipientTinEncrypted());
        writeElement(xml, "RecipientTIN", recipientTin);
        writeElement(xml, "TINTypeCd", record.getRecipientTinType().name());

        if (record.getRecipientFirstName() != null && !record.getRecipientFirstName().isEmpty()) {
            xml.writeStartElement("RecipientPersonNm");
            writeElement(xml, "PersonFirstNm", sanitizeName(record.getRecipientFirstName()));
            if (record.getRecipientMiddleName() != null && !record.getRecipientMiddleName().isEmpty()) {
                writeElement(xml, "PersonMiddleNm", sanitizeName(record.getRecipientMiddleName()));
            }
            writeElement(xml, "PersonLastNm", sanitizeName(record.getRecipientLastName()));
            xml.writeEndElement(); // RecipientPersonNm
        }

        if (record.getRecipientBusinessName() != null && !record.getRecipientBusinessName().isEmpty()) {
            writeElement(xml, "RecipientBusinessNm", sanitize(record.getRecipientBusinessName()));
        }

        xml.writeStartElement("RecipientAddress");
        writeElement(xml, "AddressLine1Txt", sanitize(record.getRecipientAddressLine1()));
        if (record.getRecipientAddressLine2() != null && !record.getRecipientAddressLine2().isEmpty()) {
            writeElement(xml, "AddressLine2Txt", sanitize(record.getRecipientAddressLine2()));
        }
        writeElement(xml, "CityNm", sanitize(record.getRecipientCity()));
        writeElement(xml, "StateAbbreviationCd", record.getRecipientState());
        writeElement(xml, "ZIPCd", record.getRecipientZipCode());
        xml.writeEndElement(); // RecipientAddress

        if (record.getRecipientAccountNumber() != null && !record.getRecipientAccountNumber().isEmpty()) {
            writeElement(xml, "AccountNum", record.getRecipientAccountNumber());
        }

        // 1099-NEC specific financial data
        try {
            JsonNode formData = objectMapper.readTree(record.getFormDataJson());

            String box1 = formData.path("box1NonemployeeCompensation").asText("0");
            if (!box1.isEmpty() && !"0".equals(box1)) {
                writeElement(xml, "NonemployeeCompensationAmt", formatAmount(box1));
            }

            boolean box2 = formData.path("box2PayerMadeDirectSales").asBoolean(false);
            if (box2) {
                writeElement(xml, "PayerDirectSalesInd", "1");
            }

            String box4 = formData.path("box4FederalTaxWithheld").asText("0");
            if (!box4.isEmpty() && !"0".equals(box4)) {
                writeElement(xml, "FederalIncomeTaxWithheldAmt", formatAmount(box4));
            }

            // State tax info
            String stateCode = formData.path("stateCode").asText("");
            String box5 = formData.path("box5StateTaxWithheld").asText("0");
            if (!stateCode.isEmpty() && !box5.isEmpty() && !"0".equals(box5)) {
                xml.writeStartElement("StateTaxGrp");
                writeElement(xml, "StateAbbreviationCd", stateCode);
                writeElement(xml, "StateTaxWithheldAmt", formatAmount(box5));

                String box6 = formData.path("box6StatePayersNo").asText("");
                if (!box6.isEmpty()) {
                    writeElement(xml, "StatePayerIdNum", box6);
                }

                String box7 = formData.path("box7StateIncome").asText("0");
                if (!box7.isEmpty() && !"0".equals(box7)) {
                    writeElement(xml, "StateIncomeAmt", formatAmount(box7));
                }
                xml.writeEndElement(); // StateTaxGrp
            }

        } catch (Exception e) {
            log.error("Failed to parse form data JSON for record {}", record.getId(), e);
        }

        xml.writeEndElement(); // Form1099NECDetail
    }

    // === Utility Methods ===

    /**
     * Generate UTID per spec: UUID:IRIS:TCC::A
     */
    public String generateUtid(String tcc) {
        return UUID.randomUUID().toString() + ":" + IRIS_APP_ID + ":" + tcc + "::" + REQUEST_TYPE;
    }

    /**
     * Sanitize text for XML: escape special characters, strip prohibited ones.
     * Per Pub 5718 Section 3.2.3:
     * - Double dash (--) is prohibited
     * - & ' < " must be escaped (handled by XMLStreamWriter)
     */
    private String sanitize(String text) {
        if (text == null) return "";
        return text.replace("--", "-");
    }

    /**
     * Sanitize person names: only alphanumeric and hyphen allowed.
     * Per Pub 5718: PersonFirstNm, PersonMiddleNm, PersonLastNm
     * cannot contain special characters except "-".
     */
    private String sanitizeName(String name) {
        if (name == null) return "";
        return name.replaceAll("[^a-zA-Z0-9\\s-]", "").trim();
    }

    /**
     * Format monetary amount: ensure 2 decimal places, no currency symbol.
     */
    private String formatAmount(String amount) {
        try {
            double value = Double.parseDouble(amount.replaceAll("[^0-9.]", ""));
            return String.format("%.2f", value);
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }

    /**
     * Map form type string to IRS form code.
     */
    private String getFormCode(String formType) {
        switch (formType) {
            case "1099-NEC": return "NEC";
            case "1099-MISC": return "MISC";
            case "1099-INT": return "INT";
            case "1099-DIV": return "DIV";
            case "1099-K": return "K";
            case "1099-R": return "R";
            default: return formType.replace("1099-", "");
        }
    }

    private void writeElement(XMLStreamWriter xml, String name, String value) throws XMLStreamException {
        if (value != null && !value.isEmpty()) {
            xml.writeStartElement(name);
            xml.writeCharacters(value);
            xml.writeEndElement();
        }
    }

    /**
     * Result of XML generation.
     */
    public static class XmlGenerationResult {
        private final boolean success;
        private final String xml;
        private final String utid;
        private final String submissionId;
        private final String error;

        private XmlGenerationResult(boolean success, String xml, String utid, String submissionId, String error) {
            this.success = success;
            this.xml = xml;
            this.utid = utid;
            this.submissionId = submissionId;
            this.error = error;
        }

        public static XmlGenerationResult success(String xml, String utid, String submissionId) {
            return new XmlGenerationResult(true, xml, utid, submissionId, null);
        }

        public static XmlGenerationResult error(String error) {
            return new XmlGenerationResult(false, null, null, null, error);
        }

        public boolean isSuccess() { return success; }
        public String getXml() { return xml; }
        public String getUtid() { return utid; }
        public String getSubmissionId() { return submissionId; }
        public String getError() { return error; }
    }
}
