package org.bel.birthdeath.death.service;

import java.util.List;

import org.bel.birthdeath.common.contract.DeathPdfApplicationRequest;
import org.bel.birthdeath.common.contract.EgovPdfResp;
import org.bel.birthdeath.common.model.AuditDetails;
import org.bel.birthdeath.death.certmodel.DeathCertAppln;
import org.bel.birthdeath.death.certmodel.DeathCertRequest;
import org.bel.birthdeath.death.certmodel.DeathCertificate;
import org.bel.birthdeath.death.certmodel.DeathCertificate.StatusEnum;
import org.bel.birthdeath.death.model.EgDeathDtl;
import org.bel.birthdeath.death.model.SearchCriteria;
import org.bel.birthdeath.death.repository.DeathRepository;
import org.bel.birthdeath.death.validator.DeathValidator;
import org.bel.birthdeath.utils.CommonUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeathService {
	
	@Autowired
	DeathRepository repository;

	@Autowired
	DeathValidator validator;
	
	@Autowired
	EnrichmentServiceDeath enrichmentServiceDeath;
	
	@Autowired
	CalculationServiceDeath calculationServiceDeath;
	
	@Autowired
	CommonUtils commUtils;
	
	public List<EgDeathDtl> search(SearchCriteria criteria) {
		List<EgDeathDtl> deathDtls = null ;
		if(validator.validateFields(criteria))
			deathDtls = repository.getDeathDtls(criteria);
		return deathDtls;
	}

	public DeathCertificate download(SearchCriteria criteria, RequestInfo requestInfo) {
		try {
		DeathCertificate deathCertificate = new DeathCertificate();
		deathCertificate.setDeathDtlId(criteria.getId());
		deathCertificate.setTenantId(criteria.getTenantId());
		DeathCertRequest deathCertRequest = DeathCertRequest.builder().deathCertificate(deathCertificate).requestInfo(requestInfo).build();
		List<EgDeathDtl> deathDtls = repository.getDeathDtlsAll(criteria,requestInfo);
		if(deathDtls.size()>1) 
			throw new CustomException("Invalid_Input","Error in processing data");
		enrichmentServiceDeath.enrichCreateRequest(deathCertRequest);
		enrichmentServiceDeath.setIdgenIds(deathCertRequest);
		if(deathDtls.get(0).getCounter()>0){
			enrichmentServiceDeath.setDemandParams(deathCertRequest);
			enrichmentServiceDeath.setGLCode(deathCertRequest);
			calculationServiceDeath.addCalculation(deathCertRequest);
			deathCertificate.setApplicationStatus(StatusEnum.ACTIVE);
		}
		else{
			deathDtls.get(0).setDeathcertificateno(deathCertRequest.getDeathCertificate().getDeathCertificateNo());
			DeathPdfApplicationRequest applicationRequest = DeathPdfApplicationRequest.builder().requestInfo(requestInfo).deathCertificate(deathDtls).build();
			EgovPdfResp pdfResp = repository.saveDeathCertPdf(applicationRequest);
			deathCertificate.setEmbeddedUrl(applicationRequest.getDeathCertificate().get(0).getEmbeddedUrl());
			deathCertificate.setDateofissue(applicationRequest.getDeathCertificate().get(0).getDateofissue());
			deathCertificate.setFilestoreid(pdfResp.getFilestoreIds().get(0));
			repository.updateCounter(deathCertificate.getDeathDtlId());
			deathCertificate.setApplicationStatus(StatusEnum.FREE_DOWNLOAD);
			
		}
		deathCertificate.setCounter(deathDtls.get(0).getCounter());
		repository.save(deathCertRequest);
		return deathCertificate;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new CustomException("DOWNLOAD_ERROR","Error in Downloading Certificate");
		}
	}

	public DeathCertificate getDeathCertReqByConsumerCode(SearchCriteria criteria, RequestInfo requestInfo) {
		return repository.getDeathCertReqByConsumerCode(criteria.getConsumerCode(),requestInfo);
	}
	
	public List<DeathCertAppln> searchApplications(RequestInfo requestInfo) {
		List<DeathCertAppln> certApplns=null;
		certApplns = repository.searchApplications(requestInfo.getUserInfo().getUuid());
		return certApplns;
	}

	public void updateDownloadStatus(DeathCertRequest certRequest) {
		if(null!=certRequest.getRequestInfo() && null!=certRequest.getRequestInfo().getUserInfo() && null!=certRequest.getRequestInfo().getUserInfo().getUuid())
		{
			AuditDetails auditDetails = commUtils.getAuditDetails(certRequest.getRequestInfo().getUserInfo().getUuid(), false);
			DeathCertificate deathCert = certRequest.getDeathCertificate();
			deathCert.getAuditDetails().setLastModifiedBy(auditDetails.getLastModifiedBy());
			deathCert.getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
			//deathCert.setAuditDetails(auditDetails);
			deathCert.setApplicationStatus(StatusEnum.PAID_DOWNLOAD);
			repository.update(certRequest);
		}

	}
	
	public List<EgDeathDtl> viewCertificateData(SearchCriteria criteria) {
		return repository.viewCertificateData(criteria);
	}
	
	public List<EgDeathDtl> viewfullCertMasterData(SearchCriteria criteria,RequestInfo requestInfo) {
		return repository.viewfullCertMasterData(criteria,requestInfo);
	}
}