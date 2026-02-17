# AI Voice Assistant: Arduino UNO R4 WiFi & Gemini API Integration

## Project Overview

Developed in **May 2025**, this project explores the integration of Physical Computing with Generative AI. The goal was to build a voice-activated assistant using the **Arduino UNO R4 WiFi** and Google's **Gemini API**.

The system captures voice input via a custom Android application (Kotlin), converts it to text, and transmits it via TCP Sockets to the Arduino, which then queries the Gemini AI for an intelligent response.

## Technical Architecture

- **Microcontroller:** Arduino UNO R4 WiFi (Server)
- **Mobile Client:** Android App developed in Kotlin (Speech-to-Text)
- **AI Engine:** Google Gemini API (via HTTPS Requests)
- **Communication:** TCP/IP Sockets & JSON

## Key Features

- **Speech Recognition:** Real-time voice-to-text conversion using Android's SpeechRecognizer.
- **Wireless Communication:** Arduino acting as a TCP Server to receive commands over the local network.
- **AI Integration:** Direct HTTPS POST requests from Arduino to Gemini API using the `WiFiS3` and `ArduinoHttpClient` libraries.
- **Dynamic Response Handling:** Parsing complex JSON responses on a microcontroller.

## Challenges & Hardware Limitations

During development, several technical obstacles were encountered:

- **Audio Processing:** The Arduino UNO R4 WiFi faced challenges processing low-voltage analog signals from microphones (millivolt range) without dedicated amplification.
- **Peripheral Integration:** Stability issues occurred when attempting to drive an LCD screen and speakers simultaneously with high-frequency network requests.
- **Solution:** The project was pivoted to a hybrid architecture where the smartphone handles heavy audio processing, while the Arduino manages the AI logic and system control.

## Project Timeline

- **Concept & Research:** April 2025
- **Development & Prototyping:** May 2025
- **Final Documentation:** May 13, 2025

## License

This project is licensed under the MIT License.
