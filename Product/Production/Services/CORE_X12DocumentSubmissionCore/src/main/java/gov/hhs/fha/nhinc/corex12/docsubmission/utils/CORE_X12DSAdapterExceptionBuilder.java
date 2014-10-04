/*
 * Copyright (c) 2014, United States Government, as represented by the Secretary of Health and Human Services.
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

package gov.hhs.fha.nhinc.corex12.docsubmission.utils;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.Logger;
import org.caqh.soap.wsdl.corerule2_2_0.COREEnvelopeBatchSubmission;
import org.caqh.soap.wsdl.corerule2_2_0.COREEnvelopeBatchSubmissionResponse;
import org.caqh.soap.wsdl.corerule2_2_0.COREEnvelopeRealTimeRequest;
import org.caqh.soap.wsdl.corerule2_2_0.COREEnvelopeRealTimeResponse;

/**
 *
 * @author svalluripalli
 */
public class CORE_X12DSAdapterExceptionBuilder {
   private static final Logger LOG = Logger.getLogger(CORE_X12DSAdapterExceptionBuilder.class); 
   private static final String ADAPTER_ERROR_CODE = "Receiver";
   private static final String ADAPTER_ERROR_MESSAGE = "Failed to connect to backend adapter to process the message";
   private static final String ADAPTER_PAYLOAD_TYPE = "CoreEnvelopeError";
   
   /**
    * Constructor...
    */
   public CORE_X12DSAdapterExceptionBuilder()
   {
       
   }
   
   /**
     *
     * @param oCOREEnvelopeBatchSubmission
     * @param oBatchSubmissionResponse
     */
    public void buildCOREEnvelopeGenericBatchErrorResponse(COREEnvelopeBatchSubmission oCOREEnvelopeBatchSubmission, COREEnvelopeBatchSubmissionResponse oBatchSubmissionResponse) {
        Date currentDate = new Date();
        oBatchSubmissionResponse.setCORERuleVersion(oCOREEnvelopeBatchSubmission.getCORERuleVersion());
        oBatchSubmissionResponse.setCheckSum(oCOREEnvelopeBatchSubmission.getCheckSum());
        oBatchSubmissionResponse.setErrorCode(ADAPTER_ERROR_CODE);
        oBatchSubmissionResponse.setErrorMessage(ADAPTER_ERROR_MESSAGE);
        oBatchSubmissionResponse.setPayload(oCOREEnvelopeBatchSubmission.getPayload());
        oBatchSubmissionResponse.setPayloadLength(oCOREEnvelopeBatchSubmission.getPayloadLength());
        oBatchSubmissionResponse.setPayloadType(ADAPTER_PAYLOAD_TYPE);
        oBatchSubmissionResponse.setProcessingMode(oCOREEnvelopeBatchSubmission.getProcessingMode());
        oBatchSubmissionResponse.setReceiverID(oCOREEnvelopeBatchSubmission.getReceiverID());
        oBatchSubmissionResponse.setSenderID(oCOREEnvelopeBatchSubmission.getSenderID());
        oBatchSubmissionResponse.setTimeStamp(new Timestamp(currentDate.getTime()).toString());
    }

    /**
     *
     * @param oCOREEnvelopeRealTimeRequest
     * @param realTimeResponse
     */
    public void buildCOREEnvelopeRealTimeErrorResponse(COREEnvelopeRealTimeRequest oCOREEnvelopeRealTimeRequest, COREEnvelopeRealTimeResponse realTimeResponse) {
        Date currentDate = new Date();
        realTimeResponse.setCORERuleVersion(oCOREEnvelopeRealTimeRequest.getCORERuleVersion());
        realTimeResponse.setErrorCode(ADAPTER_ERROR_CODE);
        realTimeResponse.setErrorMessage(ADAPTER_ERROR_MESSAGE);
        realTimeResponse.setPayload(oCOREEnvelopeRealTimeRequest.getPayload());
        realTimeResponse.setPayloadType(ADAPTER_PAYLOAD_TYPE);
        realTimeResponse.setProcessingMode(oCOREEnvelopeRealTimeRequest.getProcessingMode());
        realTimeResponse.setReceiverID(oCOREEnvelopeRealTimeRequest.getReceiverID());
        realTimeResponse.setSenderID(oCOREEnvelopeRealTimeRequest.getSenderID());
        realTimeResponse.setTimeStamp(new Timestamp(currentDate.getTime()).toString());
        realTimeResponse.setPayloadID(oCOREEnvelopeRealTimeRequest.getPayloadID());
    }
}