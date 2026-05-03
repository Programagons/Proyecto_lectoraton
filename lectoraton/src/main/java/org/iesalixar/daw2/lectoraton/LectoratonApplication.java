package org.iesalixar.daw2.lectoraton;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LectoratonApplication {

	public static void main(String[] args) {
		loadDotenvIntoSystemProperties();
		SpringApplication.run(LectoratonApplication.class, args);
	}

	/**
	 * Spring no lee el archivo .env por defecto: lo cargamos antes del contexto para que
	 * {@code ${GOOGLE_CLIENT_ID}} y el resto de variables estén disponibles en {@code application.properties}.
	 * Coloca {@code .env} en el directorio de trabajo (habitualmente la carpeta del módulo {@code lectoraton},
	 * junto al {@code pom.xml}). Si ya existen variables de entorno del sistema o del IDE, no las sobrescribimos.
	 */
	private static void loadDotenvIntoSystemProperties() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();
			for (DotenvEntry e : dotenv.entries()) {
				String key = e.getKey();
				if (key == null || key.isBlank()) {
					continue;
				}
				if (System.getenv(key) != null || System.getProperty(key) != null) {
					continue;
				}
				String value = e.getValue();
				if (value != null) {
					System.setProperty(key, value);
				}
			}
		} catch (Exception ignored) {
			// .env opcional
		}
	}

}
