#!/bin/bash

# Navigate to the Minecraft directory
cd /home/keon/minecraft

# Start Minecraft in a screen session named "minecraft"
screen -dmS minecraft java -Xmx1024M -Xms1024M -jar minecraft_server.jar nogui

