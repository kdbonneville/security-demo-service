package edu.cnm.deepdive.securitydemoservice.service;

import edu.cnm.deepdive.securitydemoservice.model.dao.UserRepository;
import edu.cnm.deepdive.securitydemoservice.model.entity.User;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class UserService implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

  private final UserRepository repository;

  @Autowired
  public UserService(UserRepository repository) {
    this.repository = repository;
  }

  public User getOrCreate(String oauthKey, String displayName) {
    return repository.findFirstByOauthKey(oauthKey)
        .map((user) -> {
          user.setConnected(new Date());
          return repository.save(user);
        })
        .orElseGet(() -> {
          User user = new User();
          user.setOauthKey(oauthKey);
          user.setDisplayName(displayName);
          user.setConnected(new Date());
          return repository.save(user);
        });
  }

  @Override
  public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
    Collection<SimpleGrantedAuthority> grants = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    return new UsernamePasswordAuthenticationToken(
        getOrCreate(jwt.getSubject(), jwt.getClaim("name")), jwt.getTokenValue(), grants);
  }
}
