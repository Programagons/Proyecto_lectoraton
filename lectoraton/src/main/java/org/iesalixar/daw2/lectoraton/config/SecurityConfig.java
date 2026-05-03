package org.iesalixar.daw2.lectoraton.config;

import org.iesalixar.daw2.lectoraton.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configura la seguridad de la aplicación, definiendo autenticación y
 * autorización
 * para diferentes roles de usuario, y gestionando la política de sesiones.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Activa la seguridad basada en métodos
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * API REST JWT (solo rutas {@code /api/**}). Las rutas {@code /oauth2/**} y {@code /login/oauth2/**}
     * se configuran aparte cuando Google OAuth2 está habilitado.
     *
     * @param http instancia de {@link HttpSecurity} para configurar la seguridad.
     * @return una instancia de {@link SecurityFilterChain} que contiene la
     *         configuración de seguridad.
     * @throws Exception si ocurre un error en la configuración de seguridad.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .securityMatcher("/api/**") // Solo protege rutas que empiezan con "/api/"
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable()) // Las APIs REST no suelen necesitar CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin
                                                                                                              // sesiones
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api").permitAll()
                        .requestMatchers("/api/v1/authenticate", "/api/v1/register",
                                "/api/v1/oauth/google-enabled", "/swagger-ui/**",
                                "/v3/api-docs/**", "/swagger-ui.html", "/api-docs", "/api-docs/**")
                        .permitAll()
                        // Lectura pública para página de inicio (libros, autores, géneros, tropos)
                        .requestMatchers(HttpMethod.GET, "/api/libros", "/api/libros/**", "/api/autores", "/api/autores/**", "/api/generos", "/api/generos/**", "/api/tropos", "/api/tropos/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/editor-requests/approve")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Filtro JWT
        return http.build();
    }

    /**
     * Configura el proveedor de autenticación para usar el servicio de detalles de
     * usuario
     * personalizado y el codificador de contraseñas.
     *
     * @return una instancia de {@link DaoAuthenticationProvider) para la
     *         autenticación.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configura el codificador de contraseñas para cifrar las contraseñas de los
     * usuarios
     * utilizando BCrypt.
     *
     * @return una instancia de {@link PasswordEncoder} que utiliza BCrypt para
     *         cifrar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.info("Entrando en el método passwordEncoder");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        logger.info("Saliendo del método passwordEncoder");
        return encoder;
    }

    /**
     * Configura y expone un bean de tipo {@link AuthenticationManager}.
     * <p>
     * En Spring Security, el 'AuthenticationManager es el componente principal que
     * se encarga
     * de procesar solicitudes de autenticación. Este método obtiene la instancia de
     * AuthenticationManager configurada automáticamente por Spring a través de
     * "Authentication Configuration y la expone como un bean disponible en el
     * contexto
     * de la aplicación.
     *
     * @param configuration Objeto de tipo {@link AuthenticationConfiguration} que
     *                      contiene
     *                      la configuración de autenticación de Spring Security.
     *                      Este objeto incluye los detalles del flujo de
     *                      autenticación configurado, como
     *                      el proveedor de autenticación y los detalles del
     *                      usuario.
     * @return Una instancia de {@link AuthenticationManager} configurada con los
     *         detalles
     *         <p>
     *         especificados en la aplicación.
     * @throws Exception Si ocurre algún error al obtener el AuthenticationManager`.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        // Obtiene y devuelve el AuthenticationManager desde la configuración
        // proporcionada

        return configuration.getAuthenticationManager();

    }

}