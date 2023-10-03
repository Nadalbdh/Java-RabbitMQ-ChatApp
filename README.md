# Chat Application with RabbitMQ

## Introduction

This is a simple chat application developed in Java using RabbitMQ for message queuing. It allows users to send and receive messages in real-time with a basic GUI interface. Users can connect to the chat with their chosen usernames and send messages to other users who are online.

## Prerequisites

Before running the application, ensure that you have the following:

1. Java Development Kit (JDK) 11 or higher installed.
2. Apache Maven for building the project.
3. RabbitMQ server running (you can download it from [RabbitMQ](https://www.rabbitmq.com/download.html)).

## Getting Started

1. Clone or download the project from the repository.

2. Open the project in your preferred Java IDE (e.g., Eclipse, IntelliJ IDEA).

3. Build the project using Maven:

   ```
   mvn clean install
   ```

4. Run the `Main` class to start the chat application.

5. Enter your desired username and start chatting with other users.

## Usage

- After launching the application, you can add users to your contact list by typing their usernames and clicking the "Ajouter Utilisateur" button.

- Select a user from your contact list to start a chat with them.

- Type your message in the message box and press Enter or click the "Envoyer" button to send it.

- Received messages will be displayed in the chat area.

## Features

- Real-time messaging using RabbitMQ queues.
- Simple graphical user interface.
- Easy-to-use contact list for managing conversations.
- Ability to send and receive messages to/from other users.
