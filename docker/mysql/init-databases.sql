CREATE DATABASE IF NOT EXISTS claimpaws_resource CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS claimpaws_reservation CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE IF NOT EXISTS claimpaws_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

GRANT ALL PRIVILEGES ON claimpaws_identity.* TO 'claimpaws'@'%';
GRANT ALL PRIVILEGES ON claimpaws_resource.* TO 'claimpaws'@'%';
GRANT ALL PRIVILEGES ON claimpaws_reservation.* TO 'claimpaws'@'%';
GRANT ALL PRIVILEGES ON claimpaws_notification.* TO 'claimpaws'@'%';
