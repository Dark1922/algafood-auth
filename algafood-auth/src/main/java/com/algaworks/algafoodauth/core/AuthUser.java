package com.algaworks.algafoodauth.core;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.algaworks.algafoodauth.domain.Usuario;

import lombok.Getter;

@Getter
public class AuthUser extends User {
	
	private static final long serialVersionUID = 1L;
	
	private String fullName;

	private Long userId;

	public AuthUser(Usuario usuario, Collection<? extends  GrantedAuthority> authorities) {
		super(usuario.getEmail(), usuario.getSenha(), authorities);
		
		this.fullName = usuario.getNome();
		this.userId = usuario.getId();
	}

	

}
