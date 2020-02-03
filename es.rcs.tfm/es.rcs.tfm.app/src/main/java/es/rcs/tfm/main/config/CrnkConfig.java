package es.rcs.tfm.main.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Callable;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.db.model.SecApplicationEntity;
import es.rcs.tfm.db.model.SecAuthorityEntity;
import es.rcs.tfm.db.model.SecFunctionEntity;
import es.rcs.tfm.db.model.SecGroupEntity;
import es.rcs.tfm.db.model.SecModuleEntity;
import es.rcs.tfm.db.model.SecRoleEntity;
import es.rcs.tfm.db.model.SecUserEntity;
import es.rcs.tfm.db.repository.SecApplicationRepository;
import es.rcs.tfm.db.repository.SecAuthorityRepository;
import es.rcs.tfm.db.repository.SecFunctionRepository;
import es.rcs.tfm.db.repository.SecGroupRepository;
import es.rcs.tfm.db.repository.SecModuleRepository;
import es.rcs.tfm.db.repository.SecRoleRepository;
import es.rcs.tfm.db.repository.SecUserRepository;
import es.rcs.tfm.main.AppNames;
import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.transaction.TransactionRunner;
import io.crnk.core.queryspec.pagingspec.NumberSizePagingBehavior;
import io.crnk.data.jpa.JpaModule;
import io.crnk.data.jpa.JpaModuleConfig;
import io.crnk.data.jpa.query.querydsl.QuerydslQueryFactory;
import io.crnk.security.ResourcePermission;
import io.crnk.security.SecurityConfig;
import io.crnk.spring.setup.boot.core.CrnkBootConfigurer;
import io.crnk.spring.setup.boot.core.CrnkCoreAutoConfiguration;
import io.crnk.spring.setup.boot.core.CrnkTomcatAutoConfiguration;
import io.crnk.spring.setup.boot.format.PlainJsonFormatAutoConfiguration;
import io.crnk.spring.setup.boot.home.CrnkHomeAutoConfiguration;
import io.crnk.spring.setup.boot.meta.CrnkMetaAutoConfiguration;
import io.crnk.spring.setup.boot.mvc.CrnkSpringMvcAutoConfiguration;
import io.crnk.spring.setup.boot.operations.CrnkOperationsAutoConfiguration;
import io.crnk.spring.setup.boot.security.CrnkSecurityAutoConfiguration;
import io.crnk.spring.setup.boot.security.SecurityModuleConfigurer;
import io.crnk.spring.setup.boot.ui.CrnkUIAutoConfiguration;
import io.crnk.spring.setup.boot.validation.CrnkValidationAutoConfiguration;

@Order(value = 120)
@Configuration( AppNames.CRNK_CONFIG )
@Import( {
		CrnkHomeAutoConfiguration.class,
		CrnkCoreAutoConfiguration.class,
		CrnkValidationAutoConfiguration.class,
		//CrnkJpaAutoConfiguration.class,
		CrnkMetaAutoConfiguration.class,
		CrnkOperationsAutoConfiguration.class,
		CrnkUIAutoConfiguration.class,
		CrnkSecurityAutoConfiguration.class,
		CrnkSpringMvcAutoConfiguration.class,
		//CrnkErrorControllerAutoConfiguration.class,
		PlainJsonFormatAutoConfiguration.class,
		CrnkTomcatAutoConfiguration.class } )
@EnableGlobalMethodSecurity(
		prePostEnabled = true, 
		securedEnabled = true, 
		jsr250Enabled = true)
public class CrnkConfig extends WebSecurityConfigurerAdapter implements CrnkBootConfigurer {

	public static final String CRNK_API_ACTION = "/api/data";
	public static final RequestMatcher CRNK_API_REQUEST = new AntPathRequestMatcher(CRNK_API_ACTION + "/**");
    
	@Override
	public void configure(CrnkBoot boot) {
		boot.addModule(NumberSizePagingBehavior.createModule());
		boot.addModule(JpaModule.createServerModule(
				jpaConfig(), 
				entityManager, 
				transactionRunner()));
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			// Esta Configuracion de seguridad solo se aplica al API
			.requestMatcher(CRNK_API_REQUEST)
			.authorizeRequests()
				.antMatchers(CRNK_API_ACTION + "/**").permitAll()
				.antMatchers(CRNK_API_ACTION + "/**/**").permitAll()
				.antMatchers(HttpMethod.GET, CRNK_API_ACTION + "/**").permitAll()
				.and()
			.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
			.cors() // Se podra acceder desde todas las IP
				.configurationSource(corsConfigurationSource())
				.and()
			.csrf()
				.disable()
//			.addFilter(new JwtAuthenticationFilter(authenticationManager()))
//			.addFilter(new JwtAuthorizationFilter(authenticationManager()))
				;

	}

	@Bean(
			name = AppNames.CRNK_SEC_CONFIG)
	public SecurityModuleConfigurer securityModuleConfiguration() {
		SecurityModuleConfigurer bean = new SecurityModuleConfigurer() {
			@Override
			public void configure(SecurityConfig.Builder config) {
				config.permitRole("tfm.rol.admin",								ResourcePermission.ALL);
				config.permitRole("tfm.rol.query",	SecRoleEntity.class,		ResourcePermission.GET);
				config.permitRole("tfm.rol.user",								ResourcePermission.GET);
				
				// APPS
				config.permitAll(					SecApplicationEntity.class,	ResourcePermission.GET);
				config.permitAll(					SecModuleEntity.class,		ResourcePermission.GET);
				config.permitAll(					SecFunctionEntity.class,	ResourcePermission.GET);
			} 
		};
		return bean;
	}
	
	@Bean(
			name = AppNames.CRNK_CORS_FILTER)
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<CorsFilter>(new CorsFilter(corsConfigurationSource()));
		bean.setOrder(0);
		return bean;
	}

    @Bean(
    		name = AppNames.CRNK_CORS_SETUP)
    public CorsConfigurationSource corsConfigurationSource() {
    	
    	final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addAllowedOrigin("*");
        //config.setAllowedMethods(Arrays.asList(new String[]{"*"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"*"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"*"}));
        //config.setAllowedMethods(Arrays.asList(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE"}));
        //config.setAllowedOrigins(Arrays.asList(new String[]{"http://localhost:4200", "http://localhost:8080", "https://localhost:8443"}));
        //config.setAllowedHeaders(Arrays.asList(new String[]{"Authorization", "Cache-Control", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"}));
        config.applyPermitDefaultValues();
    	
        final UrlBasedCorsConfigurationSource bean = new UrlBasedCorsConfigurationSource();
        bean.registerCorsConfiguration(CRNK_API_ACTION + "/**", config);

        return bean;
        
    }
    
    @Bean (
    		name = AppNames.CRNK_TX_MODULE)
    public TransactionRunner transactionRunner() {
    	TransactionRunner bean = new CrnkTransactionRunner(transactionManager);
    	return bean;
    }
    
    @Bean (
    		name = AppNames.CRNK_JPA_MODULE)
	public JpaModuleConfig jpaConfig() {
		JpaModuleConfig bean = new JpaModuleConfig();
		bean.setQueryFactory(QuerydslQueryFactory.newInstance());
		//bean.exposeAllEntities(parEntityManagerFactory);
		return bean;
	}
    
	@Autowired
	@Qualifier(
			value = AppNames.WEB_JACKSON_MAPPER)
	private ObjectMapper objectMapper;

    @Autowired
    @Qualifier(
    		value = DbNames.DB_EMF)
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    @Qualifier(
    		value = DbNames.DB_TX)
    private PlatformTransactionManager transactionManager;

    @PersistenceContext(
    		unitName = AppNames.BBDD_PU,
    		type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;
	
	@PostConstruct
	@Transactional(
			transactionManager = DbNames.DB_TX,
			propagation = Propagation.REQUIRED)
	public void setup() {
		
		try {

			SecApplicationEntity app = null;
			SecModuleEntity mod = null;
			SecFunctionEntity fun = null;
			SecUserEntity adminUsr = null;
			
			// ----------------------------------------------------------------------------------------------
			// SEGURIDAD - CONTROL DE LEGISLATURA
			
			app = segAppRep.findByCode("tfm.app.adm");
			adminUsr = segUsrRep.findByUsername("admin");
			if (app == null) {
				//app = segAppRep.findByCode("tfm.app.adm");
				//if (app == null)
					app = segAppRep.save(new SecApplicationEntity		( 		"tfm.app.adm",		"Administraci�n", 			"settings",		"admin"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.usr",	"Usuarios", 				"people",		"users"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.grp",	"Grupos", 					"group",		"groups"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.app",	"Aplicaciones", 			"view_comfy",	"applications"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.mod",	"Modulos", 					"view_module",	"modules"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.fun",	"Funciones", 				"view_list",	"functions"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.rol",	"Roles", 					"",	"roles"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.adm.aut",	"Autorizaciones", 			"security",		"authorizations"));
				
				app = segAppRep.findByCode("tfm.app.bus");
				if (app == null)
					app = segAppRep.save(new SecApplicationEntity		( 		"tfm.app.bus",		"Buscador", 				"search",		"search"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.bus.art",	"Articulos", 				"",	"articles"));
				
				app = segAppRep.findByCode("tfm.app.the");
				if (app == null)
					app = segAppRep.save(new SecApplicationEntity		( 		"tfm.app.the",		"Thesaurus", 				"account_tree",	"thesaurus"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.the.des",	"Descriptores", 			"",	"descriptors"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.the.cua",	"Cualificadores", 			"",	"qualifiers"));
				
				app = segAppRep.findByCode("tfm.app.dic");
				if (app == null)
					app = segAppRep.save(new SecApplicationEntity		( 		"tfm.app.dic",		"Diccionario", 				"",	"dictionary"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.dic.voc",	"Vocablos", 				"view_list",	"vocable"));
				
				app = segAppRep.findByCode("tfm.app.cor");
				if (app == null)
					app = segAppRep.save(new SecApplicationEntity		( 		"tfm.app.cor",		"Corpus", 					"library_books","corpus"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.cor.med",	"Pubmed Citas", 			"",	"pubmed"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.cor.pmc",	"Pubmed Central", 			"",	"pmc"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.cor.dat",	"Datasets", 				"",	"datasets"));
				mod = segModRep.save(new SecModuleEntity				( app,	"tfm.app.cor.mod",	"Modelos", 					"",	"models"));
	
				// ROLES
				SecRoleEntity adminRol = 
					segRolRep.save(
						new SecRoleEntity(
							app,
							"tfm.rol.admin",
							"Administrador General",
							new HashSet<SecAuthorityEntity>(Arrays.asList(
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.adm.admin","Administrar m�dulo de Administraci�n")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.bus.admin","Administrar m�dulo Buscador")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.the.admin","Administrar m�dulo de Thesaurus")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.dic.admin","Administrar m�dulo de Diccionario")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.cor.admin","Administrar m�dulo de Corpus")) )) ));
				
				SecRoleEntity userRol = 
					segRolRep.save(
						new SecRoleEntity(
							app,
							"tfm.rol.user",
							"Usuario de consulta",
							new HashSet<SecAuthorityEntity>(Arrays.asList(
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.bus.user","Consultar m�dulo Buscador")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.the.user","Consultar m�dulo de Thesaurus")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.dic.user","Consultar m�dulo de Diccionario")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.cor.user","Consultar m�dulo de Corpus")) )) ));
				
				SecRoleEntity queryRol = 
					segRolRep.save(
						new SecRoleEntity(
							app,
							"tfm.rol.query",
							"Usuario de consulta",
							new HashSet<SecAuthorityEntity>(Arrays.asList(
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.bus.query","Consultar m�dulo Buscador")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.the.query","Consultar m�dulo de Thesaurus")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.dic.query","Consultar m�dulo de Diccionario")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.cor.query","Consultar m�dulo de Corpus")) )) ));
	
				// GRUPOS
				SecGroupEntity editorGrp =
					segGrpRep.save(
						new SecGroupEntity(
							"tfm.grp.edit",
							"Usuario",
							new HashSet<SecAuthorityEntity>(Arrays.asList(
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.bus.edit","Editar m�dulo Buscador")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.the.edit","Editar m�dulo de Thesaurus")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.dic.edit","Editar m�dulo de Diccionario")),
								segAutRep.save(new SecAuthorityEntity	( app,	"tfm.app.cor.edit","Editar m�dulo de Corpus")) )) ));
			
			}
			
			if (adminUsr == null) {
				// USUARIOS
				adminUsr = new SecUserEntity("admin", "dukegris@gmail.com", passwordEncoder.encode("dukegris"));
				//adminUsr.setUsername("dukegris");
				//adminUsr.setEmail("dukegris@gmail.com");
				//adminUsr.setPassword(passwordEncoder.encode("dukegris"));
	
				adminUsr.setEnabled(true);
				adminUsr.setLocked(false);
				adminUsr.setExpired(false);
				adminUsr.setPasswordExpired(false);
	
				adminUsr = segUsrRep.save(adminUsr);
				
			}
			
			adminUsr.setRoles(new HashSet<SecRoleEntity>(Arrays.asList(
					segRolRep.findByCode("tfm.rol.admin"))));
			
			adminUsr.setGroups(new HashSet<SecGroupEntity>(Arrays.asList(
					segGrpRep.findByCode("tfm.grp.edit"))));

			adminUsr.setAuthorities(new HashSet<SecAuthorityEntity>(Arrays.asList(
					segAutRep.findByCode("tfm.app.adm.admin"))));

			adminUsr = segUsrRep.save(adminUsr);
			
			System.out.println(adminUsr.toString());

		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
		
	}

	public class RollbackOnlyException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		private transient Object result;

		public RollbackOnlyException(Object result) {
			this.result = result;
		}

		public Object getResult() {
			return result;
		}
	}
	
	public class CrnkTransactionRunner implements TransactionRunner {
		
		PlatformTransactionManager transactionManager = null;
		
		public CrnkTransactionRunner(PlatformTransactionManager transactionManager) {
			this.transactionManager = transactionManager;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T doInTransaction(final Callable<T> callable) {
			DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
			definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			TransactionTemplate template = new TransactionTemplate(transactionManager, definition);
			try {
				return template.execute(new TransactionCallback<T>() {

					@Override
					public T doInTransaction(TransactionStatus status) {
						try {
							T result = callable.call();
							if (status.isRollbackOnly()) {
								// TransactionTemplate does not properly deal with Rollback exceptions
								// an exception is required, otherwise it will attempt to commit again
								throw new RollbackOnlyException(result);
							}
							return result;
						} catch (RuntimeException e) {
							throw e;
						} catch (Exception e) {
							throw new IllegalStateException(e);
						}
					}
				});
			} catch (RollbackOnlyException e) {
				return (T) e.getResult();
			}
		};
		
	}

	@Autowired
	@Qualifier(	DbNames.DB_USR_REP )
	private SecUserRepository segUsrRep;

	@Autowired
	@Qualifier(	DbNames.DB_GRP_REP )
	private SecGroupRepository segGrpRep;

	@Autowired
	@Qualifier(	DbNames.DB_ROL_REP )
	private SecRoleRepository segRolRep;

	@Autowired
	@Qualifier(	DbNames.DB_AUT_REP )
	private SecAuthorityRepository segAutRep;

	@Autowired
	@Qualifier(	DbNames.DB_APP_REP )
	private SecApplicationRepository segAppRep;

	@Autowired
	@Qualifier(	DbNames.DB_MOD_REP )
	private SecModuleRepository segModRep;

	@Autowired
	@Qualifier(	DbNames.DB_FUN_REP )
	private SecFunctionRepository segFunRep;

	@Autowired
	@Qualifier(	AppNames.SEC_CRYPT_PASSWORD )
	public PasswordEncoder passwordEncoder;

	
}