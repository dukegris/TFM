package es.rcs.tfm.api.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import es.rcs.tfm.api.ApiNames;

@RestController(ApiNames.API_URL_BASE + ApiNames.API_ACCOUNT)
public class AccessController {

	public static final String API_ACCOUNT_BASE		= ApiNames.API_URL_BASE + ApiNames.API_ACCOUNT;
	public static final String API_LOGIN_ACTION		= API_ACCOUNT_BASE + "/login";
	public static final String API_LOGOUT_ACTION	= API_ACCOUNT_BASE + "/logout";
	public static final String API_ACCOUNTME_ACTION	= API_ACCOUNT_BASE + "/me";

	public static final String PARAM_ERROR = "error";

	@RequestMapping(
		value=AccessController.API_ACCOUNTME_ACTION,
		method={RequestMethod.GET})
	public Principal user(Principal user) {
		return user;
	}

}
