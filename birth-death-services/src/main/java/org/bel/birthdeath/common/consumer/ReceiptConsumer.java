package org.bel.birthdeath.common.consumer;

import static org.bel.birthdeath.utils.BirthDeathConstants.BIRTH_CERT;

import java.util.HashMap;
import java.util.List;

import org.bel.birthdeath.birth.certmodel.BirthCertRequest;
import org.bel.birthdeath.birth.certmodel.BirthCertificate;
import org.bel.birthdeath.birth.certmodel.BirthCertificate.StatusEnum;
import org.bel.birthdeath.birth.model.EgBirthDtl;
import org.bel.birthdeath.birth.model.SearchCriteria;
import org.bel.birthdeath.birth.repository.BirthRepository;
import org.bel.birthdeath.common.calculation.collections.models.PaymentDetail;
import org.bel.birthdeath.common.calculation.collections.models.PaymentRequest;
import org.bel.birthdeath.common.contract.BirthPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.common.producer.Producer;
import org.bel.birthdeath.config.BirthDeathConfiguration;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;



@Slf4j
@Component
public class ReceiptConsumer {

	@Autowired
	private ObjectMapper mapper;
	
	@Autowired
	private CommonUtils commUtils;
	
	@Autowired
	private BirthDeathConfiguration config ;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private BirthRepository repository;
	
    @KafkaListener(topics = {"${kafka.topics.receipt.create}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        try {
        process(record);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": ", e.getMessage());
        }
    }
    
    public void process(HashMap<String, Object> record) {

		try {
			log.info("Process for object"+ record);
			PaymentRequest paymentRequest = mapper.convertValue(record, PaymentRequest.class);
			RequestInfo requestInfo = paymentRequest.getRequestInfo();
			if( paymentRequest.getPayment().getTotalAmountPaid().compareTo(paymentRequest.getPayment().getTotalDue())!=0) 
				return;
			List<PaymentDetail> paymentDetails = paymentRequest.getPayment().getPaymentDetails();
			for (PaymentDetail paymentDetail : paymentDetails) {
				if(paymentDetail.getBusinessService().equalsIgnoreCase(BIRTH_CERT)) {
					String uuid = requestInfo.getUserInfo().getUuid();
				    AuditDetails auditDetails = commUtils.getAuditDetails(uuid, false);
					SearchCriteria criteria=new SearchCriteria();
					BirthCertificate birthCertificate = repository.getBirthCertReqByConsumerCode(paymentDetail.getBill().getConsumerCode());
					criteria.setId(birthCertificate.getBirthDtlId());
					List<EgBirthDtl> birtDtls = repository.getBirthDtlsAll(criteria);
					if(birtDtls.size()>1) 
						throw new CustomException("Invalid_Input","Error in processing data");
					BirthPdfApplicationRequest applicationRequest = BirthPdfApplicationRequest.builder().requestInfo(requestInfo).BirthCertificate(birtDtls).build();
					EgovPdfResp pdfResp = repository.saveBirthCertPdf(applicationRequest);
					birthCertificate.setFilestoreid(pdfResp.getFilestoreIds().get(0));
					birthCertificate.setAuditDetails(auditDetails);
					birthCertificate.setApplicationStatus(StatusEnum.PAID);
					BirthCertRequest request = BirthCertRequest.builder().requestInfo(requestInfo).birthCertificate(birthCertificate).build();
					producer.push(config.getUpdateBirthTopic(), request);
					repository.updateCounter(birthCertificate.getBirthDtlId());
				}
			}
		} catch (Exception e) {
			log.error("Exception while processing payment update: ",e);
		}

	}
}