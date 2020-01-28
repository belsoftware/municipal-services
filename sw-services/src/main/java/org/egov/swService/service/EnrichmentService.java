package org.egov.swService.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Function;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.swService.config.SWConfiguration;
import org.egov.swService.model.AuditDetails;
import org.egov.swService.model.Connection.ApplicationStatusEnum;
import org.egov.swService.model.Connection.StatusEnum;
import org.egov.swService.model.Property;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.Idgen.IdResponse;
import org.egov.swService.repository.IdGenRepository;
import org.egov.swService.util.SWConstants;
import org.egov.swService.util.SewerageServicesUtil;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.validator.ValidateProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	
	@Autowired
	SewerageServicesUtil sewerageServicesUtil;
	
	@Autowired
	IdGenRepository idGenRepository;
	
	@Autowired
	SWConfiguration config;
	
	@Autowired
	ValidateProperty validateProperty;
	

	/**
	 * 
	 * @param sewerageConnectionList
	 *            List of sewerage connection for enriching the sewerage connection
	 *            with property.
	 * @param requestInfo 
	 *            is RequestInfo from request
	 */

	public void enrichSewerageSearch(List<SewerageConnection> sewerageConnectionList, RequestInfo requestInfo,
			SearchCriteria sewerageConnectionSearchCriteria) {

		if (!sewerageConnectionList.isEmpty()) {
			String propertyIdsString = sewerageConnectionList.stream()
					.map(sewerageConnection -> sewerageConnection.getProperty().getPropertyId())
					.collect(Collectors.toList()).stream().collect(Collectors.joining(","));
			List<Property> propertyList = sewerageServicesUtil
					.searchPropertyOnId(sewerageConnectionSearchCriteria.getTenantId(), propertyIdsString, requestInfo);
			HashMap<String, Property> propertyMap = propertyList.stream()
					.collect(Collectors.toMap(Property::getPropertyId, Function.identity(),
							(oldValue, newValue) -> newValue, LinkedHashMap::new));
			sewerageConnectionList.forEach(sewerageConnection -> {

				String propertyId = sewerageConnection.getProperty().getPropertyId();
				if (propertyMap.containsKey(propertyId)) {
					sewerageConnection.setProperty(propertyMap.get(propertyId));
				} else {

					throw new CustomException("INVALID SEARCH", "NO PROPERTY FOUND FOR "
							+ sewerageConnection.getConnectionNo() + " SEWERAGE CONNECTION No");
				}
			});

		}
	}
	

	
	/**
	 * 
	 * @param sewerageConnectionRequest
	 * @param propertyList
	 */

	public void enrichSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		validateProperty.enrichPropertyForSewerageConnection(sewerageConnectionRequest);
		AuditDetails auditDetails = sewerageServicesUtil
				.getAuditDetails(sewerageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), true);
		sewerageConnectionRequest.getSewerageConnection().setId(UUID.randomUUID().toString());
		sewerageConnectionRequest.getSewerageConnection().setStatus(StatusEnum.ACTIVE);
		setSewarageApplicationIdgenIds(sewerageConnectionRequest);
		setStatusForCreate(sewerageConnectionRequest);
	}
	
	
	/**
	 * Sets status for create request
	 * 
	 * @param ConnectionRequest
	 *            The create request
	 */
	private void setStatusForCreate(SewerageConnectionRequest sewerageConnectionRequest) {
		if (sewerageConnectionRequest.getSewerageConnection().getAction()
				.equalsIgnoreCase(SWConstants.ACTION_INITIATE)) {
			sewerageConnectionRequest.getSewerageConnection().setApplicationStatus(ApplicationStatusEnum.INITIATED);
		}
		if (sewerageConnectionRequest.getSewerageConnection().getAction().equalsIgnoreCase(SWConstants.ACTION_APPLY)) {
			sewerageConnectionRequest.getSewerageConnection().setApplicationStatus(ApplicationStatusEnum.APPLIED);
		}
	}
	


	/**
	 * Sets the SewarageConnectionId for given SewerageConnectionRequest
	 *
	 * @param request SewerageConnectionRequest which is to be created
	 */
	private void setSewarageApplicationIdgenIds(SewerageConnectionRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getRequestInfo().getUserInfo().getTenantId();
		SewerageConnection sewerageConnection = request.getSewerageConnection();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getSewerageApplicationIdGenName(),
				config.getSewerageApplicationIdGenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != 1) {
			errorMap.put("IDGEN ERROR ",
					"The Id of SewerageConnection returned by idgen is not equal to number of SewerageConnection");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		sewerageConnection.setApplicationNo(itr.next());
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}
	
	/**
	 * Enrich update sewarage connection
	 * 
	 * @param sewarageConnectionRequest
	 */
	public void enrichUpdateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest) {
		validateProperty.enrichPropertyForSewerageConnection(sewerageConnectionRequest);
		AuditDetails auditDetails = sewerageServicesUtil
				.getAuditDetails(sewerageConnectionRequest.getRequestInfo().getUserInfo().getUuid(), false);
	}
	
	  /**
     * Sets status for create request
     * @param tradeLicenseRequest The create request
     */
	
//    private void setStatusForCreate(SewerageConnectionRequest sewerageConnectionRequest){
//    	sewerageConnectionRequest.getSewerageConnection(){
//            if(.equalsIgnoreCase(ACTION_INITIATE))
//                license.setStatus(STATUS_INITIATED);
//            if(license.getAction().equalsIgnoreCase(ACTION_APPLY))
//                license.setStatus(STATUS_APPLIED);
//        });
//    }
}