package org.egov.pt.util;

import static org.egov.pt.util.PTConstants.CREATE_PROCESS_CONSTANT;
import static org.egov.pt.util.PTConstants.MUTATION_PROCESS_CONSTANT;
import static org.egov.pt.util.PTConstants.UPDATE_PROCESS_CONSTANT;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.User;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.user.UserDetailResponse;
import org.egov.pt.models.workflow.ProcessInstance;
import org.egov.pt.models.workflow.ProcessInstanceRequest;
import org.egov.pt.web.contracts.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PropertyUtil extends CommonUtils {

    @Autowired
    private PropertyConfiguration config;


    /**
	 * Populates the owner fields inside of property objects from the response by user api
	 * 
	 * Ignoring if now user is not found in user response, no error will be thrown
	 * 
	 * @param userDetailResponse response from user api which contains list of user
	 *                           which are used to populate owners in properties
	 * @param properties         List of property whose owner's are to be populated
	 *                           from userDetailResponse
	 */
	public void enrichOwner(UserDetailResponse userDetailResponse, List<Property> properties) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));

		properties.forEach(property -> {

			property.getOwners().forEach(owner -> {

				if (userIdToOwnerMap.get(owner.getUuid()) == null)
					log.info("OWNER SEARCH ERROR", "The owner with UUID : \"" + owner.getUuid() +
							"\" for the property with Id \"" + property.getPropertyId() + "\" is not present in user search response");
				else
					owner.addUserDetail(userIdToOwnerMap.get(owner.getUuid()));
			});
		});
	}
	

	public ProcessInstanceRequest getProcessInstanceForPayment(PropertyRequest propertyRequest) {

			Property property = propertyRequest.getProperty();
			
			ProcessInstance process = ProcessInstance.builder()
				.businessService(config.getPropertyRegistryWf())
				.businessId(property.getAcknowldgementNumber())
				.comment("Payment for property processed")
				.assignes(getUserForWorkflow(property))
				.moduleName("PT")
				.action("PAY")
				.build();
			
			return ProcessInstanceRequest.builder()
					.requestInfo(propertyRequest.getRequestInfo())
					.processInstances(Arrays.asList(process))
					.build();
	}
	
	public ProcessInstanceRequest getWfForPropertyRegistry(PropertyRequest request, String process) {
		
		Property property = request.getProperty();
		ProcessInstance wf = null != property.getWorkflow() ? property.getWorkflow() : new ProcessInstance();
		
		wf.setBusinessId(property.getAcknowldgementNumber());
		wf.setTenantId(property.getTenantId());
	
		
		switch (process) {
		
		case CREATE_PROCESS_CONSTANT :
			List<User> owners = getUserForWorkflow(property);
			wf.setAssignes(owners);
			wf.setBusinessService(config.getPropertyRegistryWf());
			wf.setModuleName(config.getPropertyModuleName());
			wf.setAction("OPEN");
			break;

		case MUTATION_PROCESS_CONSTANT:
			break;

		case UPDATE_PROCESS_CONSTANT:
			break;
			
		default:
			break;
		}
		
		return ProcessInstanceRequest.builder()
				.processInstances(Arrays.asList(request.getProperty().getWorkflow()))
				.requestInfo(request.getRequestInfo())
				.build();
	}

	/**
	 * 
	 * @param request
	 * @param propertyFromSearch
	 */
	public void mergeAdditionalDetails(PropertyRequest request, Property propertyFromSearch) {

		request.getProperty().setAdditionalDetails(jsonMerge(propertyFromSearch.getAdditionalDetails(),
				request.getProperty().getAdditionalDetails()));
	}

	public JsonNode saveOldUuidToRequest(PropertyRequest request, String uuid) {

		ObjectNode additionalDetail = (ObjectNode) request.getProperty().getAdditionalDetails();
		return additionalDetail.put(PTConstants.PREVIOUS_PROPERTY_PREVIOUD_UUID, uuid);
	}

	public void clearSensitiveDataForPersistance(Property property) {
		property.getOwners().forEach(owner -> owner.setMobileNumber(null));
	}
	
}