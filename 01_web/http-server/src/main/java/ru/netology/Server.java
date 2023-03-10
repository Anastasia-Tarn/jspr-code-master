package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private ExecutorService executorService = Executors.newFixedThreadPool(64);

    public final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

    private final ConcurrentHashMap <String, ConcurrentHashMap<String, Handler>> handlers = new ConcurrentHashMap<>();

    public Server() {
    }

    public ServerSocket startServer(int port) {
        try {
            return new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void listen(ServerSocket server) throws IOException {

        Socket socket = server.accept();

        executorService.execute(new Thread(() -> {

            try (final var in = new BufferedInputStream(socket.getInputStream());
                 final var out = new BufferedOutputStream(socket.getOutputStream())) {

                final var limit = 4096;

                in.mark(limit);

                final var buffer = new byte[limit];
                final var read = in.read(buffer);

                final var requestLineDelimiter = new byte[]{'\r', '\n'};
                final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
                if (requestLineEnd == -1) {
                    badRequest(out);
                    socket.close();
                }

                final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
                if (requestLine.length != 3) {
                    badRequest(out);
                    socket.close();
                }

                final var headersDelimiter = new byte[]{'\r', '\n', '\r', '\n',};
                final var headersStart = requestLineEnd + requestLineDelimiter.length;
                final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
                if (headersEnd == -1) {
                    badRequest(out);
                    socket.close();
                }

                in.reset();
                in.skip(headersStart);

                final var headersBytes = in.readNBytes(headersEnd - headersStart);
                final var headers = Arrays.asList(new String(headersBytes).split("\r\n"));

                final var method = requestLine[0];

                final var path = requestLine[1];
                if (!validPaths.contains(path)) {
                    badRequest(out);
                    socket.close();
                }


                in.skip(headersDelimiter.length);
                final var contentLength = extractHeader(headers, "Content-length");
                final var length = Integer.parseInt(contentLength.get());
                final var bodyBytes = in.readNBytes(length);


                final var request = new Request(requestLine[0], requestLine[1], requestLine[2], headers, bodyBytes);


                final var filePath = Path.of(".", "public", path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals("/classic.html")) {
                    final var template = Files.readString(filePath);
                    final var content = template.replace(
                            "{time}",
                            LocalDateTime.now().toString()
                    ).getBytes();

                    out.write(("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n").getBytes());

                    out.flush();
                    socket.close();
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }));
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }


    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }


    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write(("HTTP/1.1 404 Not Found\r\n" +
                "Content-Length: 0\r\n" +
                "Connection: close\r\n" +
                "\r\n"
        ).getBytes());
        out.flush();
    }


    public void addHandler(String method, String path, ru.netology.Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new ConcurrentHashMap<>());
        }

        handlers.get(method).put(path, handler);
    }
}

