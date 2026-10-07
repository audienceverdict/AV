# Hostinger VPS deployment

These files target an Ubuntu VPS with systemd and Nginx. Confirm the VM operating
system before running the commands. This is not a shared-hosting deployment.
The public site is https://audienceverdict.in; Nginx serves the React build and
proxies /api/ to Spring Boot on localhost:8080.

## Prerequisites

Install Git, Java 21, Maven, Node.js 22 with npm, MySQL 8, Nginx, and Certbot
with its Nginx plugin. Point the domain's DNS A record to the VPS IP. Configure
www as well if using it. Allow SSH and ports 80/443; keep MySQL and 8080 private.
If a hosting control panel already manages Nginx, integrate the locations into
its site configuration rather than replacing its configuration.

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
sudo install -d /var/www/audience-verdict
sudo cp -a dist/. /var/www/audience-verdict/
sudo cp ../deploy/audience-verdict.service /etc/systemd/system/
sudo cp ../deploy/nginx.conf /etc/nginx/sites-available/audience-verdict
sudo ln -s /etc/nginx/sites-available/audience-verdict /etc/nginx/sites-enabled/audience-verdict
sudo nginx -t
sudo systemctl daemon-reload
sudo systemctl enable --now audience-verdict
sudo systemctl reload nginx
sudo certbot --nginx -d audienceverdict.in -d www.audienceverdict.in
```

Only request the www certificate if its DNS points to this VPS. Review existing
Nginx sites for conflicting domain names before enabling this site. Production
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
and frontend copy, then run `sudo systemctl restart audience-verdict`. These
commands are a manual deployment, not automatic deployment on GitHub push.
