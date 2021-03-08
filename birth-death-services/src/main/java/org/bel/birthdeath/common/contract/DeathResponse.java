package org.bel.birthdeath.common.contract;

import java.util.List;

import javax.validation.Valid;

import org.bel.birthdeath.death.model.EgDeathDtl;
import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeathResponse   {
        @JsonProperty("ResponseInfo")
        private ResponseInfo responseInfo = null;

        @JsonProperty("deathCerts")
        @Valid
        private List<EgDeathDtl> deathCerts = null;

}

