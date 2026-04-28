package pe.idat.BackEndConecta.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;
    
    public String getToken(UserDetails user){ return getToken(new HashMap<>(), user);}
    
    private String getToken(Map<String, Object> extraClaims, UserDetails user){
        List<String> roles = user.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        List<String> permissions = user.getAuthorities().stream()
                        .filter(authority -> !authority.getAuthority().startsWith("ROLE_"))
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList());
        extraClaims.put("roles", roles);
        extraClaims.put("permissions", permissions);
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getKey())
                .compact();
    }
    
    //Generar un RefreshToken
    public String getRefreshToken(UserDetails user){ return getRefreshToken(new HashMap<>(), user);}
    
    private String getRefreshToken(Map<String, Object> extraClaims, UserDetails user){
        List<String> roles = user.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        extraClaims.put("roles", roles);
        
        return Jwts.builder()
                .claims(extraClaims)
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getKey())
                .compact();
    }

    private SecretKey getKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String getUsernameFromToken(String token) {
        return getClaims(token, Claims::getSubject);
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaims(token, claims -> claims.get("roles", List.class));
    }
    
    @SuppressWarnings("unchecked")
    public List<String> getPermissionsFromToken(String token) {
        return getClaims(token, claims -> claims.get("permissions", List.class));
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    private Claims getAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public <T> T getClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Date getExpiration(String token) {
        return getClaims(token, Claims::getExpiration);
    }
    
    private boolean isTokenExpired(String token) {
        return getExpiration(token).before(new Date());
    }
}
