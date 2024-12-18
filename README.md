# Debian12-MinecraftAutostart
using systemd autostart

Requirement
*  sudo account (can use root)
*  Screen `sudo apt install -y screen`
*  Adoptium Temurin 21 JDK
*  Already configure server.properties and eula.txt


## The Systemd autostart
Part 1
* firt use this command `sudo nano /etc/systemd/system/minecraft.service` (you can change the name xxxx.service)
* then paste the content of minecraft.service from this repo to the terminal
* save

Part 2
* copy `start.sh` or the copy the content if you have .sh file but remember to change the filename on Part 1
* open terminal then use `cd (directory of the folder)` and use `sudo chmod +x .start.sh`

Part 3
*  on terminal `sudo systemctl enable minecraft` (or the name of the service you created to enable autostart)
*  next `sudo systemctl start minecraft` to start the service
*  to check if its working use `sudo systemctl status minecraft`
*  done!!!

## Use Adoptium Temurin JDK (I recommend it, fxck Oracle and Debian Default JDK)
Installation guide
* `sudo apt install -y wget apt-transport-https gpg`
* `sudo wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | sudo tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null`
* `sudo echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list`
* `sudo apt update` # update if you haven't already
* `sudo apt install temurin-21-jdk`
