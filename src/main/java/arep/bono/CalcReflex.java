package arep.bono;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

import static arep.bono.CalcReflexFachada.getHttpClient;
import static arep.bono.CalcReflexFachada.getRequestURI;

public class CalcReflex {
    public static void main(String[] args) throws IOException, URISyntaxException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
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
            if(reqURL.getPath().startsWith("/compreflex")){
                String method = reqURL.toString().split("\\?")[1];
                String command = method.split("\\(")[0];
                String[] values = method.split("\\)")[0].split(",");
                String answer = computeMathCommand(command,values);
                outputLine = "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"
                        + answer;
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

    public static String computeMathCommand(String command,String[] values) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class c = Math.class;
        Class[] parametersTypes = {double.class,};
        Object[] params = new Double[values.length];
        for(int i = 0; i<values.length; i++){
            params[i] = Double.parseDouble(values[i]);
        }
        Method listmethod = c.getDeclaredMethod(command, parametersTypes);
        String resp = listmethod.invoke(null,(Object) params).toString();
        return resp;
    }

    private static double[] bbl(double[] operators){
        while(!isInOrder(operators)){
            for(int i=0; i< operators.length -1; i++){
                if(operators[i] > operators[i+1]){
                    double min = operators[i+1];
                    double max = operators[i];
                    operators[i] = min;
                    operators[i+1] = max;
                }
            }
        }
        return operators;
    }
    private static boolean isInOrder(double[] list){
        for(int i =0; i<list.length-1;i++){
            if(list[i] > list[i+1]){
                return false;
            }
        }
        return true;
    }
}