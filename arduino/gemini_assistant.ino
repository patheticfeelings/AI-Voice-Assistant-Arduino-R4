#include <WiFiS3.h>
#include <ArduinoHttpClient.h>
#include <ArduinoJson.h>

// --- SECURITY: Never hardcode your real keys here for GitHub ---
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";
const char* geminiApiKey = "YOUR_GEMINI_API_KEY";

const char* geminiHost = "generativelanguage.googleapis.com";
const int geminiPort = 443;
const char* geminiPath = "/v1beta/models/gemini-2.0-flash:generateContent?key=";

WiFiSSLClient wifiClient;
HttpClient client = HttpClient(wifiClient, geminiHost, geminiPort);
WiFiServer server(8080);

void setup() {
  Serial.begin(115200);
  while (!Serial);

  connectWiFi();
  server.begin();
  Serial.println("TCP Server started on port 8080");
}

void loop() {
  WiFiClient clientSocket = server.available();

  if (clientSocket) {
    String clientData = "";
    // Safety timeout to prevent infinite loops
    unsigned long timeout = millis();
    while (clientSocket.connected() && millis() - timeout < 3000) {
      if (clientSocket.available()) {
        char c = clientSocket.read();
        if (c == '\n') break;
        clientData += c;
      }
    }
    
    clientData.trim();
    if (clientData.length() > 0) {
      Serial.println("Request: " + clientData);
      String aiResponse = askGemini(clientData);
      
      // Send response back to Android
      clientSocket.println(aiResponse);
      Serial.println("Response sent.");
    }
    clientSocket.stop();
  }
}

String askGemini(String question) {
  DynamicJsonDocument doc(1024);
  // Constraints for short responses to save Arduino memory
  String prompt = question + " (Reply in max 15 words)";
  
  JsonArray contentsParts = doc.createNestedObject("contents").createNestedArray("parts");
  contentsParts.createNestedObject()["text"] = prompt;

  String requestBody;
  serializeJson(doc, requestBody);

  client.post(String(geminiPath) + geminiApiKey, "application/json", requestBody);

  int statusCode = client.responseStatusCode();
  String response = client.responseBody();

  if (statusCode == 200) {
    DynamicJsonDocument responseDoc(2048);
    deserializeJson(responseDoc, response);
    return responseDoc["candidates"][0]["content"]["parts"][0]["text"].as<String>();
  } else {
    return "Error: " + String(statusCode);
  }
}

void connectWiFi() {
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nConnected! IP: " + WiFi.localIP().toString());
}