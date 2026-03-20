package com.example.daj2ee.security;

import com.example.daj2ee.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Adapter that exposes our domain {@link User} as a Spring Security {@link UserDetails}.
 *
 * Provides a convenient {@link #create(User)} factory to convert from the entity.
 */
public class UserPrincipal implements UserDetails {

  private final Long id;
  private final String username;

  @JsonIgnore
  private final String password;

  private final Collection<? extends GrantedAuthority> authorities;
  private final boolean enabled;

  public UserPrincipal(
    Long id,
    String username,
    String password,
    Collection<? extends GrantedAuthority> authorities,
    boolean enabled
  ) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.authorities = authorities;
    this.enabled = enabled;
  }

  /**
   * Create a {@link UserPrincipal} from a domain {@link User}.
   *
   * @param user domain user
   * @return UserPrincipal instance
   */
  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities = user
      .getRoleList()
      .stream()
      .map(SimpleGrantedAuthority::new)
      .collect(Collectors.toList());

    return new UserPrincipal(
      user.getId(),
      user.getUsername(),
      user.getPassword(),
      authorities,
      user.isEnabled()
    );
  }

  public Long getId() {
    return id;
  }

  // --- UserDetails implementation ---

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  // For the basic implementation we treat all accounts as non-expired / non-locked / credentials non-expired.
  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  // equals/hashCode based on id to allow identity checks in collections and caches

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserPrincipal that = (UserPrincipal) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }

  @Override
  public String toString() {
    return (
      "UserPrincipal{" +
      "id=" +
      id +
      ", username='" +
      username +
      '\'' +
      ", authorities=" +
      authorities +
      ", enabled=" +
      enabled +
      '}'
    );
  }
}
