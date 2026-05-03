package org.iesalixar.daw2.lectoraton.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Autowired
    private KeyPair jwtKeyPair;

    private static final long JWT_EXPIRATION = 36000000; // 1 hora

    /**
     * Clave secreta para firmar y verificar el token JWT.
     * La clave se inyecta desde el archivo application.properties para mantener
     * la seguridad y permitir flexibilidad en entornos diferentes.
     *
     * @Value obtiene el valor configurado en application.properties, por ejemplo:
     *        jwt.secret=tu-clave-super-segura-de-al-menos-32-caracteres
     */
    @Value("${jwt.secret}")
    private String secretKeyFromProperties;

    /**
     * Obtiene la clave de firma (SecretKey) usando la clave secreta inyectada.
     * Keys.hmacShaKeyFor convierte la clave en un objeto SecretKey.
     * <p>
     * Es importante que la clave tenga al menos 256 bits (32 caracteres) para
     * HS256.
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyFromProperties.getBytes());
    }

    /**
     * Método genérico para extraer cualquier claim del token JWT.
     *
     * @param token          el token JWT.
     * @param claimsResolver función para resolver el claim deseado.
     * @return el valor del claim extraído.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims (payload) del token JWT.
     * Utiliza el parser de JJWT configurado con la clave de firma (SecretKey).
     * Verifica la integridad y autenticidad del token antes de extraer los claims.
     *
     * @param token el token JWT.
     * @return los claims contenidos en el token.
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(jwtKeyPair.getPublic()) // Configura la clave para verificar la
                // firma
                .build()
                .parseSignedClaims(token) // Verifica el token y lo parsea
                .getPayload(); // Devuelve el cuerpo del JWT (claims)
    }

    /**
     * Extrae el nombre de usuario (claim "sub") del token.
     * El nombre de usuario suele ser el identificador del usuario que está
     * autenticado.
     *
     * @param token el token JWT del cual se extraerá el claim.
     * @return el nombre de usuario contenido en el token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Genera un token JWT para un usuario con roles específicos.
     * Incluye los roles y el ID del usuario en el token como parte de los claims y
     * configura
     * una duración de 1 hora.
     *
     * @param username el nombre del usuario para el cual se genera el token.
     * @param roles    la lista de roles del usuario (por ejemplo, ["USER",
     *                 "ADMIN"]).
     * @param id       el ID del usuario.
     * @return el token JWT generado.
     */
    public String generateToken(String username, List<String> roles, Long id) {
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .claim("id", id)
                .issuedAt(new Date()) // Fecha de emisión del token
                .expiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION)) // Expira en 1 hora
                .signWith(jwtKeyPair.getPrivate(), Jwts.SIG.RS256) // Firma el token con la clave secreta
                .compact(); // Genera el token en formato JWT
    }

    /**
     * Valida un token JWT verificando:
     * 1. Que el nombre de usuario extraído del token coincida con el esperado.
     * 2. Que el token no haya expirado.
     *
     * @param token    el token JWT.
     * @param username el nombre de usuario esperado.
     * @return true si el token es válido, false en caso contrario.
     */

    public boolean validateToken(String token, String username) {
        Claims claims = Jwts.parser()
         .verifyWith(jwtKeyPair.getPublic())
         .build()
         .parseSignedClaims(token)
         .getPayload();
         return username.equals(claims.getSubject()) && !isTokenExpired(claims);

       /* final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);*/

    }

    /**
     * Verifica si un token JWT ha expirado.
     * Extrae el claim "exp" (fecha de expiración) y lo compara con la fecha actual.
     *
     * @param claims el token JWT.
     * @return true si el token ha expirado, false si aún es válido.
     */
    /*
     * private boolean isTokenExpired(Claims claims) {
     * return claims.getExpiration().before(new Date());
     * }
     */
   /* private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }*/

    private boolean isTokenExpired (Claims claims){
        return claims.getExpiration().before(new Date());
    }

}
