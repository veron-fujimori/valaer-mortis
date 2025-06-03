-- VALAER MORTIS - FINAL DATABASE SCHEMA
-- Last Updated: 2025-06-02 16:21:01 UTC
-- Author: OrionHoshizora

-- 1. USERS
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. GAME_STATE
CREATE TABLE game_state (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    townhall_lvl TINYINT DEFAULT 1,
    storage_lvl TINYINT DEFAULT 1,
    food BIGINT DEFAULT 2000,
    wood BIGINT DEFAULT 2000,
    stone BIGINT DEFAULT 1000,
    max_food BIGINT DEFAULT 5000,
    max_wood BIGINT DEFAULT 7000,
    max_stone BIGINT DEFAULT 3000,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 3. BUILDINGS
CREATE TABLE buildings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('townhall','storage','barbarian_barrack','archer_barrack','mage_barrack','knight_barrack','healer_barrack') NOT NULL,
    level TINYINT DEFAULT 1,
    is_upgrading BOOLEAN DEFAULT FALSE,
    upgrade_start TIMESTAMP NULL,
    upgrade_end TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. BARRACK_UNITS
CREATE TABLE barrack_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    unit_type ENUM('Barbarian','Archer','Mage','Knight','Healer') NOT NULL,
    current_count INT NOT NULL DEFAULT 0,
    max_capacity INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE,
    UNIQUE KEY uk_barrack_unit (building_id, unit_type)
);

-- 5. UNIT_QUEUE
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

-- 6. MINING_AREAS
CREATE TABLE mining_areas (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    resource_type ENUM('food','wood','stone') NOT NULL,
    area_level TINYINT NOT NULL CHECK (area_level IN (1, 2)),
    current_stock BIGINT NOT NULL,
    max_stock BIGINT NOT NULL,
    distance TINYINT NOT NULL CHECK (distance BETWEEN 1 AND 3),
    regenerated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE
);

-- 7. CREATURES
CREATE TABLE creatures (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    level TINYINT NOT NULL CHECK (level BETWEEN 1 AND 10),
    max_hp INT NOT NULL,
    attack_power SMALLINT NOT NULL,
    distance TINYINT NOT NULL CHECK (distance BETWEEN 1 AND 3),
    reward_food SMALLINT NOT NULL,
    reward_wood SMALLINT NOT NULL,
    reward_stone SMALLINT NOT NULL,
    battle_time TINYINT NOT NULL CHECK (battle_time BETWEEN 3 AND 7),
    spawned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_alive BOOLEAN DEFAULT TRUE
);

-- 8. MISSIONS
CREATE TABLE missions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('mining','attack') NOT NULL,
    mining_area_id BIGINT NULL,
    creature_id BIGINT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status ENUM('in_progress','completed') DEFAULT 'in_progress',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mining_area_id) REFERENCES mining_areas(id) ON DELETE SET NULL,
    FOREIGN KEY (creature_id) REFERENCES creatures(id) ON DELETE SET NULL,
    CHECK (
      (type = 'mining' AND mining_area_id IS NOT NULL AND creature_id IS NULL) OR
      (type = 'attack' AND creature_id IS NOT NULL AND mining_area_id IS NULL)
    )
);

-- 9. MISSION_UNITS
CREATE TABLE mission_units (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mission_id BIGINT NOT NULL,
    unit_type ENUM('Barbarian','Archer','Mage','Knight','Healer') NOT NULL,
    units_sent SMALLINT NOT NULL,
    units_lost SMALLINT DEFAULT 0,
    units_returned SMALLINT DEFAULT 0,
    FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

-- 10. MISSION_RESULTS
CREATE TABLE mission_results (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    mission_id BIGINT NOT NULL UNIQUE,
    food_gained INT DEFAULT 0,
    wood_gained INT DEFAULT 0,
    stone_gained INT DEFAULT 0,
    success BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (mission_id) REFERENCES missions(id) ON DELETE CASCADE
);

-- 11. INDEXES FOR PERFORMANCE
CREATE INDEX idx_missions_user_status ON missions(user_id, status);
CREATE INDEX idx_buildings_user_type ON buildings(user_id, type);
CREATE INDEX idx_unit_queue_status ON unit_queue(status, end_time);
CREATE INDEX idx_creatures_alive ON creatures(is_alive, level);
CREATE INDEX idx_mining_areas_active ON mining_areas(is_active, resource_type);

-- 12. INITIAL DATA FOR MINING AREAS
INSERT INTO mining_areas (resource_type, area_level, current_stock, max_stock, distance) VALUES
('food', 1, 75000, 100000, 2),
('wood', 1, 60000, 80000, 3),
('stone', 2, 180000, 200000, 2),
('food', 2, 150000, 200000, 3),
('wood', 2, 120000, 160000, 2),
('stone', 1, 90000, 120000, 1);

-- 13. INITIAL DATA FOR CREATURES
INSERT INTO creatures (level, max_hp, attack_power, distance, reward_food, reward_wood, reward_stone, battle_time) VALUES
(1, 400, 15, 1, 150, 100, 50, 3),
(2, 700, 25, 1, 300, 200, 100, 3),
(3, 1200, 40, 2, 500, 350, 175, 4),
(4, 1800, 60, 2, 750, 500, 250, 4),
(5, 2500, 80, 2, 1000, 700, 350, 5),
(6, 3500, 100, 3, 1500, 1000, 500, 5),
(7, 4500, 120, 3, 2000, 1400, 700, 6),
(8, 6000, 150, 3, 2500, 1750, 875, 6),
(9, 8000, 180, 3, 3000, 2100, 1050, 7),
(10, 10000, 220, 3, 4000, 2800, 1400, 7);