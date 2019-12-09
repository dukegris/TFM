package es.rcs.tfm.srv;

public class SrvException extends RuntimeException {
	
	private static final long serialVersionUID = 2132415014761224012L;

	private SrvViolation formatViolation;

	public SrvException(String message) {
		super(message);
	}
    
    public SrvException(SrvViolation formatViolation, final String message) {
		super(message);
		this.formatViolation = formatViolation;
	}

    public SrvException(SrvViolation formatViolation, Exception ex) {
    	super(ex);
		this.formatViolation = formatViolation;
    }

	public SrvViolation getFormatViolation() {
		return formatViolation;
	}
	
	public static enum SrvViolation {
    	
        UNKNOWN,
        FILE_FAIL,
        SAX_FAIL,
        JAXB_FAIL, 
        NO_MORE_DATA

	}

}
