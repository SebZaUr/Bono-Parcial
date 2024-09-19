package arep.bono;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

import static arep.bono.CalcReflexFachada.getHttpClient;
import static arep.bono.CalcReflexFachada.getRequestURI;

public class CalcReflex {
    public static void main(String[] args) throws IOException, URISyntaxException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while(running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean isFirstLine = true;
            String firstLine = "";
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Recibí: " + inputLine);
                if(isFirstLine){
                    firstLine=inputLine;
                    isFirstLine = false;
                }
                if (!in.ready()) {
                    break;
                }
            }
            URI reqURL = getRequestURI(firstLine);
            if(reqURL.getPath().startsWith("/computar")){
                outputLine = HttpConnection.getResponse("/compreflex?" + reqURL.getQuery());
            }else{
                outputLine = getHttpClient();
            }
            out.println(outputLine);
            out.close();
            in.close();
            clientSocket.close();
        }
        serverSocket.close();
    }
    public static String getHttpClient(){
        String htmlcode="HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html\r\n" +
                "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <head>\n" +
                "        <title>Form Example</title>\n" +
                "        <meta charset=\"UTF-8\">\n" +
                "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    </head>\n" +
                "    <body>\n" +
                "        <h1>Method Not Found</h1>\n" +
                "</html>";
        return htmlcode;
    }

    public static URI getRequestURI(String firstLine) throws  URISyntaxException {
        String rurl = firstLine.split(" ")[1];
        return new URI(rurl);
    }

    public static String computeMathCommand(String command) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class c = Math.class;
        Class[] parametersTypes = {double.class,};
        Method listmethod = c.getDeclaredMethod("abs", parametersTypes);
        Object[] params = {-2.0};
        String resp = listmethod.invoke(null,(Object) params).toString();
        return "";
    }
}