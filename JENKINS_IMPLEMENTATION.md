# Jenkins CI/CD Implementation Guide

This guide details how to configure Jenkins to build and deploy the RevHub application using the newly created `Jenkinsfile`.

## Prerequisites
1.  **Jenkins Installed**: Ensure Jenkins is running (locally or on a server).
2.  **Docker Installed**: Jenkins needs access to Docker to build and run containers.
3.  **Plugins**: Ensure the following Jenkins plugins are installed:
    *   Pipeline
    *   Git Plugin
    *   Docker Pipeline (optional, but good for advanced usage)

## Step 1: Create a New Pipeline Job
1.  Open your Jenkins Dashboard.
2.  Click **New Item**.
3.  Enter a name (e.g., `RevHub-Pipeline`).
4.  Select **Pipeline**.
5.  Click **OK**.

## Step 2: Configure the Pipeline
1.  Scroll down to the **Pipeline** section.
2.  Set **Definition** to `Pipeline script from SCM`.
3.  **SCM**: Select `Git`.
4.  **Repository URL**: Enter the path or URL to your repository.
    *   *If running locally*: `file:///C:/Users/ASUS/Downloads/RevHubTeam4 (3)/RevHubTeam4` (Verify the exact path or use the GitHub URL if you pushed it).
5.  **Branch Specifier**: `*/main` (or your active branch).
6.  **Script Path**: `RevHub/Jenkinsfile` (Since the Jenkinsfile is inside the `RevHub` folder in your repo, ensure this path is correct relative to the repo root).

## Step 3: Run the Build
1.  Click **Save**.
2.  Click **Build Now** in the left sidebar.
3.  Monitor the build in the **Console Output**.

## Troubleshooting
*   **Docker Permission Denied**: If Jenkins fails to run Docker commands, ensure the user running Jenkins (e.g., `jenkins` or `LocalSystem`) has permissions to access the Docker daemon. On Windows, adding the user to the `docker-users` group often helps.
*   **Port Conflicts**: The `docker-compose.yml` has been updated to use ports `9090` (Frontend) and `9091` (Backend) to avoid conflicts with Jenkins (often on `8080`).

## Pipeline Stages Explained
*   **Checkout**: Pulls code from Git.
*   **Build Frontend**: Installs npm dependencies and runs Angular build.
*   **Build Backend**: Runs Maven clean package to generate the JAR file.
*   **Docker Deploy**: Restarts the Docker containers with the new build artifacts.
