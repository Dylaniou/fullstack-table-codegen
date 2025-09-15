CREATE TABLE `test` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'id',
  `mobile` varchar(20) NOT NULL COMMENT 'mobile',
  `passwd` varchar(255) NOT NULL COMMENT 'passwd',
  `name` varchar(50) DEFAULT NULL COMMENT 'name',
  `sex` enum('0','1') DEFAULT NULL COMMENT 'sex',
  `age` tinyint unsigned DEFAULT NULL COMMENT 'age',
  `birthday` date DEFAULT NULL COMMENT 'birthday',
  `area` varchar(100) DEFAULT NULL COMMENT 'area',
  `score` decimal(10,2) DEFAULT NULL COMMENT 'score',
  PRIMARY KEY (`id`),
  UNIQUE KEY `mobile` (`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='test'