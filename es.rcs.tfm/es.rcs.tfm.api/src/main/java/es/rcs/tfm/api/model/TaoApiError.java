package es.rcs.tfm.api.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class TaoApiError implements Serializable {
	
	private static final long serialVersionUID = -8679332080744649147L;

	// -------------------------------------------------------------------------------------
	// ERRORES DEL API
	public static final String SERVER	= "serverError";
	public static final String REQUIRED	= "required";
	public static final String MIN		= "min";
	public static final String MAX		= "max";

	/**
	 * Descripcion del error en detalle
	 */
	public class TaoApiSubError {
		
		private String object;
		private String field;
		private Object value;
		private String error;
		private String message;

		// CONSTRUCTOR -------------------------------------------------------------------------------------------
		public TaoApiSubError() {
		}

		public TaoApiSubError(String object, String error, String message) {
            this.object = object;
            this.error = error;
            this.message = message;
        }

		public TaoApiSubError(String object, String field, Object value, String error, String message) {
			this.object = object;
			this.field = field;
			this.value = value;
            this.error = error;
			this.message = message;
		}
		
		// UTILITIES ---------------------------------------------------------------------------------------------
		@Override
		public String toString() {
			return "TaoApiSubError [" 
					+ (object != null ? "object=" + object + ", " : "")
					+ (field != null ? "field=" + field + ", " : "")
					+ (value != null ? "value=" + value + ", " : "")
					+ (error != null ? "error=" + error : "") 
					+ (message != null ? "message=" + message : "") 
					+ "]";
		}
		
		// GENERATED ---------------------------------------------------------------------------------------------
		public String getObject() {
			return object;
		}
		public void setObject(String object) {
			this.object = object;
		}
		public String getField() {
			return field;
		}
		public void setField(String field) {
			this.field = field;
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public String getError() {
			return error;
		}
		public void setError(String error) {
			this.error = error;
		}

	};

	/**
	 * Descripcion del error en detalle para casos de validacion
	 */
    public class TaoApiValidationError extends TaoApiSubError {

		// CONSTRUCTOR -------------------------------------------------------------------------------------------
		public TaoApiValidationError() {
			super();
		}

		public TaoApiValidationError(String object, String error, String message) {
			super(object, error, message);
        }

		public TaoApiValidationError(String object, String field, Object rejectedValue, String error, String message) {
			super(object, field, rejectedValue, error, message);
		}

    }	

	/**
	 * Descripcion del error en detalle para casos de enlaces
	 */
    public class TaoApiBindingError extends TaoApiSubError {

		// CONSTRUCTOR -------------------------------------------------------------------------------------------
		public TaoApiBindingError() {
			super();
		}

		public TaoApiBindingError(String object, String error, String message) {
			super(object, error, message);
        }

		public TaoApiBindingError(String object, String field, Object rejectedValue, String error, String message) {
			super(object, message, rejectedValue, error, message);
		}

    }
    
	/**
	 * Descripcion del error en detalle para casos de validacion
	 */
	public class LowerCaseClassNameResolver extends TypeIdResolverBase {

		@Override
		public String idFromValue(Object value) {
			return value.getClass().getSimpleName().toLowerCase();
		}

		@Override
		public String idFromValueAndType(Object value, Class<?> suggestedType) {
			return idFromValue(value);
		}

		@Override
		public JsonTypeInfo.Id getMechanism() {
			return JsonTypeInfo.Id.CUSTOM;
		}

		// CONSTRUCTOR -------------------------------------------------------------------------------------------
		public LowerCaseClassNameResolver() {
			super();
		}

	}
	
	
	// {
	//  "timestamp":1420442772928,
	//  "status":401,
	//  "error":"Unauthorized",
	//  "message":"Full authentication is required to access this resource",
	//  "path":"/resource"
	// }

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
	private LocalDateTime timestamp; 
	private HttpStatus httpStatus;
	private int status;  
	private String error;
	private String message;
	private String path;
	private List<TaoApiSubError> subErrors;

	private void addSubError(TaoApiSubError subError) {
		if (subErrors == null) {
			subErrors = new ArrayList<>();
		}
		subErrors.add(subError);
	}

	public void addValidationError (
			String object, 
			String field, 
			Object value, 
			String error, 
			String message) {
		addSubError(new TaoApiValidationError(
			object, 
			field, 
			value, 
			error,
			message));
	}

	public void addValidationError (
			String object, 
			String error, 
			String message) {
		addSubError(new TaoApiValidationError(
			object, 
			error,
			message));
	}

	private void addValidationError (
			FieldError fieldError) {
		this.addValidationError(
			fieldError.getObjectName(), 
			fieldError.getField(), 
			fieldError.getRejectedValue(),
			fieldError.getCode(),
			fieldError.getDefaultMessage());
	}

	private void addValidationError(
			ObjectError objectError) {
		this.addValidationError(
			objectError.getObjectName(), 
			objectError.getCode(),
			objectError.getDefaultMessage());
	}

	public void addValidationErrors(
			List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	public void addValidationError(
			List<ObjectError> objectErrors) {
		objectErrors.forEach(this::addValidationError);
	}

	/**
	 * Utility method for adding error of ConstraintViolation. Usually when
	 * a @Validated validation fails.
	 *
	 * @param cv
	 *            the ConstraintViolation
	 */
	private void addValidationError(
			ConstraintViolation<?> cv) {
		this.addValidationError(
			cv.getRootBeanClass().getSimpleName(),
			((PathImpl) cv.getPropertyPath()).getLeafNode().asString(), 
			cv.getInvalidValue(), 
			SERVER,
			cv.getMessage());
	}

	void addValidationErrors(
			Set<ConstraintViolation<?>> constraintViolations) {
		constraintViolations.forEach(this::addValidationError);
	}

	public void addBindingError(
			String object, 
			String field, 
			Object value, 
			String error, 
			String message) {
		addSubError(new TaoApiBindingError(
			object, 
			field, 
			value, 
			error,
			message));
	}

	public void addBindingErrors(
			List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addBindingError);
	}

	public void addBindingError(
			FieldError fieldError) {
		this.addBindingError(
			fieldError.getObjectName(), 
			fieldError.getField(), 
			fieldError.getRejectedValue(),
			fieldError.getCode(),
			fieldError.getDefaultMessage());
	}

	public void addBindingError(
			BindingResult bindingResult) {
		bindingResult.getFieldErrors().forEach(this::addBindingError);
	}
	    
	// UTILITIES ---------------------------------------------------------------------------------------------
	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		String errorsAsJSON = "";
		try {
			errorsAsJSON = mapper.writeValueAsString(this);
		} catch (JsonProcessingException ex) {
			ex.printStackTrace();
		}
		return errorsAsJSON;
	}

	@Override
	public String toString() {
		return "TaoApiError [" 
				+ (timestamp != null ? "timestamp=" + timestamp + ", " : "") 
				+ "status=" + status + ", "
				+ (error != null ? "error=" + error + ", " : "") 
				+ (message != null ? "message=" + message + ", " : "")
				+ (path != null ? "path=" + path : "") 
				+ "]";
	}

	// CONSTRUCTOR -------------------------------------------------------------------------------------------
	public TaoApiError(HttpStatus status, String message, String path) {
		super();
		this.httpStatus = status;
		this.status = status.value();
		this.error = status.name();
		this.message = message;
		this.path = path;
		this.timestamp = LocalDateTime.now();
		
	}

	// GENERATED ---------------------------------------------------------------------------------------------
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
	public void setHttpStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public List<TaoApiSubError> getSubErrors() {
		return subErrors;
	}
	public void setSubErrors(List<TaoApiSubError> subErrors) {
		this.subErrors = subErrors;
	}

}
