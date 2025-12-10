# Deployment Guide for Amazon EC2

This guide outlines the steps to deploy the RevHub Social Media Application to an Amazon EC2 instance using Docker and Docker Compose.

## Prerequisites

1.  **EC2 Instance**: You should have a running EC2 instance (Ubuntu 22.04 LTS recommended).
2.  **SSH Access**: You must have the `.pem` key file to SSH into your instance.
3.  **Security Group Rules**: Ensure the following inbound ports are open in your EC2 Security Group:
    *   `22` (SSH)
    *   `80` (HTTP) - *If using production mapping*
    *   `8088` (Frontend) - *Current configuration*
    *   `8081` (Backend) - *Current configuration*

## Step 1: Connect to your EC2 Instance

Open your terminal and run:

```bash
ssh -i /path/to/your-key.pem ubuntu@<your-ec2-public-ip>
```

## Step 2: Install Docker and Docker Compose

Run the following commands on your EC2 instance to install Docker:

```bash
# Update package database
sudo apt-get update

# Install Docker
sudo apt-get install -y docker.io

# Start and enable Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add your user to the docker group (avoids using sudo for docker commands)
sudo usermod -aG docker $USER
```

*Note: You may need to log out and log back in for the group change to take effect.*

Install Docker Compose:

```bash
sudo apt-get install -y docker-compose
# OR if you prefer the plugin version
sudo apt-get install -y docker-compose-plugin
```

## Step 3: Clone the Repository

Clone your project repository to the EC2 instance. You may need to generate an SSH key on the EC2 instance and add it to your GitHub/GitLab account if the repo is private.

```bash
git clone <your-repository-url>
cd RevHub-Social-Media-Application
```

## Step 4: Configure for Production (Optional)

By default, your `docker-compose.yml` maps:
*   Frontend: `8088` -> `80`
*   Backend: `8081` -> `8080`

If you want the application to be accessible on standard port **80** (so users don't have to type a port number) and backend on **8080**, run the app with strict port mappings or modify `docker-compose.yml`.

**To run on Port 80:**
Edit `docker-compose.yml` using `nano`:

```bash
nano docker-compose.yml
```

Change:
```yaml
ports:
  - "80:80"       # Changed from 8088:80
  - "8080:8080"   # Changed from 8081:8080
```

## Step 5: Build and Run

Run the application:

```bash
docker-compose up -d --build
```

## Step 6: Verification

Access your application in a browser:
*   **Frontend**: `http://<your-ec2-public-ip>:8088` (or `:80` if configured)
*   **Backend Health**: `http://<your-ec2-public-ip>:8081/actuator/health`

## Troubleshooting

*   **Permission Denied**: Run `sudo chmod 666 /var/run/docker.sock` (Temporary fix) or ensure user is in docker group.
*   **Ports Not Working**: Check AWS Security Group settings (Inbound Rules).
*   **Memory Issues**: If the build fails due to memory, you might need to add swap space to your Nano/Micro instance.

### Adding Swap (If needed for small instances)
```bash
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
```

## Restarting the Instance

If you stop and start your EC2 instance, the Public IP address will likely change (unless you have an Elastic IP).

1.  **Get the new Public IP** from AWS Console.
2.  **SSH** into the instance:
    ```bash
    ssh -i "path-to-key.pem" ubuntu@<new-public-ip>
    ```
3.  **Navigate** to the folder:
    ```bash
    cd RevHub-Social-Media-Application
    ```
4.  **Start the App** (Docker usually restarts automatically, but to be sure):
    ```bash
    sudo docker-compose up -d
    ```
