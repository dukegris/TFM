package es.rcs.tfm.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@JsonApiResource(type = "libraries")
public class Library {
	
	@JsonApiId
	private Long id;
	
	@JsonProperty
	private String name;

}
