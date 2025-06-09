CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE game_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL UNIQUE,
    townhall_lvl TINYINT DEFAULT 1,
    storage_lvl TINYINT DEFAULT 1,
    food BIGINT DEFAULT 2000,
    wood BIGINT DEFAULT 2000,
    stone BIGINT DEFAULT 1000,
    max_food BIGINT DEFAULT 5000,
    max_wood BIGINT DEFAULT 7000,
    max_stone BIGINT DEFAULT 3000,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE buildings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    type ENUM('townhall','storage','barbarian_barrack','archer_barrack','mage_barrack','knight_barrack','healer_barrack') NOT NULL,
    level TINYINT DEFAULT 1,
    is_upgrading BOOLEAN DEFAULT FALSE,
    upgrade_start TIMESTAMP NULL,
    upgrade_end TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE barrack_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    unit_type ENUM('Barbarian','Archer','Mage','Knight','Healer') NOT NULL,
    current_count INT NOT NULL DEFAULT 0,
    max_capacity INT NOT NULL,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_barrack_unit (building_id, unit_type)
);

CREATE TABLE unit_queue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    unit_type ENUM('Barbarian','Archer','Mage','Knight','Healer') NOT NULL,
    quantity SMALLINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status ENUM('training','completed') DEFAULT 'training',
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE
);

CREATE TABLE mining_areas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type ENUM('food','wood','stone') NOT NULL,
    area_level TINYINT NOT NULL CHECK (area_level IN (1, 2)),
    current_stock BIGINT NOT NULL,
    max_stock BIGINT NOT NULL,
    distance TINYINT NOT NULL CHECK (distance BETWEEN 1 AND 3)
);

CREATE TABLE creatures (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level TINYINT NOT NULL CHECK (level BETWEEN 1 AND 10),
    max_hp INT NOT NULL,
    attack_power SMALLINT NOT NULL,
    distance TINYINT NOT NULL CHECK (distance BETWEEN 1 AND 3),
    reward_food SMALLINT NOT NULL,
    reward_wood SMALLINT NOT NULL,
    reward_stone SMALLINT NOT NULL,
    max_battle_time TINYINT NOT NULL CHECK (max_battle_time BETWEEN 3 AND 7),
    spawned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_alive BOOLEAN DEFAULT TRUE
);

CREATE TABLE missions (
    id VARCHAR(36) PRIMARY KEY DEFAULT (UUID()),
    user_id VARCHAR(36) NOT NULL,
    type ENUM('mining','attack') NOT NULL,
    mining_area_id BIGINT NULL,
    creature_id BIGINT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status ENUM('in_progress','completed') DEFAULT 'in_progress',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mining_area_id) REFERENCES mining_areas(id) ON DELETE SET NULL,
    FOREIGN KEY (creature_id) REFERENCES creatures(id) ON DELETE SET NULL,
    CHECK (
        (type = 'mining' AND mining_area_id IS NOT NULL AND creature_id IS NULL) OR
        (type = 'attack' AND creature_id IS NOT NULL AND mining_area_id IS NULL)
    )
);

CREATE TABLE mission_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mission_id VARCHAR(36) NOT NULL,
    unit_type ENUM('Barbarian','Archer','Mage','Knight','Healer') NOT NULL,
    units_sent SMALLINT NOT NULL,
    units_lost SMALLINT DEFAULT 0,
    units_returned SMALLINT DEFAULT 0,
    FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

CREATE TABLE mission_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mission_id VARCHAR(36) NOT NULL UNIQUE,
    food_gained INT DEFAULT 0,
    wood_gained INT DEFAULT 0,
    stone_gained INT DEFAULT 0,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id VARCHAR(36) NOT NULL,
    title VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_missions_user_status ON missions(user_id, status);
CREATE INDEX idx_buildings_user_type ON buildings(user_id, type);
CREATE INDEX idx_unit_queue_status ON unit_queue(status, end_time);
CREATE INDEX idx_creatures_alive ON creatures(is_alive, level);
CREATE INDEX idx_messages_user ON messages(user_id, created_at);

INSERT INTO mining_areas (resource_type, area_level, current_stock, max_stock, distance) VALUES
('food', 1, 75000, 100000, 2),
('wood', 1, 60000, 80000, 3),
('stone', 1, 70000, 100000, 2),
('food', 1, 65000, 100000, 1),
('wood', 1, 55000, 80000, 1);

INSERT INTO creatures (level, max_hp, attack_power, distance, reward_food, reward_wood, reward_stone, max_battle_time) VALUES
(1, 400, 15, 1, 150, 100, 50, 3),
(1, 400, 15, 1, 150, 100, 50, 3),
(1, 400, 15, 2, 150, 100, 50, 3),
(1, 400, 15, 2, 150, 100, 50, 3),
(1, 400, 15, 3, 150, 100, 50, 3);