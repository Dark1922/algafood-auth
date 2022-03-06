package com.algaworks.algafoodauth;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;


@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig  extends AuthorizationServerConfigurerAdapter {
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtKeyStoreProperties jwtKeyStoreProperties;  
	
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients
			.inMemory()
				 .withClient("algafood-web")
				 .secret(passwordEncoder.encode("web123"))
				 .authorizedGrantTypes("password", "refresh_token")
				 .scopes("write", "read")
				 .accessTokenValiditySeconds(6 * 60 * 60) // 6 horas (padrão é 12 horas)
				.refreshTokenValiditySeconds(60 * 24 * 60 * 60) // 60 dias
				  
				.and()
				 .withClient("faturamento")
				 .secret(passwordEncoder.encode("faturamento123"))
				 .authorizedGrantTypes("client_credentials")
				 .scopes("write", "read")
				 
				.and()
				 .withClient("foodanalytics")
				 .secret("food123")
				 .authorizedGrantTypes("authorization_code")
				 .scopes("write", "read")
				 .redirectUris("http://www.foodanalytics.local:8082")
				 
					.and()
					 .withClient("webadmin")
					 .authorizedGrantTypes("impliciti")
					 .scopes("write", "read")
					 .redirectUris("http://aplicacao-cliente")
				 
				.and()
				   .withClient("checktoken")
				   .secret(passwordEncoder.encode("check123"));
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
		.authenticationManager(authenticationManager)//endpoint de gerar o token
		.userDetailsService(userDetailsService)//tava nulo ent colcoamos o userDetailsService
		.reuseRefreshTokens(false)
		.accessTokenConverter(jwtAccessTokenConverter()) //conversos de access token jwt
		.tokenGranter(tokenGranter(endpoints));
		 
	}

	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
		
		//chave simetrica
		//jwtAccessTokenConverter.setSigningKey("DB4AEF4719809709E560ED8DE2F9C77B886B963B28BA20E9A8A621BBD4ABA400");
		
		//chave assimetrica
		    var jksResource = new ClassPathResource(jwtKeyStoreProperties.getPath());
		    var keyStorePass = jwtKeyStoreProperties.getPassword();
		    var keyPairAlias = jwtKeyStoreProperties.getKeypairAlias();
		    
		    var keyStoreKeyFactory = new KeyStoreKeyFactory(jksResource, keyStorePass.toCharArray());
		    var keyPair = keyStoreKeyFactory.getKeyPair(keyPairAlias);
		    
		    jwtAccessTokenConverter.setKeyPair(keyPair);
		return jwtAccessTokenConverter;
	}
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
	//	security.checkTokenAccess("isAuthenticated()");//para acessar o endpoint de check token tem que está autenticado
		security.checkTokenAccess("permitAll()"); //permite acesso sem o acesso do base auth
	}

	private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
		var pkceAuthorizationCodeTokenGranter = new PkceAuthorizationCodeTokenGranter(endpoints.getTokenServices(),
				endpoints.getAuthorizationCodeServices(), endpoints.getClientDetailsService(),
				endpoints.getOAuth2RequestFactory());
		
		var granters = Arrays.asList(
				pkceAuthorizationCodeTokenGranter, endpoints.getTokenGranter());
		
		return new CompositeTokenGranter(granters);
	}
	
}
 