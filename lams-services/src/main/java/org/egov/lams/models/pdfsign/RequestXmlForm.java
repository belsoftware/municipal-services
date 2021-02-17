package org.egov.lams.models.pdfsign;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RequestXmlForm {
	public String id;
	public String type;
	public String description;
	public String eSignRequest;
	public String aspTxnID;
	
	@JsonProperty("Content-Type")
	public String contentType = "application/xml";

}