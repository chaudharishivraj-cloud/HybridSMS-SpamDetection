# smishing-android
# Smishing SMS Detection - Android App

This is an Android application that reads incoming SMS messages and detects smishing (SMS phishing) attempts by sending the content to a Flask-based backend API.

## ✨ Features

- Reads all inbox SMS messages
- Sends each message to a Flask API for spam detection
- Displays results in a modern RecyclerView with:
  - All Messages
  - Spam Messages
  - Not Spam Messages

## 🧰 Tech Stack

- Java (Android)
- Retrofit for API communication
- RecyclerView + ViewPager2 for UI
- Flask API backend (see below)

## 🔗 Backend

The app connects to a local Flask server. Ensure the Flask server is running on:

