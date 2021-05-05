package strava;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

/**
 * Servlet implementation class GetToken
 * Servlet zur Autorisierung bei Strava.
 * Es wird JSON als Protokoll verwendet.
 * Zum aktualisieren in Eclipse das strava markieren und als .war exportieren. Anschliessend
 * im Tomcat-Manager (www.mtbsimulator.de:8085/manager) den alten Service undeploy, das neue war-File hochladen und deployen.
 * 
 * Copyright (C) Bruno Schmidt
 * 
 * @author Bruno Schmidt (mail@mtbsimulator.de)

 */
@WebServlet(description = "Servlet um das Zugriffstoken von Strava zu ermitteln", urlPatterns = { "/GetToken" })
public class GetToken extends HttpServlet {
    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";
    private int clientId = 0;			// hier die clientId einsetzen
    private String secrete  = "";		// hier secrete einsetzen

    private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		//Enumeration parameterNames = request.getParameterNames();
		String code = request.getParameter("code");
		AuthResponse ar = getToken(code);
		//out.print("<head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		out.print("<link type=\"text/css\" rel=\"stylesheet\" href=\"http://www.mtbsimulator.de/mtbs.css\">");
		out.print("<link rel=\"stylesheet\" href=\"http://www.mtbsimulator.de/clean.css\">");
		out.print("<h1>MTB-Simulator Strava Registrierung</h1>");
		if (ar == null) {
			out.print("Es wurde von Strava leider kein gültiger Zugriffscode übermittelt. Die Registrierung war nicht erfolgreich!");
		} else {
			out.print("Zur Verwendung des STRAVA-Uploadbuttons wird noch ein gültiger Zugriffscode in der Konfiguration benötigt.</br></br>");
			out.print("Bitte den unten stehenden Zugriffscode (mit \"Strg-C\") in die Zwischenablage übernehmen ");
			out.print("und anschliessend im MTB-Simulator in der Konfiguration in das entsprechende Feld einfügen (mittels \"Strg-V\") und die Konfiguration schliessen.</br></br>");
			out.print("<img src=\"http://www.mtbsimulator.de/img/stravakey.png\" alt=\"Stravakey\"></br></br>");
			out.print("Zugriffscode: <b>" + ar.getAccess_token() + "</b></br></br>");
			out.print("Bitte anschliessend den Upload zu Strava nochmal durchführen. Dieses Browserfenster kann nach Übernahme des Zugriffscodes geschlossen werden.");
			// evtl. kann man den Token direkt in die Anwendung übermitteln ?
		}
	}

	/**
	 * getToken frägt von strava den gültigen Accesstoken ab und liefert die Klasse AuthResponse zurück.
	 * @param code
	 * @return
	 */
    protected AuthResponse getToken(String code) {
        try {

            URI uri = new URI(TOKEN_URL);
            URL url = uri.toURL();

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            try {
                StringBuilder sb = new StringBuilder();
                sb.append("client_id=" + clientId);
                sb.append("&client_secret=" + secrete);
                sb.append("&code=" + code);

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                OutputStream os = conn.getOutputStream();
                os.write(sb.toString().getBytes("UTF-8"));

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                Reader br = new InputStreamReader((conn.getInputStream()));
                Gson gson = new Gson();
                return gson.fromJson(br, AuthResponse.class);

            } finally {
                conn.disconnect();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
