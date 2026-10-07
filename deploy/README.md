# Hostinger VPS deployment

These files target an Ubuntu VPS with systemd. Confirm the VM operating
system before running the commands. This is not a shared-hosting deployment.
The public site is https://audienceverdict.in. Use the existing web server and
HTTPS setup. The backend listens on localhost:8080.

## Prerequisites

Install Git, Java 21, Maven, Node.js 22 with npm, and MySQL 8. Keep the existing
web server, DNS, and HTTPS configuration. The existing site must serve the React
build with SPA fallback and forward /api/ to http://127.0.0.1:8080, preserving
the /api/ prefix. Keep MySQL and port 8080 private.

## Database and configuration

In MySQL, create a dedicated application account, substituting a strong password:

```sql
CREATE DATABASE IF NOT EXISTS movie_booking;
CREATE USER 'movie_user'@'localhost' IDENTIFIED BY 'REPLACE_WITH_DATABASE_PASSWORD';
GRANT ALL PRIVILEGES ON movie_booking.* TO 'movie_user'@'localhost';
```

Clone the repository and create the service account:

```sh
sudo git clone https://github.com/audienceverdict/AV.git /opt/audience-verdict
sudo useradd --system --home /opt/audience-verdict --shell /usr/sbin/nologin audience-verdict
sudo install -d -m 700 /etc/audience-verdict
sudo install -m 600 /opt/audience-verdict/deploy/backend.env.example /etc/audience-verdict/backend.env
sudo nano /etc/audience-verdict/backend.env
```

Replace every placeholder. Generate JWT_SECRET using `openssl rand -hex 48` and
keep it stable across deployments. Configure a working SMTP account: production
OTP login requires email delivery. Do not copy the local root/root credentials
to the server. The environment file belongs on the VM, outside Git.

## Build and start

```sh
cd /opt/audience-verdict/backend
sudo mvn -DskipTests package
cd ../frontend
sudo npm ci
sudo npm run build
sudo cp ../deploy/audience-verdict.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now audience-verdict
```

Publish frontend/dist to your existing site's document root using your current
deployment process. No web server or certificate changes are included. Production
Flyway applies pending MySQL migrations on startup; back up an existing database
before updating. The build above skips integration tests because they require
the separate movie_booking_test database; run them before release in a test environment.

## Verify and update

```sh
sudo systemctl status audience-verdict
sudo journalctl -u audience-verdict -n 100 --no-pager
curl -i https://audienceverdict.in/api/v1/movies
```

Open the website, test a direct link to a frontend route, and complete email OTP
login. The dev administrator seed does not run in production; provision the
first administrator through a controlled database procedure after registration.

For updates, pull the reviewed commit in /opt/audience-verdict, repeat the builds
and publish frontend/dist, then run `sudo systemctl restart audience-verdict`. These
commands are a manual deployment, not automatic deployment on GitHub push.
