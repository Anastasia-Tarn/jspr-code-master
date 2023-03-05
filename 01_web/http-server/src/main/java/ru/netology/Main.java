package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
  public static void main(String[] args) throws IOException {

    final var server = new Server();

    server.addHandler("GET", "/messages", (request, responseStream) -> {
      var response = "Hello fro POST /message";
      responseStream.write(response.getBytes(), 0, response.length());
    });
    server.addHandler("POST", "/messages", (request, responseStream) -> {
      var response = "Hello fro GET /message";
      responseStream.write(response.getBytes(), 0, response.length());
    });


    server.listen(server.startServer(9991));


  }
}


