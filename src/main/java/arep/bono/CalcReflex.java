package arep.bono;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;
import java.util.Objects;

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
                String command = method.split("\\(")[0].split("=")[1];
                String[] values = method.split("\\)")[0].split("\\(")[1].split(",");
                String answer ;
                if(Objects.equals(command, "bbl")){
                    Double[] params = convertDoubles(values);
                    answer = bbl(params);
                }  else{
                    answer = computeMathCommand(command,values);
                }
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

    public static String computeMathCommand(String command, String[] values) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> c = Math.class;
        Method listMethod;
        Double[] params = convertDoubles(values);
        if(params.length == 1){
            listMethod = c.getDeclaredMethod(command, double.class);
            return listMethod.invoke(null, params[0]).toString();
        }else if (params.length == 2) {
            listMethod = c.getDeclaredMethod(command, double.class, double.class);
            return listMethod.invoke(null, params[0], params[1]).toString();
        } else {
            throw new IllegalArgumentException("El método max requiere exactamente 2 parámetros.");
        }
    }


    private static String bbl(Double[] operators){
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
        String answer = "List: ";
        for(int i=0; i< operators.length-1;i++){
            answer += operators[i].toString() +"-";
        }
        return answer+operators[operators.length-1].toString();
    }
    private static boolean isInOrder(Double[] list){
        for(int i =0; i<list.length-1;i++){
            if(list[i] > list[i+1]){
                return false;
            }
        }
        return true;
    }

    private static Double[] convertDoubles(String[] values){
        Double[] params = new Double[values.length];
        for (int i = 0; i < values.length; i++) {
            params[i] = Double.parseDouble(values[i]);
        }
        return params;
    }
}