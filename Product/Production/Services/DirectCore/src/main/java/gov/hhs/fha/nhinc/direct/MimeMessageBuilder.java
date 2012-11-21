/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.direct;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringUtils;
import org.nhindirect.gateway.smtp.SmtpAgentSettings;
import org.nhindirect.xd.common.DirectDocument2;
import org.nhindirect.xd.common.DirectDocuments;
import org.nhindirect.xd.common.XdmPackage;
import org.nhindirect.xd.transform.util.type.MimeType;

/**
 * Builder for {@link MimeMessage}.
 */
public class MimeMessageBuilder {

    private final Session session;
    private final Address fromAddress;
    private final Address[] recipients;
    private final SmtpAgentSettings settings;

    private String subject;
    private String text;
    private DirectDocuments documents;
    private String messageId;
    private Document attachment;
    private String attachmentName;

    /**
     * Construct the Mime Message builder with required fields.
     * 
     * @param session used to build the message.
     * @param fromAddress sender of the message.
     * @param recipient of the message.
     */
    public MimeMessageBuilder(Session session, Address fromAddress, Address[] recipients, SmtpAgentSettings settings) {
        super();
        this.session = session;
        this.fromAddress = fromAddress;
        this.recipients = recipients;
        this.settings = settings;
    }

    /**
     * @param str for subject.
     * @return builder
     */
    public MimeMessageBuilder subject(String str) {
        this.subject = str;
        return this;
    }

    /**
     * @param str for text
     * @return builder
     */
    public MimeMessageBuilder text(String str) {
        this.text = str;
        return this;
    }

    /**
     * @param documents for attachment
     * @return builder
     */
    public MimeMessageBuilder documents(DirectDocuments documents) {
        this.documents = documents;
        return this;
    }
    
    /**
     * @param messageId for message
     * @return builder
     */
    public MimeMessageBuilder messageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
    
    /**
     * @param doc for attachment
     * @return builder
     */
    public MimeMessageBuilder attachment(Document doc) {
        this.attachment = doc;
        return this;
    }

    /**
     * @param str for attachment name
     * @return builder
     */
    public MimeMessageBuilder attachmentName(String str) {
        this.attachmentName = str;
        return this;
    }

    /**
     * Build the Mime Message.
     * 
     * @return the Mime message.
     */
    public MimeMessage build() {

        final MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(fromAddress);
        } catch (Exception e) {
            throw new DirectException("Exception setting from address: " + fromAddress, e);
        }

        try {
            message.addRecipients(Message.RecipientType.TO, recipients);
        } catch (Exception e) {
            throw new DirectException("Exception setting recipient to address(es): " + recipients, e);
        }

        try {
            message.setSubject(subject);
        } catch (Exception e) {
            throw new DirectException("Exception setting subject: " + subject, e);
        }

        MimeBodyPart messagePart = new MimeBodyPart();
        try {
            messagePart.setText(text);
        } catch (Exception e) {
            throw new DirectException("Exception setting mime message part text: " + text, e);
        }

        MimeBodyPart attachmentPart = null;
		try {
			if (null != documents && !StringUtils.isBlank(messageId)) {
				attachmentPart = getMimeBodyPart();
				
				//attachmentPart.attachFile(getXDMFile(documents.toXdmPackage(messageId)));
				attachmentPart.attachFile(documents.toXdmPackage(messageId).toFile());
			} else if (null != attachment && !StringUtils.isBlank(attachmentName)) {
				attachmentPart = createAttachmentFromSOAPRequest(attachment,
						attachmentName);
			} else {
				throw new Exception("Could not create attachment. Need documents and messageId or attachment and attachmentName.");
			}
		} catch (Exception e) {
			throw new DirectException("Exception creating attachment: "
					+ attachmentName, e);
		}

        Multipart multipart = new MimeMultipart();
        try {
            multipart.addBodyPart(messagePart);
            multipart.addBodyPart(attachmentPart);
            message.setContent(multipart);
        } catch (Exception e) {
            throw new DirectException("Exception creating multi-part attachment.", e);
        }

        try {
            message.saveChanges();
        } catch (Exception e) {
            throw new DirectException("Exception saving changes.", e);
        }

        return message;
    }

    protected File getXDMFile(XdmPackage xdmPackage) {
        File xdmFile = null;
        final String XDM_SUB_FOLDER = "IHE_XDM/SUBSET01";
        final String XDM_METADATA_FILE = "METADATA.xml";

        try {
        	String rawMessagePath = settings.getRawMessageSettings().getSaveMessageFolder().getAbsolutePath();
            xdmFile = new File(rawMessagePath + File.separator + messageId + "-xdm.zip");

            FileOutputStream dest = new FileOutputStream(xdmFile);

            ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(dest));
            zipOutputStream.setMethod(ZipOutputStream.DEFLATED);

            for (DirectDocument2 document : documents.getDocuments()) {
                if (document.getData() != null) {
                    String fileName = document.getMetadata().getId() ;
                    fileName = fileName.replace("urn:uuid:", "");
                    fileName = fileName + getSuffix(document.getMetadata().getMimeType());
                  
                    document.getMetadata().setURI(fileName);
                    addEntry(zipOutputStream, document.getData(), XDM_SUB_FOLDER + fileName );
                }
            }

            addEntry(zipOutputStream, documents.getSubmitObjectsRequestAsString().getBytes(), XDM_SUB_FOLDER + XDM_METADATA_FILE);

            /*addEntry(zipOutputStream, getIndex().getBytes(), "INDEX.htm");

            addEntry(zipOutputStream, getReadme().getBytes(), "README.txt");

            if (SUFFIX.equals(".xml")) {
                addEntry(zipOutputStream, getXsl().getBytes(), XDM_SUB_FOLDER + "CCD.xsl");
            }*/

            zipOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return xdmFile;
	}
    
    private void addEntry(ZipOutputStream zipOutputStream, byte[] data, String fileName) throws IOException {
    	final int BUFFER = 2048;
    	
        InputStream inputStream = new ByteArrayInputStream(data);
        BufferedInputStream outputStream = new BufferedInputStream(inputStream);

        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOutputStream.putNextEntry(zipEntry);

        int count = 0;
        byte dataOut[] = new byte[BUFFER];
        while ((count = outputStream.read(dataOut, 0, BUFFER)) != -1) {
            zipOutputStream.write(dataOut, 0, count);
        }
    }
    
    private String getSuffix(String mimeType) {
        return "." + MimeType.lookup(mimeType).getSuffix();
    }

	protected MimeBodyPart getMimeBodyPart() {
		return new MimeBodyPart();
	}

	private MimeBodyPart createAttachmentFromSOAPRequest(Document data, String name) throws MessagingException,
            IOException {
        DataSource source = new ByteArrayDataSource(data.getValue().getInputStream(), "application/octet-stream");
        DataHandler dhnew = new DataHandler(source);
        MimeBodyPart bodypart = new MimeBodyPart();
        bodypart.setDataHandler(dhnew);
        bodypart.setHeader("Content-Type", "application/octet-stream");
        bodypart.setDisposition(Part.ATTACHMENT);
        bodypart.setFileName(name);
        return (MimeBodyPart) bodypart;
    }

}
