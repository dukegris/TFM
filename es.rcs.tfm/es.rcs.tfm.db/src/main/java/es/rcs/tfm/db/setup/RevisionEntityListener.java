package es.rcs.tfm.db.setup;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import es.rcs.tfm.db.model.AuditedRevisionEntity;

public class RevisionEntityListener implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		if (revisionEntity instanceof AuditedRevisionEntity) {
			AuditedRevisionEntity obj = (AuditedRevisionEntity) revisionEntity;
			String modifiedBy = getUsernameOfAuthenticatedUser();
			obj.setModifiedBy(modifiedBy);
		}
	}
	    
   public static String getUsernameOfAuthenticatedUser() {
    	
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymousUser";
        }
 
        String username = "";
        Object principal = authentication.getPrincipal();
        if (principal.getClass().equals(String.class)) {
        	username = (String)principal;
        //} else if (principal.getClass().equals(TaoDetails.class)) {
        //	username = ((TaoDetails)principal).getUsername();
    	//} else if (principal instanceof LdapUserDetails) {
        //    username = authentication.getName();
        } else if (principal.getClass().equals(UserDetails.class)) {
            username = ((UserDetails)principal).getUsername();
        } else if (principal.getClass().equals(User.class)) {
            username = ((User)principal).getUsername();
        } else {
        	username = principal.toString();
        }
     
        return username;

    }

}
