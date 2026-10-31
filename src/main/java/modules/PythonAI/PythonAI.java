package modules.PythonAI;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PythonAI {

    private final Process process;
    private final BufferedWriter writer;
    private final BufferedReader reader;
    private final BufferedReader errorReader;

    public PythonAI(String pythonPath, String scriptPath) throws IOException {

        ProcessBuilder pb = new ProcessBuilder(
                pythonPath,
                scriptPath
        );

        // Important: don't merge stderr into stdout.
        pb.redirectErrorStream(false);

        process = pb.start();

        writer = new BufferedWriter(
                new OutputStreamWriter(
                        process.getOutputStream(),
                        StandardCharsets.UTF_8
                )
        );

        reader = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        errorReader = new BufferedReader(
                new InputStreamReader(
                        process.getErrorStream(),
                        StandardCharsets.UTF_8
                )
        );
    }

    public String send(String json) throws IOException {

        writer.write(json);
        writer.newLine();
        writer.flush();

        return reader.readLine();
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        errorReader.close();
        process.destroy();
    }
}